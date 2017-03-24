package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.TripsTable;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;
import wb.android.storage.StorageManager;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link co.smartreceipts.android.persistence.database.tables.TripsTable}
 */
public final class TripDatabaseAdapter implements DatabaseAdapter<Trip, PrimaryKey<Trip, String>> {

    private final StorageManager mStorageManager;
    private final UserPreferenceManager mPreferences;
    private final SyncStateAdapter mSyncStateAdapter;

//    public TripDatabaseAdapter(@NonNull PersistenceManager persistenceManager) {
//        this(persistenceManager.getStorageManager(), persistenceManager.getPreferenceManager(), new SyncStateAdapter());
//    }

    public TripDatabaseAdapter(@NonNull StorageManager storageManager, @NonNull UserPreferenceManager preferences, @NonNull SyncStateAdapter syncStateAdapter) {
        mStorageManager = Preconditions.checkNotNull(storageManager);
        mPreferences = Preconditions.checkNotNull(preferences);
        mSyncStateAdapter = Preconditions.checkNotNull(syncStateAdapter);
    }

    @Override
    @NonNull
    public Trip read(@NonNull Cursor cursor) {
        final int nameIndex = cursor.getColumnIndex(TripsTable.COLUMN_NAME);
        final int fromIndex = cursor.getColumnIndex(TripsTable.COLUMN_FROM);
        final int toIndex = cursor.getColumnIndex(TripsTable.COLUMN_TO);
        final int fromTimeZoneIndex = cursor.getColumnIndex(TripsTable.COLUMN_FROM_TIMEZONE);
        final int toTimeZoneIndex = cursor.getColumnIndex(TripsTable.COLUMN_TO_TIMEZONE);
        final int commentIndex = cursor.getColumnIndex(TripsTable.COLUMN_COMMENT);
        final int costCenterIndex = cursor.getColumnIndex(TripsTable.COLUMN_COST_CENTER);
        final int defaultCurrencyIndex = cursor.getColumnIndex(TripsTable.COLUMN_DEFAULT_CURRENCY);

        final String name = cursor.getString(nameIndex);
        final long from = cursor.getLong(fromIndex);
        final long to = cursor.getLong(toIndex);
        final String fromTimeZone = cursor.getString(fromTimeZoneIndex);
        final String toTimeZone = cursor.getString(toTimeZoneIndex);
        final String comment = cursor.getString(commentIndex);
        final String costCenter = cursor.getString(costCenterIndex);
        final String defaultCurrency = cursor.getString(defaultCurrencyIndex);
        final SyncState syncState = mSyncStateAdapter.read(cursor);

        return new TripBuilderFactory().setDirectory(mStorageManager.mkdir(name))
                .setStartDate(from)
                .setEndDate(to)
                .setStartTimeZone(fromTimeZone)
                .setEndTimeZone(toTimeZone)
                .setComment(comment)
                .setCostCenter(costCenter)
                .setDefaultCurrency(defaultCurrency, mPreferences.get(UserPreference.General.DefaultCurrency))
                .setSourceAsCache()
                .setSyncState(syncState)
                .build();
    }

    @Override
    @NonNull
    public ContentValues write(@NonNull Trip trip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final ContentValues values = new ContentValues();
        values.put(TripsTable.COLUMN_NAME, trip.getName());
        values.put(TripsTable.COLUMN_FROM, trip.getStartDate().getTime());
        values.put(TripsTable.COLUMN_TO, trip.getEndDate().getTime());
        values.put(TripsTable.COLUMN_FROM_TIMEZONE, trip.getStartTimeZone().getID());
        values.put(TripsTable.COLUMN_TO_TIMEZONE, trip.getEndTimeZone().getID());
        values.put(TripsTable.COLUMN_COMMENT, trip.getComment());
        values.put(TripsTable.COLUMN_COST_CENTER, trip.getCostCenter());
        values.put(TripsTable.COLUMN_DEFAULT_CURRENCY, trip.getDefaultCurrencyCode());
        if (databaseOperationMetadata.getOperationFamilyType() == OperationFamilyType.Sync) {
            values.putAll(mSyncStateAdapter.write(trip.getSyncState()));
        } else {
            values.putAll(mSyncStateAdapter.writeUnsynced(trip.getSyncState()));
        }
        return values;
    }

    @Override
    @NonNull
    public Trip build(@NonNull Trip trip, @NonNull PrimaryKey<Trip, String> primaryKey, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        return new TripBuilderFactory(trip).setDirectory(mStorageManager.getFile(primaryKey.getPrimaryKeyValue(trip))).setSyncState(mSyncStateAdapter.get(trip.getSyncState(), databaseOperationMetadata)).build();
    }

}
