package co.smartreceipts.android.persistence.database.tables;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.tables.columns.PDFTableColumns;

public class PDFTable extends AbstractColumnTable {

    public PDFTable(@NonNull DatabaseHelper databaseHelper, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        super(databaseHelper, receiptColumnDefinitions);
    }

    @Override
    public String getTableName() {
        return PDFTableColumns.TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return PDFTableColumns.COLUMN_ID;
    }

    @Override
    public String getTypeColumn() {
        return PDFTableColumns.COLUMN_TYPE;
    }

    @Override
    public int getTableExistsSinceDatabaseVersion() {
        return 9;
    }

    @Override
    protected void insertDefaults(@NonNull TableDefaultsCustomizer customizer) {
        customizer.insertPDFDefaults(this);
    }
}
