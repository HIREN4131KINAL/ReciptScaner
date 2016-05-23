package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import co.smartreceipts.android.persistence.DatabaseHelper;

public interface Table<T> {

    String getTableName();

    void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer);

    void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer);

    void onPostCreateUpgrade();
}
