package de.microtema.stream.listener.annotation;

import de.microtema.stream.listener.listener.RecordFilterStrategy;
import de.microtema.stream.listener.listener.StreamEventListenerErrorHandler;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Annotation that marks a method to be registered as listener. Exactly one of the topic attribute must be specified.
 * The annotated method must expect arguments of typo single record or collection of records.
 * It will typically have a void return type; if not, the returned value will be ignored when called through the factory.
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StreamListener {

    /**
     * The unique identifier of the container for this listener.
     * <p>If none is specified an auto-generated id is used.
     * <p>SpEL {@code #{...}} and property place holders {@code ${...}} are supported.
     *
     * @return the {@code id} for the container managing for this endpoint.
     */
    String id() default "";

    /**
     * Intended to be used when no other attributes are needed, for example: @StreamListener("topicBeanName").
     *
     * @return the topic names or expressions (SpEL) to listen to.
     */
    @AliasFor("topics")
    String[] value() default {};

    /**
     * The topics for this listener.
     * The entries can be 'topic name', 'property-placeholder keys' or 'expressions'.
     * An expression must be resolved to the topic name.
     * <p>
     *
     * @return the topic names or expressions (SpEL) to listen to.
     */
    String[] topics() default {};

    /**
     * Set an {@link StreamEventListenerErrorHandler} bean
     * name to invoke if the listener method throws an exception. If a SpEL expression is
     * provided ({@code #{...}}), the expression can either evaluate to a
     * {@link StreamEventListenerErrorHandler} instance or a
     * bean name.
     *
     * @return the error handler.
     */
    String errorHandler() default "";

    /**
     * Override the {@code group.id} property for the consumer factory with this value
     * for this listener only.
     * <p>SpEL {@code #{...}} and property place holders {@code ${...}} are supported.
     *
     * @return the group id.
     */
    String groupId() default "";

    /**
     * When {@link #groupId() groupId} is not provided, use the {@link #id() id} (if
     * provided) as the {@code group.id} property for the consumer. Set to false, to use
     * the {@code group.id} from the consumer factory.
     *
     * @return false to disable.
     * @since 1.3
     */
    boolean idIsGroup() default true;

    /**
     * Override the container factory's {@code concurrency} setting for this listener. May
     * be a property placeholder or SpEL expression that evaluates to a {@link Number}, in
     * which case {@link Number#intValue()} is used to obtain the value.
     * <p>SpEL {@code #{...}} and property place holders {@code ${...}} are supported.
     *
     * @return the concurrency.
     */
    String concurrency() default "";

    /**
     * delay – the time from now to delay execution
     * <p>SpEL {@code #{...}} and property place holders {@code ${...}} are supported.
     *
     * @return the delay.
     */
    String delay() default "";

    /**
     * Set to true or false, to override the default setting in the container factory. May
     * be a property placeholder or SpEL expression that evaluates to a {@link Boolean} or
     * a {@link String}, in which case the {@link Boolean#parseBoolean(String)} is used to
     * obtain the value.
     * <p>SpEL {@code #{...}} and property place holders {@code ${...}} are supported.
     *
     * @return true to auto start, false to not auto start.
     */
    String autoStartup() default "";

    /**
     * Stream consumer properties; they will supersede any properties with the same name
     * defined in the consumer factory (if the consumer factory supports property overrides).
     * <p>
     * <b>Supported Syntax</b>
     * <p>The supported syntax for key-value pairs is the same as the
     * syntax defined for entries in a Java
     * {@linkplain java.util.Properties#load(java.io.Reader) properties file}:
     * <ul>
     * <li>{@code key=value}</li>
     * <li>{@code key:value}</li>
     * <li>{@code key value}</li>
     * </ul>
     * {@code group.id} and {@code client.id} are ignored.
     * <p>SpEL {@code #{...}} and property place holders {@code ${...}} are supported.
     * SpEL expressions must resolve to a {@link String}, a @{link String[]} or a
     * {@code Collection<String>} where each member of the array or collection is a
     * property name + value with the above formats.
     *
     * @return the properties.
     */
    String[] properties() default {};

    /**
     * Set the bean name of a MessageConverter bean name.
     *
     * @return the bean name.
     */
    String contentTypeConverter() default "";

    /**
     * Set the bean name of a Data Provider bean name.
     *
     * @return the bean name.
     */
    String dataProvider() default "";

    /**
     * The listener method signature should receive a {@code List<?>}; refer to the reference
     * documentation. This allows a single container factory to be used for both record
     * and batch listeners; previously separate container factories were required.
     *
     * @return "true" for the annotated method to be a batch listener or "false" for a
     * record listener. If not set, the container factory setting is used. SpEL and
     * property placeholders are not supported because the listener type cannot be
     * variable.
     * @see Boolean#parseBoolean(String)
     */
    String batch() default "";

    /**
     * Set an {@link RecordFilterStrategy} bean
     * name to override the strategy configured on the container factory. If a SpEL
     * expression is provided ({@code #{...}}), the expression can either evaluate to a
     * {@link RecordFilterStrategy} instance or
     * a bean name.
     *
     * @return the record filter strategy handler.
     */
    String filter() default "";
}
