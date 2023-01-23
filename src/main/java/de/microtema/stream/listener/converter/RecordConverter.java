package de.microtema.stream.listener.converter;

import de.microtema.model.converter.MetaConverter;
import de.microtema.stream.listener.model.EventIdAware;
import de.microtema.stream.listener.model.StreamListenerEndpoint;

import java.util.Map;

public interface RecordConverter<D extends EventIdAware> extends MetaConverter<D, Map<String, Object>, StreamListenerEndpoint<D>> {
}
