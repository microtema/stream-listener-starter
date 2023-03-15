package de.microtema.stream.listener.publisher;

import de.microtema.stream.listener.model.StreamListenerEndpoint;
import de.microtema.stream.listener.service.StreamListenerExecutionService;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.log.LogAccessor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
@ConditionalOnProperty(prefix = "stream-listener", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StreamEventPublisher {

    private final LogAccessor log = new LogAccessor(LogFactory.getLog(getClass()));

    private final Set<String> endpointIds = Collections.synchronizedSet(new HashSet<>());
    private final Set<StreamListenerEndpoint> endpoints = Collections.synchronizedSet(new HashSet<>());

    private final ScheduledExecutorService scheduledExecutorService;
    private final StreamListenerExecutionService streamListenerExecutionService;

    private ScheduledTaskRegistrar scheduledTaskRegistrar;

    public StreamEventPublisher(ScheduledExecutorService scheduledExecutorService, StreamListenerExecutionService streamListenerExecutionService) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.streamListenerExecutionService = streamListenerExecutionService;
    }

    public void registerStreamListenerEndpoint(StreamListenerEndpoint endpoint) {

        log.info(() -> String.format("Endpoint [%s][%s] for topic [%s] within concurrency [%s] successfully registered", endpoint.getGroupId(), endpoint.getId(), endpoint.getTopic(), endpoint.isConcurrency()));

        endpoints.add(endpoint);
    }

    public void startup(ScheduledTaskRegistrar scheduledTaskRegistrar) {

        this.scheduledTaskRegistrar = scheduledTaskRegistrar;

        for (var endpoint : endpoints) {

            var autoStartup = endpoint.isAutoStartup();

            if (autoStartup) {
                publishEvent(endpoint);
            } else {
                scheduledTaskRegistrar.addCronTask(() -> publishEvent(endpoint), endpoint.getCron());
            }
        }
    }

    public void destroy() {

        scheduledTaskRegistrar.destroy();
    }

    private void publishEvent(StreamListenerEndpoint endpoint) {

        var listenerId = endpoint.getId();
        var autoStartup = endpoint.isAutoStartup();
        var delay = endpoint.getDelay();

        if (!endpointIds.add(listenerId)) {

            log.warn(() -> String.format("RACE CONDITION DETECTED on [%s][%s] endpoint", endpoint.getGroupId(), endpoint.getId()));

            return;
        }

        boolean hasMore;

        try {
            hasMore = streamListenerExecutionService.executeEndpointMethod(endpoint);
        } finally {
            endpointIds.remove(listenerId);
        }

        if (!hasMore) {
            return;
        }

        if (autoStartup) {

            log.trace(() -> String.format("Call next task on [%s][%s] endpoint", endpoint.getGroupId(), endpoint.getId()));

            // Small delay, due to other threads/systems
            sleepTill(delay);

            publishEvent(endpoint);
        } else {

            log.trace(() -> String.format("Reschedule next async task on [%s][%s] endpoint", endpoint.getGroupId(), endpoint.getId()));

            scheduledExecutorService.schedule(() -> publishEvent(endpoint), delay, TimeUnit.MILLISECONDS);
        }
    }

    private void sleepTill(long delayInMillis) {

        if (delayInMillis < 1) {
            return;
        }

        try {
            Thread.sleep(delayInMillis);
        } catch (InterruptedException e) {

            log.warn(() -> "Interrupted Exception during the thread sleep! Message: " + e.getMessage());

            Thread.currentThread().interrupt();
        }

    }
}
