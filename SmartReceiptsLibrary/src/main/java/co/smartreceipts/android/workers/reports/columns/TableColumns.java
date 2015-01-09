package co.smartreceipts.android.workers.reports.columns;

import android.support.annotation.NonNull;

/**
 * Defines a contract for which we can generate a column-based report
 *
 * @author williambaumann
 * TODO: Refactor/generalize this to also work for receipts
 */
public interface TableColumns {

    /**
     * Gets the number of columns that will be printed
     *
     * @return - the number of columns
     */
    int getColumnCount();

    /**
     * Gets the {@link java.lang.String} representation of particular column
     *
     * @param column - the column index
     * @return the string value for this column
     */
    @NonNull
    String getValueAt(int column);

    /**
     * Indicates that we need to move down to the next row. The very first time this is called should
     * move to the very first available row.
     *
     * @return {@code true} if we are on a valid row, {@code false} if we've passed our last row
     */
    boolean nextRow();
}
