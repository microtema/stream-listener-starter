package de.microtema.stream.listener.model;

import de.microtema.stream.listener.converter.RecordConverter;
import de.microtema.stream.listener.listener.RecordFilterStrategy;
import de.microtema.stream.listener.listener.StreamEventListenerErrorHandler;
import de.microtema.stream.listener.provider.service.StreamListenerDataProvider;

import java.lang.reflect.Method;
import java.util.Properties;


public class StreamListenerEndpoint<T extends EventIdAware> {

    private String id;
    private String groupId;
    private Object bean;
    private Method method;
    private String beanName;
    private boolean batch;
    private String topic;
    private String cron = "0 */1 * ? * *";
    private boolean autoStartup;
    private boolean concurrency;

    /**
     * delay – the time from now to delay execution
     */
    private long delay = 0;
    private Object[] methodParameters;
    private Class<T> recordType;
    private Properties consumerProperties;
    private StreamEventListenerErrorHandler<T> errorHandler;
    private RecordFilterStrategy<T> recordFilterStrategy;
    private RecordConverter<T> recordConverter;
    private StreamListenerDataProvider<T> dataProvider;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public boolean isBatch() {
        return batch;
    }

    public void setBatch(boolean batch) {
        this.batch = batch;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public boolean isAutoStartup() {
        return autoStartup;
    }

    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    public boolean isConcurrency() {
        return concurrency;
    }

    public void setConcurrency(boolean concurrency) {
        this.concurrency = concurrency;
    }

    public Object[] getMethodParameters() {
        return methodParameters;
    }

    public void setMethodParameters(Object[] methodParameters) {
        this.methodParameters = methodParameters;
    }

    public Class<T> getRecordType() {
        return recordType;
    }

    public void setRecordType(Class<T> recordType) {
        this.recordType = recordType;
    }

    public Properties getConsumerProperties() {
        return consumerProperties;
    }

    public void setConsumerProperties(Properties consumerProperties) {
        this.consumerProperties = consumerProperties;
    }

    public StreamEventListenerErrorHandler<T> getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(StreamEventListenerErrorHandler<T> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public RecordFilterStrategy<T> getRecordFilterStrategy() {
        return recordFilterStrategy;
    }

    public void setRecordFilterStrategy(RecordFilterStrategy<T> recordFilterStrategy) {
        this.recordFilterStrategy = recordFilterStrategy;
    }

    public RecordConverter<T> getRecordConverter() {
        return recordConverter;
    }

    public void setRecordConverter(RecordConverter<T> recordConverter) {
        this.recordConverter = recordConverter;
    }

    public StreamListenerDataProvider<T> getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(StreamListenerDataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }
}
