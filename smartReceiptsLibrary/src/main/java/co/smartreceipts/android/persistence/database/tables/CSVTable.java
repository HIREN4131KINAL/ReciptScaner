package co.smartreceipts.android.persistence.database.tables;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.tables.columns.CSVTableColumns;

public class CSVTable extends AbstractColumnTable {

    public CSVTable(@NonNull DatabaseHelper databaseHelper, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        super(databaseHelper, receiptColumnDefinitions);
    }

    @Override
    public String getTableName() {
        return CSVTableColumns.TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return CSVTableColumns.COLUMN_ID;
    }

    @Override
    public String getTypeColumn() {
        return CSVTableColumns.COLUMN_TYPE;
    }

    @Override
    public int getTableExistsSinceDatabaseVersion() {
        return 2;
    }

    @Override
    protected void insertDefaults(@NonNull TableDefaultsCustomizer customizer) {
        customizer.insertCSVDefaults(this);
    }
}
