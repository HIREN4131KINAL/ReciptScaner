package co.smartreceipts.android.persistence.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ColumnBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.tables.columns.CSVTableColumns;
import co.smartreceipts.android.persistence.database.tables.columns.PDFTableColumns;
import co.smartreceipts.android.utils.ListUtils;

abstract class AbstractColumnTable extends AbstractSqlTable<Column<Receipt>> {

    private static final String TAG = AbstractColumnTable.class.getSimpleName();

    private final List<Column<Receipt>> mCachedColumns;
    private final ColumnDefinitions<Receipt> mReceiptColumnDefinitions;

    public AbstractColumnTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        super(sqLiteOpenHelper);
        mReceiptColumnDefinitions = receiptColumnDefinitions;
        mCachedColumns = new ArrayList<>();
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        this.createColumnsTable(db, customizer);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);
        if (oldVersion <= getTableExistsSinceDatabaseVersion()) {
            this.createColumnsTable(db, customizer);
        }
    }

    private void createColumnsTable(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        final String columnsTable = "CREATE TABLE " + getTableName() + " (" +
                                    getIdColumn() + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    getTypeColumn() + " TEXT" + ");";
        Log.d(TAG, columnsTable);

        db.execSQL(columnsTable);
        insertDefaults(customizer);
    }

    @NonNull
    public synchronized List<Column<Receipt>> get() {
        Cursor c = null;
        mCachedColumns.clear();
        try {
            final SQLiteDatabase db = this.getReadableDatabase();
            c = db.query(getTableName(), null, null, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                final int idIndex = c.getColumnIndex(getIdColumn());
                final int typeIndex = c.getColumnIndex(getTypeColumn());
                do {
                    final int id = c.getInt(idIndex);
                    final String type = c.getString(typeIndex);
                    final Column<Receipt> column = new ColumnBuilderFactory<>(mReceiptColumnDefinitions).setColumnId(id).setColumnName(type).build();
                    mCachedColumns.add(column);
                }
                while (c.moveToNext());
            }
            return new ArrayList<>(mCachedColumns);
        } finally { // Close the cursor and db to avoid memory leaks
            if (c != null) {
                c.close();
            }
        }
    }

    @Nullable
    public synchronized Column<Receipt> insertDefaultColumn() {
        return insert(mReceiptColumnDefinitions.getDefaultInsertColumn());
    }

    @Nullable
    public synchronized Column<Receipt> insert(@NonNull Column<Receipt> column) {
        final ContentValues values = new ContentValues(1);
        values.put(getTypeColumn(), column.getName());
        Cursor c = null;
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            if (db.insertOrThrow(getTableName(), null, values) == -1) {
                return null;
            } else {
                c = db.rawQuery("SELECT last_insert_rowid()", null);
                if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
                    final int id = c.getInt(0);
                    final Column<Receipt> newColumn = new ColumnBuilderFactory<>(mReceiptColumnDefinitions).setColumnId(id).setColumnName(column).build();
                    if (mCachedColumns != null) {
                        mCachedColumns.add(newColumn);
                    }
                    return newColumn; //TODO: Update UTs to confirm this
                } else {
                    return null;
                }
            }
        } finally { // Close the cursor and db to avoid memory leaks
            if (c != null) {
                c.close();
            }
        }
    }

    public synchronized boolean deleteLast() {
        final List<Column<Receipt>> columns = get();
        final Column<Receipt> lastColumn = ListUtils.removeLast(columns);
        if (lastColumn != null) {
            return delete(lastColumn);
        } else {
            return false;
        }
    }

    public synchronized boolean delete(@NonNull Column<Receipt> column) {
        if (getWritableDatabase().delete(getTableName(), getIdColumn() + " = ?", new String[]{Integer.toString(column.getId())}) > 0) {
            mCachedColumns.remove(column);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    public synchronized Column<Receipt> update(@NonNull Column<Receipt> oldColumn, @NonNull Column<Receipt> newColumn) {
        if (oldColumn.getName().equals(newColumn.getName())) {
            // Don't bother updating, since we've already set this column type
            return oldColumn;
        }
        final ContentValues values = new ContentValues(1);
        values.put(getTypeColumn(), newColumn.getName());
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            if (db.update(getTableName(), values, getIdColumn() + " = ?", new String[]{Integer.toString(oldColumn.getId())}) == 0) {
                return null;
            } else {
                final Column<Receipt> newColumnWithId = new ColumnBuilderFactory<>(mReceiptColumnDefinitions).setColumnId(oldColumn.getId()).setColumnName(newColumn).build();
                ListUtils.replace(mCachedColumns, oldColumn, newColumnWithId);
                return newColumnWithId;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * @return the primary key "id" column name (e.g. {@link CSVTableColumns#COLUMN_ID} or {@link PDFTableColumns#COLUMN_ID})
     */
    protected abstract String getIdColumn();

    /**
     * @return the column name for the "type" column (e.g. {@link CSVTableColumns#COLUMN_TYPE} or {@link PDFTableColumns#COLUMN_TYPE})
     */
    protected abstract String getTypeColumn();

    /**
     * @return the database version since which this table exists
     */
    protected abstract int getTableExistsSinceDatabaseVersion();

    /**
     * Passes alongs a call to insert our "table" defaults to the appropriate sub implementation
     *
     * @param customizer the {@link co.smartreceipts.android.persistence.DatabaseHelper.TableDefaultsCustomizer} implementation
     */
    protected abstract void insertDefaults(@NonNull TableDefaultsCustomizer customizer);

}
