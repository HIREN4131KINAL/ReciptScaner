package co.smartreceipts.android.persistence.database.controllers.alterations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.DistanceTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import rx.Observable;
import rx.functions.Func0;
import wb.android.storage.StorageManager;

public class TripTableActionAlterations extends StubTableActionAlterations<Trip> {

    private static final String TAG = TripTableActionAlterations.class.getSimpleName();

    private final Table<Trip, String> mTripsTable;
    private final ReceiptsTable mReceiptsTable;
    private final DistanceTable mDistanceTable;
    private final DatabaseHelper mDatabaseHelper;
    private final StorageManager mStorageManager;
    private final Executor mExecutor;

    public TripTableActionAlterations(@NonNull PersistenceManager persistenceManager) {
        this(Preconditions.checkNotNull(persistenceManager).getDatabase(), Preconditions.checkNotNull(persistenceManager).getStorageManager());
    }

    public TripTableActionAlterations(@NonNull DatabaseHelper databaseHelper, @NonNull StorageManager storageManager) {
        this(Preconditions.checkNotNull(databaseHelper).getTripsTable(), databaseHelper.getReceiptsTable(), databaseHelper.getDistanceTable(), databaseHelper, storageManager, Executors.newSingleThreadExecutor());
    }

    public TripTableActionAlterations(@NonNull Table<Trip, String> tripsTable, @NonNull ReceiptsTable receiptsTable, @NonNull DistanceTable distanceTable,
                                      @NonNull DatabaseHelper databaseHelper, @NonNull StorageManager storageManager, @NonNull Executor executor) {
        mTripsTable = Preconditions.checkNotNull(tripsTable);
        mReceiptsTable = Preconditions.checkNotNull(receiptsTable);
        mDistanceTable = Preconditions.checkNotNull(distanceTable);
        mDatabaseHelper = Preconditions.checkNotNull(databaseHelper);
        mStorageManager = Preconditions.checkNotNull(storageManager);
        mExecutor = executor;
    }

    @Override
    public void postGet(@NonNull List<Trip> trips) {
        for (final Trip trip : trips) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    // TODO: Rethink this pricing flow, since it requires querying (and caching!) all receipts...
                    // TODO: But we need it right now to help with buggy receipt processing
                    mDatabaseHelper.getTripPriceAndDailyPrice(trip);
                }
            });
        }
    }

    @Override
    public void postInsert(@Nullable Trip trip) throws IOException {
        if (trip != null) {
            final File dir = mStorageManager.mkdir(trip.getName());
            if (dir == null) {
                Log.e(TAG, "Failed to create a trip directory... Rolling back and throwing an exception");
                mTripsTable.delete(trip, new DatabaseOperationMetadata()).toBlocking().first();
                throw new IOException("Failed to create trip directory");
            } else {
                backUpDatabase();
            }
        }
    }

    @Override
    public void postUpdate(@NonNull Trip oldTrip, @Nullable Trip newTrip) throws Exception {
        if (newTrip != null) {
            newTrip.setPrice(oldTrip.getPrice());
            newTrip.setDailySubTotal(oldTrip.getDailySubTotal());
            if (!oldTrip.getDirectory().equals(newTrip.getDirectory())) {
                mReceiptsTable.updateParentBlocking(oldTrip, newTrip);
                mDistanceTable.updateParentBlocking(oldTrip, newTrip);
                final File dir = mStorageManager.rename(oldTrip.getDirectory(), newTrip.getName());
                if (dir.equals(oldTrip.getDirectory())) {
                    Log.e(TAG, "Failed to re-name the trip directory... Rolling back and throwing an exception");
                    mTripsTable.update(newTrip, oldTrip, new DatabaseOperationMetadata()).toBlocking().first();
                    mReceiptsTable.updateParentBlocking(newTrip, oldTrip);
                    mDistanceTable.updateParentBlocking(newTrip, oldTrip);
                    throw new IOException("Failed to create trip directory");
                }
            }
        }
    }

    @Override
    public void postDelete(boolean success, @NonNull Trip trip) {
        if (success) {
            mReceiptsTable.deleteParentBlocking(trip);
            mDistanceTable.deleteParentBlocking(trip);
            if (!mStorageManager.deleteRecursively(trip.getDirectory())) {
                // TODO: Create clean up script
                Log.e(TAG, "Failed to fully delete the underlying data. Create a clean up script to fix this later");
            }
        }
    }

    /**
     * Simple utility method that takes a snapshot backup of our database after all trip "insert'
     */
    public void backUpDatabase() {
        File sdDB = mStorageManager.getFile(DateUtils.getCurrentDateAsYYYY_MM_DDString() + "_" + DatabaseHelper.DATABASE_NAME + ".bak");
        try {
            mStorageManager.copy(new File(DatabaseHelper.DATABASE_NAME), sdDB, true);
            Log.i(TAG, "Backed up database file to: " + sdDB.getName());
        } catch (Exception e) {
            Log.e(TAG, "Failed to back up database: " + e.toString());
        }
    }
}
