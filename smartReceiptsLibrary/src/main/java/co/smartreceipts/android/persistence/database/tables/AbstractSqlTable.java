package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import co.smartreceipts.android.persistence.DatabaseHelper;

abstract class AbstractSqlTable<T> implements Table<T> {

    private final SQLiteOpenHelper mSQLiteOpenHelper;
    private SQLiteDatabase initialNonRecursivelyCalledDatabase;

    public AbstractSqlTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        mSQLiteOpenHelper = sqLiteOpenHelper;
    }

    public final SQLiteDatabase getReadableDatabase() {
        if (initialNonRecursivelyCalledDatabase == null) {
            return mSQLiteOpenHelper.getReadableDatabase();
        } else {
            return initialNonRecursivelyCalledDatabase;
        }
    }

    public final SQLiteDatabase getWritableDatabase() {
        if (initialNonRecursivelyCalledDatabase == null) {
            return mSQLiteOpenHelper.getWritableDatabase();
        } else {
            return initialNonRecursivelyCalledDatabase;
        }
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        initialNonRecursivelyCalledDatabase = db;
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        initialNonRecursivelyCalledDatabase = db;
    }

    @Override
    public final void onPostCreateUpgrade() {
        // We no longer need to worry about recursive database calls
        initialNonRecursivelyCalledDatabase = null;
    }

}
