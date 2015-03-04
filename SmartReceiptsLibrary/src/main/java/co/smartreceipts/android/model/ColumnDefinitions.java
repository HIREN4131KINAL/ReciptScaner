package co.smartreceipts.android.model;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Defines a simple contract for tracking the different column types that are available to users of the app
 */
public interface ColumnDefinitions<T> {

    /**
     * Gets a {@link co.smartreceipts.android.model.Column} instance for a specific definition (i.e. column
     * name). Please note that different definition names can potentially be applied to the same column).
     *
     * @param id the unique identifier for the column
     * @param definitionName the name of the new column
     * @return a new column instance or {@code null} if none can be found
     */
    Column<T> getColumn(int id, @NonNull String definitionName);

    /**
     * Gets a list of all {@link co.smartreceipts.android.model.Column} instances that are available as part
     * of this set of definitions
     *
     * @return the list of all columns
     */
    @NonNull
    List<Column<T>> getAllColumns();

    /**
     * Gets a {@link co.smartreceipts.android.model.Column} instance that can be treated as the default
     * one for this particular set of definitions when a user adds a new column
     *
     * @return a new instance of the default column
     */
    @NonNull
    Column<T> getDefaultInsertColumn();

}
