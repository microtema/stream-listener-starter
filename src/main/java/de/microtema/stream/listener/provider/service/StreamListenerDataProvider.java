package de.microtema.stream.listener.provider.service;

import de.microtema.stream.listener.model.EventIdAware;
import de.microtema.stream.listener.model.StreamListenerEndpoint;
import de.microtema.stream.listener.support.ResponseStatus;

import java.util.List;

public interface StreamListenerDataProvider<T extends EventIdAware> {

    List<T> receive(StreamListenerEndpoint<T> endpoint);

    void commit(StreamListenerEndpoint<T> endpoint, List<ResponseStatus> responses);
}
