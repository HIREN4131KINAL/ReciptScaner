package co.smartreceipts.android.persistence.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ColumnBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.tables.columns.CSVTableColumns;
import co.smartreceipts.android.utils.ListUtils;

public class CSVTable extends AbstractSqlTable<Column<Receipt>> {

    private static final String TAG = CSVTable.class.getSimpleName();

    private final List<Column<Receipt>> mCSVColumns;
    private final ColumnDefinitions<Receipt> mReceiptColumnDefinitions;
    private SQLiteDatabase initialNonRecursivelyCalledDatabase;

    public CSVTable(@NonNull DatabaseHelper databaseHelper, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        super(databaseHelper);
        mReceiptColumnDefinitions = receiptColumnDefinitions;
        mCSVColumns = new ArrayList<>();
    }

    @Override
    public String getTableName() {
        return "csvcolumns";
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        this.createCSVTableColumns(db, customizer);
        initialNonRecursivelyCalledDatabase = db;
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        if (oldVersion <= 2) {
            this.createCSVTableColumns(db, customizer);
        }
        initialNonRecursivelyCalledDatabase = db;
    }

    @Override
    public void onPostCreateUpgrade() {
        // We no longer need to worry about recursive database calls
        initialNonRecursivelyCalledDatabase = null;
    }

    private void createCSVTableColumns(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        final String csv = "CREATE TABLE " + getTableName() + " (" +
                           CSVTableColumns.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           CSVTableColumns.COLUMN_TYPE + " TEXT" + ");";
        Log.d(TAG, csv);

        db.execSQL(csv);
        customizer.insertCSVDefaults(this);
    }

    public synchronized List<Column<Receipt>> getCSVColumns() {
        Cursor c = null;
        mCSVColumns.clear();
        try {
            final SQLiteDatabase db = this.getReadableDatabase();
            c = db.query(getTableName(), null, null, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                final int idIndex = c.getColumnIndex(CSVTableColumns.COLUMN_ID);
                final int typeIndex = c.getColumnIndex(CSVTableColumns.COLUMN_TYPE);
                do {
                    final int id = c.getInt(idIndex);
                    final String type = c.getString(typeIndex);
                    final Column<Receipt> column = new ColumnBuilderFactory<Receipt>(mReceiptColumnDefinitions).setColumnId(id).setColumnName(type).build();
                    mCSVColumns.add(column);
                }
                while (c.moveToNext());
            }
            return mCSVColumns;
        } finally { // Close the cursor and db to avoid memory leaks
            if (c != null) {
                c.close();
            }
        }
    }

    public synchronized boolean insertCSVColumn() {
        final ContentValues values = new ContentValues(1);
        final Column<Receipt> defaultColumn = mReceiptColumnDefinitions.getDefaultInsertColumn();
        values.put(CSVTableColumns.COLUMN_TYPE, defaultColumn.getName());
        Cursor c = null;
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            if (db.insertOrThrow(getTableName(), null, values) == -1) {
                return false;
            } else {
                c = db.rawQuery("SELECT last_insert_rowid()", null);
                if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
                    final int id = c.getInt(0);
                    final Column<Receipt> column = new ColumnBuilderFactory<>(mReceiptColumnDefinitions).setColumnId(id).setColumnName(defaultColumn).build();
                    mCSVColumns.add(column);
                } else {
                    return false;
                }
                return true;
            }
        } finally { // Close the cursor and db to avoid memory leaks
            if (c != null) {
                c.close();
            }
        }
    }

    public synchronized boolean insertCSVColumnNoCache(String column) {
        final ContentValues values = new ContentValues(1);
        values.put(CSVTableColumns.COLUMN_TYPE, column);
        if (initialNonRecursivelyCalledDatabase != null) {
            if (initialNonRecursivelyCalledDatabase.insertOrThrow(getTableName(), null, values) == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            final SQLiteDatabase db = this.getWritableDatabase();
            if (db.insertOrThrow(getTableName(), null, values) == -1) {
                return false;
            } else {
                return true;
            }
        }
    }

    public synchronized boolean deleteCSVColumn() {
        final SQLiteDatabase db = this.getWritableDatabase();
        final Column<Receipt> column = ListUtils.removeLast(mCSVColumns);
        if (column != null) {
            return db.delete(getTableName(), CSVTableColumns.COLUMN_ID + " = ?", new String[]{Integer.toString(column.getId())}) > 0;
        } else {
            return false;
        }
    }

    public synchronized boolean updateCSVColumn(Column<Receipt> oldColumn, Column<Receipt> newColumn) { // Note index here refers to the actual
        if (oldColumn.getName().equals(newColumn.getName())) {
            // Don't bother updating, since we've already set this column type
            return true;
        }
        final ContentValues values = new ContentValues(1);
        values.put(CSVTableColumns.COLUMN_TYPE, newColumn.getName());
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            if (db.update(getTableName(), values, CSVTableColumns.COLUMN_ID + " = ?", new String[]{Integer.toString(oldColumn.getId())}) == 0) {
                return false;
            } else {
                ListUtils.replace(mCSVColumns, oldColumn, new ColumnBuilderFactory<>(mReceiptColumnDefinitions).setColumnId(oldColumn.getId()).setColumnName(newColumn).build());
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }


}
