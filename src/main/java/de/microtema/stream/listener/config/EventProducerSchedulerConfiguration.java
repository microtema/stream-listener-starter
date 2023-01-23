package de.microtema.stream.listener.config;

import de.microtema.stream.listener.publisher.StreamEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

@Configuration
@ConditionalOnProperty(prefix = "stream-listener", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EventProducerSchedulerConfiguration implements SchedulingConfigurer {

    private final StreamEventPublisher streamEventPublisher;

    public EventProducerSchedulerConfiguration(StreamEventPublisher streamEventPublisher) {
        this.streamEventPublisher = streamEventPublisher;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(10));

        streamEventPublisher.startup(taskRegistrar);
    }
}
