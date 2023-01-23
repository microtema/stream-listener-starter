package de.microtema.stream.listener.converter;

import de.microtema.model.converter.MetaConverter;
import de.microtema.stream.listener.model.EventIdAware;
import de.microtema.stream.listener.support.ResponseState;
import de.microtema.stream.listener.support.ResponseStatus;
import org.apache.commons.lang3.StringUtils;

public class EventDataToResponseStatusConverter<T extends EventIdAware> implements MetaConverter<ResponseStatus, T, String> {

    @Override
    public void update(ResponseStatus dest, T orig) {

        update(dest, orig, null);
    }

    @Override
    public void update(ResponseStatus dest, T orig, String meta) {

        dest.setId(orig.getId());

        boolean success = StringUtils.isEmpty(meta);

        if (success) {
            dest.setState(ResponseState.OK);
        } else {
            dest.setState(ResponseState.ERROR);
        }

        dest.setSuccess(success);
        dest.setErrorMessage(meta);
    }
}
