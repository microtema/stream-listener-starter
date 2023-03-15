package de.microtema.stream.listener.processor;

import de.microtema.model.builder.annotation.Model;
import de.microtema.model.builder.util.FieldInjectionUtil;
import de.microtema.model.builder.util.FieldUtil;
import de.microtema.stream.listener.annotation.StreamListener;
import de.microtema.stream.listener.model.EventIdAware;
import de.microtema.stream.listener.publisher.StreamEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StreamListenerPostProcessorTest {

    @InjectMocks
    StreamListenerPostProcessor sut;

    @Mock
    StreamEventPublisher streamEventPublisher;

    @Mock
    BeanFactory beanFactory;

    @Mock
    BeanExpressionContext beanExpressionContext;

    @Mock
    BeanExpressionResolver beanExpressionResolver;

    @Model
    EventListener bean;

    @Model
    String beanName;

    @BeforeEach
    void setUp() throws Exception {
        FieldInjectionUtil.injectFields(this);

        FieldUtil.setFieldValue(sut.getClass().getDeclaredField("beanFactory"), sut, beanFactory);
        FieldUtil.setFieldValue(sut.getClass().getDeclaredField("beanExpressionContext"), sut, beanExpressionContext);
        FieldUtil.setFieldValue(sut.getClass().getDeclaredField("beanExpressionResolver"), sut, beanExpressionResolver);
    }

    @Test
    void requiresDestruction() {

        boolean answer = sut.requiresDestruction(bean);

        assertTrue(answer);
    }

    @Test
    void postProcessBeforeInitialization() {

        Object answer = sut.postProcessBeforeInitialization(bean, beanName);

        assertNotNull(answer);
        assertEquals(bean, answer);
    }

    @Test
    void postProcessAfterInitialization() {

        doNothing().when(streamEventPublisher).registerStreamListenerEndpoint(any());

        when(beanExpressionResolver.evaluate(any(), any())).then(it -> {

            Object[] arguments = it.getArguments();

            if(arguments.length == 0){
                return null;
            }

            return it.getArgument(0);
        });

        Object answer = sut.postProcessAfterInitialization(bean, beanName);

        assertNotNull(answer);
        assertEquals(bean, answer);
    }

    static public class EventListener {

        @StreamListener(topics = "dwh_invoice_updated")
        public void on(EventIdAware event) {

        }
    }
}
