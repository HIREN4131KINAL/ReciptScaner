package co.smartreceipts.android.workers.reports.writers;

import android.support.annotation.NonNull;

import co.smartreceipts.android.workers.reports.columns.TableColumns;

/**
 * Creates a report of type {@link T} via the {@link #write(co.smartreceipts.android.workers.reports.columns.TableColumns)} method
 *
 * @author williambaumann
 */
public interface TableGenerator<T> {


    /**
     * Writes all relevant data in the {@link co.smartreceipts.android.workers.reports.columns.TableColumns} implementation
     *
     * @param columns - the columns to write
     * @return a "written" version of the column data
     */
    @NonNull
    T write(TableColumns columns);
}
