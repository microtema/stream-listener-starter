package de.microtema.stream.listener.listener;

import de.microtema.stream.listener.model.EventIdAware;
import de.microtema.stream.listener.model.StreamListenerEndpoint;

import java.util.List;


/**
 * An error handler which is called when a {@code @StreamListener} method
 * throws an exception. This is invoked higher up the stack than the
 * listener container's error handler. The error handler can return a result.
 */
@FunctionalInterface
public interface StreamEventListenerErrorHandler<T extends EventIdAware> {

    /**
     * Handle the error on record consumer.
     *
     * @param record    may not be null
     * @param exception may not be null
     * @param endpoint  may not be null
     * @return Object
     */
    default Object handleError(T record, Exception exception, StreamListenerEndpoint<T> endpoint) {

        return handleError(List.of(record), exception, endpoint);
    }

    /**
     * Handle the error on batch consumer.
     *
     * @param records   may not be null
     * @param exception may not be null
     * @param endpoint  may not be null
     * @return Object
     */
    Object handleError(List<T> records, Exception exception, StreamListenerEndpoint<T> endpoint);
}
