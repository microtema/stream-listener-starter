package de.microtema.stream.listener.provider.service;

import de.microtema.stream.listener.model.EventIdAware;
import de.microtema.stream.listener.model.StreamListenerEndpoint;
import de.microtema.stream.listener.support.ResponseStatus;

import java.util.List;

/**
 * Stream Listener Data Provider provide receive and commit implementation and should be implemented by subscriber
 * @param <T>
 */
public interface StreamListenerDataProvider<T extends EventIdAware> {

    /**
     * Receive records by specific endpoint
     *
     * @param endpoint may not be null
     * @return List
     */
    List<T> receive(StreamListenerEndpoint<T> endpoint);

    /**
     * Commit received records within specific endpoint
     *
     * @param endpoint may not be null
     * @param responses may not be null or empty
     */
    void commit(StreamListenerEndpoint<T> endpoint, List<ResponseStatus> responses);
}
