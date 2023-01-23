package de.microtema.stream.listener.annotation;

import de.microtema.stream.listener.config.StreamListenerPostProcessorConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({StreamListenerPostProcessorConfiguration.class})
public @interface EnableStreamListener {
}
