package de.microtema.stream.listener.service;

import de.microtema.stream.listener.converter.EventDataToResponseStatusConverter;
import de.microtema.stream.listener.listener.RecordFilterStrategy;
import de.microtema.stream.listener.model.EventIdAware;
import de.microtema.stream.listener.model.StreamListenerEndpoint;
import de.microtema.stream.listener.support.ResponseState;
import de.microtema.stream.listener.support.ResponseStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.log.LogAccessor;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@ConditionalOnProperty(prefix = "stream-listener", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StreamListenerExecutionService<T extends EventIdAware> {

    private final LogAccessor log = new LogAccessor(LogFactory.getLog(getClass()));

    private final EventDataToResponseStatusConverter<T> responseStatusConverter;

    public StreamListenerExecutionService(EventDataToResponseStatusConverter<T> responseStatusConverter) {
        this.responseStatusConverter = responseStatusConverter;
    }

    public boolean executeEndpointMethod(StreamListenerEndpoint<T> endpoint) {

        var dataProvider = endpoint.getDataProvider();

        var records = dataProvider.receive(endpoint);

        if (CollectionUtils.isEmpty(records)) {

            log.trace(() -> "Skip invocation of [" + endpoint.getId() + "] due to empty records");

            return false;
        }

        log.trace(() -> "Received ({}) record(s) for [" + endpoint.getId() + "]");

        executeImpl(endpoint, records);

        return true;
    }

    private void executeImpl(StreamListenerEndpoint<T> endpoint, List<T> records) {

        var dataProvider = endpoint.getDataProvider();
        var endpointId = endpoint.getId();
        var batch = endpoint.isBatch();

        List<ResponseStatus> responses;
        var startMillis = System.currentTimeMillis();

        if (batch) {

            responses = executeEndpoint(endpoint, records);
        } else {

            responses = records.stream().map(it -> executeEndpoint(endpoint, it)).collect(Collectors.toList());
        }

        var durationFormat = getDuration(startMillis);

       // log.info(() -> String.format("Invocation of [%s] within (%s) record(s) completed. Duration [%s]", endpointId, records.size(), durationFormat));

        dataProvider.commit(endpoint, responses);
    }

    private String getDuration(long startMillis) {

        var duration = System.currentTimeMillis() - startMillis;

        return DurationFormatUtils.formatDurationHMS(duration);
    }

    private Object[] concatMethodParameters(Object recordOrRecords, Object... methodParameters) {

        var arguments = new Object[1 + methodParameters.length];

        arguments[0] = recordOrRecords;

        System.arraycopy(methodParameters, 0, arguments, 1, methodParameters.length);

        return arguments;
    }

    private ResponseStatus executeEndpoint(StreamListenerEndpoint<T> endpoint, T record) {

        var methodParameters = endpoint.getMethodParameters();
        var endpointBean = endpoint.getBean();
        var endpointMethod = endpoint.getMethod();

        var responseStatus = filterRecordIfNecessary(record, endpoint.getRecordFilterStrategy());

        if (Objects.nonNull(responseStatus)) {
            return responseStatus;
        }

        var parameters = concatMethodParameters(record, methodParameters);

        try {

            executeEndpointMethod(endpointMethod, endpointBean, parameters);

            return responseStatusConverter.convert(record);
        } catch (Exception ex) {

            handleError(record, ex, endpoint);

            var message = String.format("Unable to execute endpoint [%s] record [%s]. Message: %s", endpoint.getId(), record.getEventId(), ex.getMessage());

            return responseStatusConverter.convert(record, message);
        }
    }

    private List<ResponseStatus> executeEndpoint(StreamListenerEndpoint<T> endpoint, List<T> records) {

        var methodParameters = endpoint.getMethodParameters();
        var endpointBean = endpoint.getBean();
        var endpointMethod = endpoint.getMethod();

        var responseStatuses = filterRecordsIfNecessary(records, endpoint.getRecordFilterStrategy());

        var parameters = concatMethodParameters(records, methodParameters);

        try {

            executeEndpointMethod(endpointMethod, endpointBean, parameters);

            responseStatuses.addAll(responseStatusConverter.convertList(records));
        } catch (Exception ex) {

            handleError(records, ex, endpoint);

            var message = String.format("Unable to execute endpoint [%s] within (%s) record(s). Message: %s", endpoint.getId(), records.size(), ex.getMessage());

            responseStatuses.addAll(responseStatusConverter.convertList(records, message));
        }

        return responseStatuses;
    }

    private List<ResponseStatus> filterRecordsIfNecessary(List<T> records, RecordFilterStrategy<T> recordFilterStrategy) {

        if (Objects.isNull(recordFilterStrategy)) {
            return new ArrayList<>();
        }

        var filterBatch = recordFilterStrategy.filterBatch(records);

        var responseStatuses = responseStatusConverter.convertList(filterBatch);

        responseStatuses.forEach(it -> it.setState(ResponseState.SKIPPED));

        return new ArrayList<>(responseStatuses);
    }

    private ResponseStatus filterRecordIfNecessary(T record, RecordFilterStrategy<T> recordFilterStrategy) {

        if (Objects.isNull(recordFilterStrategy)) {
            return null;
        }

        var filterRecord = recordFilterStrategy.filter(record);

        if (filterRecord) {
            return null;
        }

        var responseStatus = responseStatusConverter.convert(record);

        responseStatus.setState(ResponseState.SKIPPED);

        return responseStatus;
    }

    private void handleError(T record, Exception exception, StreamListenerEndpoint<T> endpoint) {

        var errorHandler = endpoint.getErrorHandler();

        if (Objects.isNull(errorHandler)) {
            return;
        }

        errorHandler.handleError(record, exception, endpoint);
    }

    private void handleError(List<T> records, Exception exception, StreamListenerEndpoint<T> endpoint) {

        var errorHandler = endpoint.getErrorHandler();

        if (Objects.isNull(errorHandler)) {
            return;
        }

        errorHandler.handleError(records, exception, endpoint);
    }

    private void executeEndpointMethod(Method endpointMethod, Object endpointBean, Object... args) {

        try {
            ReflectionUtils.makeAccessible(endpointMethod);
            endpointMethod.invoke(endpointBean, args);
        } catch (InvocationTargetException ex) {
            ReflectionUtils.rethrowRuntimeException(ex.getTargetException());
        } catch (IllegalAccessException ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }
}
