package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import co.smartreceipts.android.persistence.DatabaseHelper;

abstract class AbstractSqlTable<T> implements Table<T> {

    private final SQLiteOpenHelper mSQLiteOpenHelper;

    public AbstractSqlTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        mSQLiteOpenHelper = sqLiteOpenHelper;
    }

    public final SQLiteDatabase getReadableDatabase() {
        return mSQLiteOpenHelper.getReadableDatabase();
    }

    public final SQLiteDatabase getWritableDatabase() {
        return mSQLiteOpenHelper.getWritableDatabase();
    }

}
