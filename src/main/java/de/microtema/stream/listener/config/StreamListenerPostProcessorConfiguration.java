package de.microtema.stream.listener.config;

import de.microtema.stream.listener.converter.EventDataToResponseStatusConverter;
import de.microtema.stream.listener.processor.StreamListenerPostProcessor;
import de.microtema.stream.listener.provider.config.DataProviderConfiguration;
import de.microtema.stream.listener.publisher.StreamEventPublisher;
import de.microtema.stream.listener.service.StreamListenerExecutionService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;

@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Import(value = {
        DataProviderConfiguration.class,
        EventDataToResponseStatusConverter.class,
        StreamEventPublisher.class,
        StreamListenerExecutionService.class,
        EventProducerSchedulerConfiguration.class
})
@ConditionalOnProperty(prefix = "stream-listener", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StreamListenerPostProcessorConfiguration {

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean(name = "de.microtema.stream.listener.config.streamListenerPostProcessor")
    public StreamListenerPostProcessor streamListenerPostProcessor(StreamEventPublisher streamEventPublisher) {

        return new StreamListenerPostProcessor(streamEventPublisher);
    }
}
