package de.microtema.stream.listener.converter;

import de.microtema.model.builder.annotation.Model;
import de.microtema.model.builder.util.FieldInjectionUtil;
import de.microtema.stream.listener.model.EventIdAware;
import de.microtema.stream.listener.support.ResponseState;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventDataToResponseStatusConverterTest {

    @Inject
    EventDataToResponseStatusConverter<EventIdAware> sut;

    @Model
    String eventId;

    @Model
    Long systemId;

    EventIdAware record = new EventIdAware() {
        @Override
        public String getEventId() {
            return eventId;
        }

        @Override
        public Long getId() {
            return systemId;
        }
    };

    @Model
    String errorMessage;

    @BeforeEach
    void setUp() {
        FieldInjectionUtil.injectFields(this);
    }

    @Test
    void convert() {

        var answer = sut.convert(record, errorMessage);

        assertNotNull(answer);

        assertEquals(record.getId(), answer.getId());
        assertEquals(errorMessage, answer.getErrorMessage());
        Assertions.assertEquals(ResponseState.ERROR, answer.getState());
        assertFalse(answer.isSuccess());
    }
}
