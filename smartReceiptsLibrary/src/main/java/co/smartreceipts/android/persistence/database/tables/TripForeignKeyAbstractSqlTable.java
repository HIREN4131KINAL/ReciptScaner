package co.smartreceipts.android.persistence.database.tables;

import android.content.ContentValues;
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
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.adapters.SelectionBackedDatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderBy;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.sync.model.Syncable;
import rx.Observable;
import rx.functions.Func0;
import wb.android.google.camera.data.Log;

/**
 * Extends the {@link AbstractColumnTable} class to provide support for an extra method, {@link #get(Trip)}. We may
 * want to generify this class further (to support other classes beside just {@link Trip} objects in the future), but
 * it'll stay hard-typed for now until this requirement arises...
 *
 * @param <ModelType> the model object that CRUD operations here should return
 * @param <PrimaryKeyType> the primary key type (e.g. Integer, String) that will be used
 */
public abstract class TripForeignKeyAbstractSqlTable<ModelType, PrimaryKeyType> extends AbstractSqlTable<ModelType, PrimaryKeyType> {

    private final String TAG = getClass().getSimpleName();
    private final HashMap<Trip, List<ModelType>> mPerTripCache = new HashMap<>();
    private final SelectionBackedDatabaseAdapter<ModelType, PrimaryKey<ModelType, PrimaryKeyType>, Trip> mSelectionBackedDatabaseAdapter;
    private final String mTripForeignKeyReferenceColumnName;
    private final String mSortingOrderColumn;

    public TripForeignKeyAbstractSqlTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull String tableName, @NonNull SelectionBackedDatabaseAdapter<ModelType, PrimaryKey<ModelType, PrimaryKeyType>, Trip> databaseAdapter,
                                          @NonNull PrimaryKey<ModelType, PrimaryKeyType> primaryKey, @NonNull String tripForeignKeyReferenceColumnName, @NonNull String sortingOrderColumn) {
        super(sqLiteOpenHelper, tableName, databaseAdapter, primaryKey, new OrderBy(sortingOrderColumn, true));
        mSelectionBackedDatabaseAdapter = databaseAdapter;
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
    public Observable<List<ModelType>> get(@NonNull Trip trip) {
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
    public synchronized Observable<List<ModelType>> get(@NonNull final Trip trip, final  boolean isDescending) {
        return Observable.defer(new Func0<Observable<List<ModelType>>>() {
            @Override
            public Observable<List<ModelType>> call() {
                return Observable.just(TripForeignKeyAbstractSqlTable.this.getBlocking(trip, isDescending));
            }
        });
    }

    @NonNull
    public synchronized List<ModelType> getBlocking(@NonNull Trip trip, boolean isDescending) {
        if (mPerTripCache.containsKey(trip)) {
            return new ArrayList<>(mPerTripCache.get(trip));
        }

        Cursor cursor = null;
        try {
            final List<ModelType> results = new ArrayList<>();
            cursor = getReadableDatabase().query(getTableName(), null, mTripForeignKeyReferenceColumnName + "= ? AND "+ COLUMN_DRIVE_MARKED_FOR_DELETION + " = ?", new String[]{ trip.getName(), Integer.toString(0) }, null, null, new OrderBy(mSortingOrderColumn, isDescending).getOrderByPredicate());
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    results.add(mSelectionBackedDatabaseAdapter.readForSelection(cursor, trip));
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
    public synchronized List<ModelType> getBlocking() {
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
                    localCache.put(trip, new ArrayList<>(Collections.singletonList(modelType)));
                }
            }
        }
        mPerTripCache.putAll(localCache);
        return results;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public synchronized ModelType insertBlocking(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final ModelType insertedItem = super.insertBlocking(modelType, databaseOperationMetadata);
        if (insertedItem != null) {
            final Trip trip = getTripFor(insertedItem);
            if (mPerTripCache.containsKey(trip)) {
                final List<ModelType> perTripResults = mPerTripCache.get(trip);
                perTripResults.add(insertedItem);
                if (insertedItem instanceof Comparable<?>) {
                    Collections.sort((List<? extends Comparable>)perTripResults);
                }
            }
        }
        return insertedItem;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public synchronized ModelType updateBlocking(@NonNull ModelType oldModelType, @NonNull ModelType newModelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final ModelType updatedItem = super.updateBlocking(oldModelType, newModelType, databaseOperationMetadata);
        if (updatedItem != null) {
            Log.d(TAG, "Successfully updated this item in our table");
            final Trip oldTrip = getTripFor(oldModelType);
            if (mPerTripCache.containsKey(oldTrip)) {
                final List<ModelType> perTripResults = mPerTripCache.get(oldTrip);
                perTripResults.remove(oldModelType);
                Log.d(TAG, "Found this item in our cache. Removing it");
            }

            boolean isMarkedForDeletion = false;
            if (updatedItem instanceof Syncable) {
                final Syncable syncable = (Syncable) newModelType;
                if (syncable.getSyncState().isMarkedForDeletion(SyncProvider.GoogleDrive)) {
                    isMarkedForDeletion = true;
                }
            }

            final Trip newTrip = getTripFor(updatedItem);
            if (!isMarkedForDeletion && mPerTripCache.containsKey(newTrip)) {
                Log.d(TAG, "This item is not marked for deletion. Adding it to our cache");
                final List<ModelType> perTripResults = mPerTripCache.get(newTrip);
                perTripResults.add(updatedItem);
                if (updatedItem instanceof Comparable<?>) {
                    Collections.sort((List<? extends Comparable>)perTripResults);
                }
            }
        }
        return updatedItem;
    }

    public synchronized void updateParentBlocking(@NonNull Trip oldTrip, @NonNull Trip newTrip) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(mTripForeignKeyReferenceColumnName, newTrip.getName());
        getWritableDatabase().update(getTableName(), contentValues, mTripForeignKeyReferenceColumnName + "= ?", new String[]{ oldTrip.getName() });
        mPerTripCache.remove(oldTrip);
    }

    @Nullable
    @Override
    public synchronized ModelType deleteBlocking(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final ModelType deleteResult = super.deleteBlocking(modelType, databaseOperationMetadata);
        if (deleteResult != null) {
            final Trip trip = getTripFor(modelType);
            if (mPerTripCache.containsKey(trip)) {
                final List<ModelType> perTripResults = mPerTripCache.get(trip);
                perTripResults.remove(modelType);
            }
        }
        return deleteResult;
    }

    public synchronized void deleteParentBlocking(@NonNull Trip trip) {
        getWritableDatabase().delete(getTableName(), mTripForeignKeyReferenceColumnName + "= ?", new String[]{ trip.getName() });
        mPerTripCache.remove(trip);
    }

    @Override
    public synchronized boolean deleteSyncDataBlocking(@NonNull SyncProvider syncProvider) {
        final boolean success = super.deleteSyncDataBlocking(syncProvider);
        if (success) {
            // Clear out our cached data, so we're not out of sync
            mPerTripCache.clear();
        }
        return success;
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
