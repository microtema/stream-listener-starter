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

    private static final long DELAY = TimeUnit.SECONDS.toMillis(1);

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

        log.info(() -> String.format("Endpoint [%s] for topic [%s] successfully registered", endpoint.getId(), endpoint.getTopic()));

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

        if (!endpointIds.add(listenerId)) {

            log.warn(() -> "RACE CONDITION DETECTED on [" + endpoint.getId() + "]");

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

            log.trace(() -> "Call next task on [" + endpoint.getId() + "]");

            // Small delay, due to other threads
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                log.warn(() -> "Interrupted Exception during the thread sleep! Message: " + e.getMessage());

                throw new IllegalStateException("Interrupted Exception during the thread sleep!", e);
            }

            publishEvent(endpoint);
        } else {

            log.trace(() -> "Reschedule next async task on [" + endpoint.getId() + "]");

            scheduledExecutorService.schedule(() -> publishEvent(endpoint), DELAY, TimeUnit.MILLISECONDS);
        }
    }
}
