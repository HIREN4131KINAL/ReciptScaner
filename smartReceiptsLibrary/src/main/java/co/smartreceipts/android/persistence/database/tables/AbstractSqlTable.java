package co.smartreceipts.android.persistence.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.persistence.database.tables.adapters.DatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.AutoIncrementIdPrimaryKey;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;

/**
 * Abstracts out the core CRUD database operations in order to ensure that each of our core table instances
 * operate in a standard manner.
 *
 * @param <ModelType> the model object that CRUD operations here should return
 * @param <PrimaryKeyType> the primary key type (e.g. Integer, String) that is used by the primary key column
 */
abstract class AbstractSqlTable<ModelType, PrimaryKeyType> implements Table<ModelType, PrimaryKeyType> {

    private final SQLiteOpenHelper mSQLiteOpenHelper;
    private final String mTableName;

    protected final DatabaseAdapter<ModelType, PrimaryKey<ModelType, PrimaryKeyType>> mDatabaseAdapter;
    protected final PrimaryKey<ModelType, PrimaryKeyType> mPrimaryKey;

    private SQLiteDatabase initialNonRecursivelyCalledDatabase;
    private List<ModelType> mCachedResults;

    public AbstractSqlTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull String tableName, @NonNull DatabaseAdapter<ModelType, PrimaryKey<ModelType, PrimaryKeyType>> databaseAdapter,
                            @NonNull PrimaryKey<ModelType, PrimaryKeyType> primaryKey) {
        mSQLiteOpenHelper = Preconditions.checkNotNull(sqLiteOpenHelper);
        mTableName = Preconditions.checkNotNull(tableName);
        mDatabaseAdapter = Preconditions.checkNotNull(databaseAdapter);
        mPrimaryKey = Preconditions.checkNotNull(primaryKey);
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
    @NonNull
    public final String getTableName() {
        return mTableName;
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        initialNonRecursivelyCalledDatabase = db;
    }

    @Override
    public synchronized void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        initialNonRecursivelyCalledDatabase = db;
    }

    @Override
    public synchronized final void onPostCreateUpgrade() {
        // We no longer need to worry about recursive database calls
        initialNonRecursivelyCalledDatabase = null;
    }

    @Override
    @NonNull
    public synchronized List<ModelType> get() {
        if (mCachedResults != null) {
            return mCachedResults;
        }

        Cursor cursor = null;
        try {
            mCachedResults = new ArrayList<>();
            cursor = getReadableDatabase().query(getTableName(), null, null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    mCachedResults.add(mDatabaseAdapter.read(cursor));
                }
                while (cursor.moveToNext());
            }
            return new ArrayList<>(mCachedResults);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Nullable
    @Override
    public ModelType findByPrimaryKey(@NonNull PrimaryKeyType primaryKeyType) {
        // TODO: Consider using a Map/Cache/"SELECT" here to improve performance. The #get() call belong is overkill for a single item
        final List<ModelType> entries = new ArrayList<>(get());
        final int size = entries.size();
        for (int i = 0; i < size; i++) {
            final ModelType modelType = entries.get(i);
            if (mPrimaryKey.getPrimaryKeyValue(modelType).equals(primaryKeyType)) {
                return modelType;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public synchronized ModelType insert(@NonNull ModelType modelType) {
        final ContentValues values = mDatabaseAdapter.write(modelType);
        if (getWritableDatabase().insertOrThrow(getTableName(), null, values) != -1) {
            if (Integer.class.equals(mPrimaryKey.getPrimaryKeyClass())) {
                Cursor cursor = null;
                try {
                    cursor = getReadableDatabase().rawQuery("SELECT last_insert_rowid()", null);

                    final Integer id;
                    if (cursor != null && cursor.moveToFirst() && cursor.getColumnCount() > 0) {
                        id = cursor.getInt(0);
                    } else {
                        id = -1;
                    }

                    // Note: We do some quick hacks around generics here to ensure the types are consistent
                    final PrimaryKey<ModelType, PrimaryKeyType> autoIncrementPrimaryKey = (PrimaryKey<ModelType, PrimaryKeyType>) new AutoIncrementIdPrimaryKey<>((PrimaryKey<ModelType, Integer>) mPrimaryKey, id);

                    final ModelType insertedItem = mDatabaseAdapter.build(modelType, autoIncrementPrimaryKey);
                    if (mCachedResults != null) {
                        mCachedResults.add(insertedItem);
                    }
                    return insertedItem;
                } finally { // Close the cursor and db to avoid memory leaks
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else {
                // If it's not an auto-increment id, just grab whatever the definition is...
                final ModelType insertedItem = mDatabaseAdapter.build(modelType, mPrimaryKey);
                if (mCachedResults != null) {
                    mCachedResults.add(insertedItem);
                }
                return insertedItem;
            }
        } else {
            return null;
        }

    }

    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public synchronized ModelType update(@NonNull ModelType oldModelType, @NonNull ModelType newModelType) {
        if (oldModelType.equals(newModelType)) {
            return oldModelType;
        }

        final ContentValues values = mDatabaseAdapter.write(newModelType);
        final String oldPrimaryKeyValue = mPrimaryKey.getPrimaryKeyValue(oldModelType).toString();
        if (getWritableDatabase().update(getTableName(), values, mPrimaryKey.getPrimaryKeyColumn() + " = ?", new String[]{ oldPrimaryKeyValue }) > 0) {
            final ModelType updatedItem;
            if (Integer.class.equals(mPrimaryKey.getPrimaryKeyClass())) {
                // If it's an auto-increment key, ensure we're re-using the same id as the old key
                final PrimaryKey<ModelType, PrimaryKeyType> autoIncrementPrimaryKey = (PrimaryKey<ModelType, PrimaryKeyType>) new AutoIncrementIdPrimaryKey<>((PrimaryKey<ModelType, Integer>) mPrimaryKey, (Integer) mPrimaryKey.getPrimaryKeyValue(oldModelType));
                updatedItem = mDatabaseAdapter.build(newModelType, autoIncrementPrimaryKey);
            } else {
                // Otherwise, we'll use whatever the user defined...
                updatedItem = mDatabaseAdapter.build(newModelType, mPrimaryKey);
            }
            if (mCachedResults != null) {
                mCachedResults.remove(oldModelType);
                mCachedResults.add(updatedItem);
            }
            return updatedItem;
        } else {
            return null;
        }

    }

    @Override
    public synchronized boolean delete(@NonNull ModelType modelType) {
        final String primaryKeyValue = mPrimaryKey.getPrimaryKeyValue(modelType).toString();
        if (getWritableDatabase().delete(getTableName(), mPrimaryKey.getPrimaryKeyColumn() + " = ?", new String[]{ primaryKeyValue }) > 0) {
            if (mCachedResults != null) {
                mCachedResults.remove(modelType);
            }
            return true;
        } else {
            return false;
        }
    }

}
