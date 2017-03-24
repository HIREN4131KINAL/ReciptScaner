package co.smartreceipts.android.persistence.database.tables.keys;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;

/**
 * Defines the primary key for the {@link co.smartreceipts.android.persistence.database.tables.AbstractColumnTable} for
 * CSVs and PDFs
 */
public final class ColumnPrimaryKey implements PrimaryKey<Column<Receipt>, Integer> {

    private final String mPrimaryKeyColumnName;

    public ColumnPrimaryKey(@NonNull String primaryKeyColumnName) {
        mPrimaryKeyColumnName = Preconditions.checkNotNull(primaryKeyColumnName);
    }

    @Override
    @NonNull
    public String getPrimaryKeyColumn() {
        return mPrimaryKeyColumnName;
    }

    @Override
    @NonNull
    public Class<Integer> getPrimaryKeyClass() {
        return Integer.class;
    }

    @Override
    @NonNull
    public Integer getPrimaryKeyValue(@NonNull Column<Receipt> column) {
        return column.getId();
    }
}
