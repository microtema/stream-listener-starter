package de.microtema.stream.listener.listener;

import de.microtema.stream.listener.model.EventIdAware;

import java.util.List;

@FunctionalInterface
public interface RecordFilterStrategy<T extends EventIdAware> {

    /**
     * Return true if the record should be discarded.
     *
     * @param record the record may not be null.
     * @return true to discard.
     */
    boolean filter(T record);

    /**
     * Filter an entire batch of records; to filter all records, return an empty list, not
     * null.
     *
     * @param records the records.
     * @return the filtered records.
     */
    default List<T> filterBatch(List<T> records) {

        records.removeIf(this::filter);

        return records;
    }

}
