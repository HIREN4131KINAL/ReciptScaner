package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import co.smartreceipts.android.persistence.DatabaseHelper;

abstract class AbstractSqlTable<T> implements Table<T> {

    private final DatabaseHelper mDatabaseHelper;

    public AbstractSqlTable(@NonNull DatabaseHelper databaseHelper) {
        mDatabaseHelper = databaseHelper;
    }

    public final SQLiteDatabase getReadableDatabase() {
        return mDatabaseHelper.getReadableDatabase();
    }

    public final SQLiteDatabase getWritableDatabase() {
        return mDatabaseHelper.getWritableDatabase();
    }

}
