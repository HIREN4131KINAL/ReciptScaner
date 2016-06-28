package co.smartreceipts.android.persistence.database.tables.controllers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.tables.Table;
import wb.android.storage.StorageManager;

public class TripTableActionAlterations extends StubTableActionAlterations<Trip> {

    private static final String TAG = TripTableActionAlterations.class.getSimpleName();

    private final Table<Trip, String> mTripsTable;
    private final DatabaseHelper mDatabaseHelper;
    private final StorageManager mStorageManager;

    public TripTableActionAlterations(@NonNull PersistenceManager persistenceManager) {
        this(Preconditions.checkNotNull(persistenceManager).getDatabase(), Preconditions.checkNotNull(persistenceManager).getStorageManager());
    }

    public TripTableActionAlterations(@NonNull DatabaseHelper databaseHelper, @NonNull StorageManager storageManager) {
        this(Preconditions.checkNotNull(databaseHelper).getTripsTable(), databaseHelper, storageManager);
    }

    public TripTableActionAlterations(@NonNull Table<Trip, String> tripsTable, @NonNull DatabaseHelper databaseHelper, @NonNull StorageManager storageManager) {
        mTripsTable = Preconditions.checkNotNull(tripsTable);
        mDatabaseHelper = Preconditions.checkNotNull(databaseHelper);
        mStorageManager = Preconditions.checkNotNull(storageManager);
    }

    @Override
    public void postGet(@NonNull List<Trip> trips) {
        for (final Trip trip : trips) {
            mDatabaseHelper.getTripPriceAndDailyPrice(trip);
        }
    }

    @Override
    public void postInsert(@Nullable Trip trip) throws IOException {
        if (trip != null) {
            final File dir = mStorageManager.mkdir(trip.getName());
            if (dir == null) {
                Log.e(TAG, "Failed to create a trip directory... Rolling back and throwing an exception");
                mTripsTable.delete(trip).toBlocking().first();
                throw new IOException("Failed to create trip directory");
            }
        }
    }

    @Override
    public void postUpdate(@NonNull Trip oldTrip, @Nullable Trip newTrip) throws Exception {
        if (newTrip != null) {
            if (!oldTrip.getDirectory().equals(newTrip.getDirectory())) {
                final File dir = mStorageManager.rename(oldTrip.getDirectory(), newTrip.getName());
                if (dir.equals(oldTrip.getDirectory())) {
                    Log.e(TAG, "Failed to re-name the trip directory... Rolling back and throwing an exception");
                    mTripsTable.update(newTrip, oldTrip).toBlocking().first();
                    throw new IOException("Failed to create trip directory");
                }
            }
        }
    }

    @Override
    public void postDelete(boolean success, @NonNull Trip trip) {
        if (success) {
            if (!mStorageManager.deleteRecursively(trip.getDirectory())) {
                // TODO: Create clean up script
                Log.e(TAG, "Failed to fully delete the underlying data. Create a clean up script to fix this later");
            }
        }
    }
}
