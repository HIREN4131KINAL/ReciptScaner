package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ColumnBuilderFactory;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link co.smartreceipts.android.persistence.database.tables.AbstractColumnTable}
 * for CSVs and PDFs
 */
public final class ColumnDatabaseAdapter implements DatabaseAdapter<Column<Receipt>, PrimaryKey<Column<Receipt>, Integer>> {

    private final ColumnDefinitions<Receipt> mReceiptColumnDefinitions;
    private final String mIdColumnName;
    private final String mTypeColumnName;

    public ColumnDatabaseAdapter(@NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions, @NonNull String idColumnName, @NonNull String typeColumnName) {
        mReceiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
        mIdColumnName = Preconditions.checkNotNull(idColumnName);
        mTypeColumnName = Preconditions.checkNotNull(typeColumnName);
    }

    @NonNull
    @Override
    public Column<Receipt> read(@NonNull Cursor cursor) {
        final int idIndex = cursor.getColumnIndex(mIdColumnName);
        final int typeIndex = cursor.getColumnIndex(mTypeColumnName);

        final int id = cursor.getInt(idIndex);
        final String type = cursor.getString(typeIndex);
        return new ColumnBuilderFactory<>(mReceiptColumnDefinitions).setColumnId(id).setColumnName(type).build();
    }

    @NonNull
    @Override
    public ContentValues write(@NonNull Column<Receipt> column) {
        final ContentValues values = new ContentValues();
        values.put(mTypeColumnName, column.getName());
        return values;
    }

    @NonNull
    @Override
    public Column<Receipt> build(@NonNull Column<Receipt> column, @NonNull PrimaryKey<Column<Receipt>, Integer> primaryKey) {
        return new ColumnBuilderFactory<>(mReceiptColumnDefinitions).setColumnId(primaryKey.getPrimaryKeyValue(column)).setColumnName(column.getName()).build();
    }

}
