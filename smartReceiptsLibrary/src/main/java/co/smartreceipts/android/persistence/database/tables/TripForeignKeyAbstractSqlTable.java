package co.smartreceipts.android.persistence.database.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.tables.adapters.DatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;

/**
 * Extends the {@link AbstractColumnTable} class to provide support for an extra method, {@link #get(Trip)}. We may
 * want to generify this class further (to support other classes beside just {@link Trip} objects in the future), but
 * it'll stay hard-typed for now until this requirement arises...
 *
 * @param <ModelType> the model object that CRUD operations here should return
 * @param <PrimaryKeyColumnType> the primary key type (e.g. Integer, String) that will be used
 */
abstract class TripForeignKeyAbstractSqlTable<ModelType, PrimaryKeyColumnType> extends AbstractSqlTable<ModelType, PrimaryKeyColumnType>{

    private final HashMap<Trip, List<ModelType>> mPerTripCache = new HashMap<>();
    private final String mTripForeignKeyReferenceColumnName;
    private final String mSortingOrderColumn;

    public TripForeignKeyAbstractSqlTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull String tableName, @NonNull DatabaseAdapter<ModelType, PrimaryKey<ModelType, PrimaryKeyColumnType>> databaseAdapter,
                                          @NonNull PrimaryKey<ModelType, PrimaryKeyColumnType> primaryKey, @NonNull String tripForeignKeyReferenceColumnName, @NonNull String sortingOrderColumn) {
        super(sqLiteOpenHelper, tableName, databaseAdapter, primaryKey);
        mTripForeignKeyReferenceColumnName = Preconditions.checkNotNull(tripForeignKeyReferenceColumnName);
        mSortingOrderColumn = Preconditions.checkNotNull(sortingOrderColumn);
    }

    /**
     * Fetches all model objects with a foreign key reference to the parameter object
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     * @return all objects assigned to this foreign key in descending order
     */
    @NonNull
    public synchronized List<ModelType> get(@NonNull Trip trip) {
        return get(trip, true);
    }

    /**
     * Fetches all model objects with a foreign key reference to the parameter object
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     * @param isDescending {@code true} for descending order, {@code false} for ascending
     * @return all objects assigned to this foreign key in the desired order
     */
    @NonNull
    public synchronized List<ModelType> get(@NonNull Trip trip, boolean isDescending) {
        if (mPerTripCache.containsKey(trip)) {
            return new ArrayList<>(mPerTripCache.get(trip));
        }

        Cursor cursor = null;
        try {
            final List<ModelType> results = new ArrayList<>();
            cursor = getReadableDatabase().query(getTableName(), null, mTripForeignKeyReferenceColumnName + "= ?", new String[]{ trip.getName() }, null, null, mSortingOrderColumn + ((isDescending) ? " DESC" : " ASC"));
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    results.add(mDatabaseAdapter.read(cursor));
                }
                while (cursor.moveToNext());
            }
            mPerTripCache.put(trip, results);
            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
