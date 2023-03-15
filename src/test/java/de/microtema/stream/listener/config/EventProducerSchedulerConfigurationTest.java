package de.microtema.stream.listener.config;

import de.microtema.stream.listener.publisher.StreamEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventProducerSchedulerConfigurationTest {

    @InjectMocks
    EventProducerSchedulerConfiguration sut;

    @Mock
    StreamEventPublisher streamEventPublisher;

    @Mock
    ScheduledTaskRegistrar taskRegistrar;

    @Captor
    ArgumentCaptor<ScheduledExecutorService> schedulerCaptor;

    @Test
    void configureTasks() {

        sut.configureTasks(taskRegistrar);

        verify(streamEventPublisher).startup(taskRegistrar);

        verify(taskRegistrar).setScheduler(schedulerCaptor.capture());
        assertNotNull(schedulerCaptor.getValue());
    }
}
