package de.microtema.stream.listener.processor;

import de.microtema.stream.listener.annotation.StreamListener;
import de.microtema.stream.listener.converter.RecordConverter;
import de.microtema.stream.listener.listener.RecordFilterStrategy;
import de.microtema.stream.listener.listener.StreamEventListenerErrorHandler;
import de.microtema.stream.listener.model.EventIdAware;
import de.microtema.stream.listener.model.StreamListenerEndpoint;
import de.microtema.stream.listener.provider.service.StreamListenerDataProvider;
import de.microtema.stream.listener.publisher.StreamEventPublisher;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.log.LogAccessor;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StreamListenerPostProcessor implements DestructionAwareBeanPostProcessor, ApplicationContextAware {

    private final LogAccessor log = new LogAccessor(LogFactory.getLog(getClass()));

    private static final String APP_ID = Optional.ofNullable(System.getenv("HOSTNAME")).orElseGet(() -> UUID.randomUUID().toString());

    private final AtomicInteger counter;
    private final ListenerScope listenerScope;
    private final StreamEventPublisher streamEventPublisher;

    private BeanFactory beanFactory;
    private BeanExpressionContext beanExpressionContext;
    private BeanExpressionResolver beanExpressionResolver;

    public StreamListenerPostProcessor(StreamEventPublisher streamEventPublisher) {
        this.streamEventPublisher = streamEventPublisher;
        counter = new AtomicInteger();
        listenerScope = new ListenerScope();
        beanExpressionResolver = new StandardBeanExpressionResolver();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        beanFactory = applicationContext;

        if (applicationContext instanceof ConfigurableApplicationContext context) {
            beanFactory = context.getBeanFactory();
        }

        if (beanFactory instanceof ConfigurableListableBeanFactory factory) {
            beanExpressionResolver = factory.getBeanExpressionResolver();
            beanExpressionContext = new BeanExpressionContext(factory, listenerScope);
        }
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        Optional.ofNullable(streamEventPublisher).ifPresent(StreamEventPublisher::destroy);
    }

    @Override
    public boolean requiresDestruction(Object bean) {
        return true;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        var targetClass = AopUtils.getTargetClass(bean);

        var methods = MethodIntrospector.selectMethods(targetClass, (MethodIntrospector.MetadataLookup<StreamListener>) it -> AnnotatedElementUtils.findMergedAnnotation(it, StreamListener.class));

        methods.forEach((method, streamListener) -> streamEventPublisher.registerStreamListenerEndpoint(createStreamListenerEndpoint(method, bean, beanName, streamListener)));

        return bean;
    }

    private <T extends EventIdAware> StreamListenerEndpoint<T> createStreamListenerEndpoint(Method method, Object bean, String beanName, StreamListener streamListener) {

        var endpoint = new StreamListenerEndpoint<T>();

        endpoint.setMethod(method);
        endpoint.setBean(bean);
        endpoint.setBeanName(beanName);

        endpoint.setId(getEndpointId(streamListener, beanName));
        endpoint.setGroupId(getEndpointGroupId(streamListener, endpoint.getId()));

        // NOTE: We support only one topic per consumer.
        endpoint.setTopic(streamListener.topics()[0]);

        endpoint.setErrorHandler(resolveErrorHandler(streamListener));
        endpoint.setRecordFilterStrategy(resolveRecordFilterStrategy(streamListener));
        endpoint.setRecordConverter(resolveContentTypeConverter(streamListener));
        endpoint.setDataProvider(resolveDataProvider(streamListener));
        endpoint.setConsumerProperties(resolveStreamProperties(streamListener.properties()));

        endpoint.setBatch(isBatchConsumer(method, streamListener));
        endpoint.setAutoStartup(isAutoStartup(streamListener));
        endpoint.setMethodParameters(resolveMethodParameters(method));
        endpoint.setRecordType(resolveRecordTypeReference(method));

        return endpoint;
    }

    private <T> Class<T> resolveRecordTypeReference(Method method) {

        Class<?>[] parameterTypes = method.getParameterTypes();

        return (Class<T>) parameterTypes[0];
    }

    /**
     * NOTE: Not supported yet.
     *
     * @param method may not be null
     * @return Object Array
     */
    private Object[] resolveMethodParameters(Method method) {
        return new Object[0];
    }

    private boolean isBatchConsumer(Method method, StreamListener streamListener) {

        if (StringUtils.hasText(streamListener.batch())) {
            return Boolean.parseBoolean(streamListener.batch());
        }

        Class<?>[] parameterTypes = method.getParameterTypes();

        Class<?> recordParameterType = parameterTypes[0];

        return Collection.class.isAssignableFrom(recordParameterType);
    }

    private boolean isAutoStartup(StreamListener streamListener) {

        if (StringUtils.hasText(streamListener.autoStartup())) {

            var autoStartup = resolveExpressionAsString(streamListener.autoStartup(), "autoStartup");

            return Boolean.parseBoolean(autoStartup);
        }

        return false;
    }

    private String getEndpointGroupId(StreamListener streamListener, String id) {

        String groupId = null;

        if (StringUtils.hasText(streamListener.groupId())) {
            groupId = resolveExpressionAsString(streamListener.groupId(), "groupId");
        }

        if (groupId == null && streamListener.idIsGroup() && StringUtils.hasText(streamListener.id())) {
            groupId = id;
        }

        if (groupId == null) {
            groupId = resolveExpressionAsString("${spring.application.name}", "spring.application.name");
        }

        return groupId;
    }

    private Properties resolveStreamProperties(String[] propertyStrings) {

        var properties = new Properties();

        if (propertyStrings.length == 0) {

            return properties;
        }

        for (String property : propertyStrings) {
            Object value = resolveExpression(property);
            if (value instanceof String) {
                loadProperty(properties, property, value);
            } else if (value instanceof String[] props) {

                for (String prop : props) {
                    loadProperty(properties, prop, prop);
                }
            } else if (value instanceof Collection values) {

                if (values.size() > 0 && values.iterator().next() instanceof String) {
                    for (String prop : (Collection<String>) value) {
                        loadProperty(properties, prop, prop);
                    }
                }
            } else {
                throw new IllegalStateException("'properties' must resolve to a String, a String[] or Collection<String>");
            }
        }

        return properties;
    }

    private void loadProperty(Properties properties, String property, Object value) {
        try {
            properties.load(new StringReader((String) value));
        } catch (IOException e) {
            log.error("Failed to load property " + property + ", continuing...");
        }
    }

    private String getEndpointId(StreamListener kafkaListener, String beanName) {

        var id = kafkaListener.id();

        if (StringUtils.hasText(id)) {

            return resolveExpressionAsString(id, "id");
        }

        return APP_ID + "_" + beanName + "_" + counter.getAndIncrement();
    }

    private <T extends EventIdAware> StreamEventListenerErrorHandler<T> resolveErrorHandler(StreamListener streamListener) {

        var errorHandlerName = streamListener.errorHandler();
        var errorHandler = resolveExpression(errorHandlerName);

        if (errorHandler instanceof StreamEventListenerErrorHandler handler) {
            return handler;
        }

        var errorHandlerBeanName = resolveExpressionAsString(errorHandlerName, "errorHandler");

        if (StringUtils.hasText(errorHandlerBeanName)) {
            return beanFactory.getBean(errorHandlerBeanName, StreamEventListenerErrorHandler.class);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends EventIdAware> StreamListenerDataProvider<T> resolveDataProvider(StreamListener streamListener) {

        var dataProvider = streamListener.dataProvider();

        var recordConverter = resolveExpression(dataProvider);

        if (recordConverter instanceof StreamListenerDataProvider provider) {
            return provider;
        }

        var recordFilterStrategyBeanName = resolveExpressionAsString(dataProvider, "dataProvider");

        if (StringUtils.hasText(recordFilterStrategyBeanName)) {
            return beanFactory.getBean(recordFilterStrategyBeanName, StreamListenerDataProvider.class);
        }

        return beanFactory.getBean(StreamListenerDataProvider.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends EventIdAware> RecordConverter<T> resolveContentTypeConverter(StreamListener streamListener) {

        var typeConverter = streamListener.contentTypeConverter();

        var recordConverter = resolveExpression(typeConverter);

        if (recordConverter instanceof RecordConverter converter) {
            return converter;
        }

        var recordFilterStrategyBeanName = resolveExpressionAsString(typeConverter, "contentTypeConverter");

        if (StringUtils.hasText(recordFilterStrategyBeanName)) {
            return beanFactory.getBean(recordFilterStrategyBeanName, RecordConverter.class);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends EventIdAware> RecordFilterStrategy<T> resolveRecordFilterStrategy(StreamListener streamListener) {

        var filterName = streamListener.filter();

        var recordFilterStrategy = resolveExpression(filterName);

        if (recordFilterStrategy instanceof RecordFilterStrategy filter) {
            return filter;
        }

        var recordFilterStrategyBeanName = resolveExpressionAsString(filterName, "filter");

        if (StringUtils.hasText(recordFilterStrategyBeanName)) {
            return beanFactory.getBean(recordFilterStrategyBeanName, RecordFilterStrategy.class);
        }

        return null;
    }

    private Object resolveExpression(String value) {

        var resolve = resolve(value);

        return beanExpressionResolver.evaluate(resolve, beanExpressionContext);
    }

    private String resolve(String value) {

        if (beanFactory != null && beanFactory instanceof ConfigurableBeanFactory factory) {

            return factory.resolveEmbeddedValue(value);
        }

        return value;
    }

    private String resolveExpressionAsString(String value, String attribute) {

        var resolved = resolveExpression(value);

        if (resolved instanceof String val) {
            return val;
        }

        if (Objects.isNull(resolved)) {
            return null;
        }

        throw new IllegalStateException("[" + attribute + "] must resolve to a String. Resolved to [" + resolved.getClass() + "] for [" + value + "]");
    }
}
