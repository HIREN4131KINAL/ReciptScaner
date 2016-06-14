package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.tables.columns.PDFTableColumns;

/**
 * Stores all database operations related to the {@link Column} model object for PDF Tables
 */
public final class PDFTable extends AbstractColumnTable {

    private static final int TABLE_EXISTS_SINCE = 9;

    public PDFTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        super(sqLiteOpenHelper, PDFTableColumns.TABLE_NAME, TABLE_EXISTS_SINCE, receiptColumnDefinitions, PDFTableColumns.COLUMN_ID, PDFTableColumns.COLUMN_TYPE);
    }

    @Override
    protected void insertDefaults(@NonNull TableDefaultsCustomizer customizer) {
        customizer.insertPDFDefaults(this);
    }
}
