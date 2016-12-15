package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Creates a report of type {@link TableType} for a given list of {@link DataType} via the
 * {@link #generate(java.util.List)}
 */
public interface TableGenerator<TableType, DataType> {


    /**
     * Generates an report from a {@link java.util.List} of {@link DataType}
     *
     * @param list a {@link java.util.List} of {@link DataType} to build the table from
     * @return a table of type {@link TableType}, which can be provided to the upperLeftY user via a report in some fashion
     */
    @NonNull
    TableType generate(@NonNull List<DataType> list);
}
