package co.smartreceipts.android.persistence.database.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.tables.adapters.DatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import rx.Observable;
import rx.functions.Func0;

/**
 * Extends the {@link AbstractColumnTable} class to provide support for an extra method, {@link #get(Trip)}. We may
 * want to generify this class further (to support other classes beside just {@link Trip} objects in the future), but
 * it'll stay hard-typed for now until this requirement arises...
 *
 * @param <ModelType> the model object that CRUD operations here should return
 * @param <PrimaryKeyType> the primary key type (e.g. Integer, String) that will be used
 */
abstract class TripForeignKeyAbstractSqlTable<ModelType, PrimaryKeyType> extends AbstractSqlTable<ModelType, PrimaryKeyType>{

    private final HashMap<Trip, List<ModelType>> mPerTripCache = new HashMap<>();
    private final String mTripForeignKeyReferenceColumnName;
    private final String mSortingOrderColumn;

    public TripForeignKeyAbstractSqlTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull String tableName, @NonNull DatabaseAdapter<ModelType, PrimaryKey<ModelType, PrimaryKeyType>> databaseAdapter,
                                          @NonNull PrimaryKey<ModelType, PrimaryKeyType> primaryKey, @NonNull String tripForeignKeyReferenceColumnName, @NonNull String sortingOrderColumn) {
        super(sqLiteOpenHelper, tableName, databaseAdapter, primaryKey);
        mTripForeignKeyReferenceColumnName = Preconditions.checkNotNull(tripForeignKeyReferenceColumnName);
        mSortingOrderColumn = Preconditions.checkNotNull(sortingOrderColumn);
    }

    /**
     * Fetches all model objects with a foreign key reference to the parameter object
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     * @return an {@link Observable} with: all objects assigned to this foreign key in descending order
     */
    @NonNull
    protected final Observable<List<ModelType>> get(@NonNull Trip trip) {
        return get(trip, true);
    }

    /**
     * Fetches all model objects with a foreign key reference to the parameter object
     *
     * @param trip the {@link Trip} parameter that should be treated as a foreign key
     * @param isDescending {@code true} for descending order, {@code false} for ascending
     * @return an {@link Observable} with: all objects assigned to this foreign key in the desired order
     */
    @NonNull
    protected synchronized Observable<List<ModelType>> get(@NonNull final Trip trip, final  boolean isDescending) {
        return Observable.defer(new Func0<Observable<List<ModelType>>>() {
            @Override
            public Observable<List<ModelType>> call() {
                return Observable.just(TripForeignKeyAbstractSqlTable.this.getBlocking(trip, isDescending));
            }
        });
    }

    @NonNull
    protected final synchronized List<ModelType> getBlocking(@NonNull Trip trip, boolean isDescending) {
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
            return new ArrayList<>(results);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @NonNull
    @Override
    protected final synchronized List<ModelType> getBlocking() {
        final List<ModelType> results = super.getBlocking();
        final HashMap<Trip, List<ModelType>> localCache = new HashMap<>();
        for (int i = 0; i < results.size(); i++) {
            final ModelType modelType = results.get(i);
            final Trip trip = getTripFor(modelType);
            if (!mPerTripCache.containsKey(trip)) {
                // Note: we only populate items here that haven't been previously added to the cache
                if (localCache.containsKey(trip)) {
                    final List<ModelType> perTripResults = localCache.get(trip);
                    perTripResults.add(modelType);
                } else {
                    localCache.put(trip, new ArrayList<ModelType>(Collections.singletonList(modelType)));
                }
            }
        }
        mPerTripCache.putAll(localCache);
        return results;
    }

    @Nullable
    @Override
    protected final synchronized ModelType insertBlocking(@NonNull ModelType modelType) {
        final ModelType insertedItem = super.insertBlocking(modelType);
        if (insertedItem != null) {
            final Trip trip = getTripFor(insertedItem);
            if (mPerTripCache.containsKey(trip)) {
                final List<ModelType> perTripResults = mPerTripCache.get(trip);
                perTripResults.add(insertedItem);
            }
        }
        return insertedItem;
    }

    @Nullable
    @Override
    protected final synchronized ModelType updateBlocking(@NonNull ModelType oldModelType, @NonNull ModelType newModelType) {
        final ModelType updatedItem = super.updateBlocking(oldModelType, newModelType);
        if (updatedItem != null) {
            final Trip oldTrip = getTripFor(oldModelType);
            if (mPerTripCache.containsKey(oldTrip)) {
                final List<ModelType> perTripResults = mPerTripCache.get(oldTrip);
                perTripResults.remove(updatedItem);
            }

            final Trip newTrip = getTripFor(updatedItem);
            if (mPerTripCache.containsKey(newTrip)) {
                final List<ModelType> perTripResults = mPerTripCache.get(newTrip);
                perTripResults.add(updatedItem);
            }
        }
        return updatedItem;
    }

    @Override
    protected final synchronized boolean deleteBlocking(@NonNull ModelType modelType) {
        final boolean deleteResult = super.deleteBlocking(modelType);
        if (deleteResult) {
            final Trip trip = getTripFor(modelType);
            if (mPerTripCache.containsKey(trip)) {
                final List<ModelType> perTripResults = mPerTripCache.get(trip);
                perTripResults.remove(modelType);
            }
        }
        return deleteResult;
    }

    /**
     * Gets the parent {@link Trip} for this {@link ModelType} instance
     *
     * @param modelType the {@link ModelType} to get the trip for
     * @return the parent {@link Trip} instance
     */
    @NonNull
    protected abstract Trip getTripFor(@NonNull ModelType modelType);
}
