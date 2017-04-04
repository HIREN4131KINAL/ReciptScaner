package co.smartreceipts.android.persistence.database.controllers.alterations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.DistanceTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import wb.android.storage.StorageManager;

public class TripTableActionAlterations extends StubTableActionAlterations<Trip> {

    private final Table<Trip, String> mTripsTable;
    private final ReceiptsTable mReceiptsTable;
    private final DistanceTable mDistanceTable;
    private final DatabaseHelper mDatabaseHelper;
    private final StorageManager mStorageManager;

    public TripTableActionAlterations(@NonNull PersistenceManager persistenceManager) {
        this(Preconditions.checkNotNull(persistenceManager).getDatabase(), Preconditions.checkNotNull(persistenceManager).getStorageManager());
    }

    public TripTableActionAlterations(@NonNull DatabaseHelper databaseHelper, @NonNull StorageManager storageManager) {
        this(Preconditions.checkNotNull(databaseHelper).getTripsTable(), databaseHelper.getReceiptsTable(), databaseHelper.getDistanceTable(), databaseHelper, storageManager);
    }

    public TripTableActionAlterations(@NonNull Table<Trip, String> tripsTable, @NonNull ReceiptsTable receiptsTable, @NonNull DistanceTable distanceTable,
                                      @NonNull DatabaseHelper databaseHelper, @NonNull StorageManager storageManager) {
        mTripsTable = Preconditions.checkNotNull(tripsTable);
        mReceiptsTable = Preconditions.checkNotNull(receiptsTable);
        mDistanceTable = Preconditions.checkNotNull(distanceTable);
        mDatabaseHelper = Preconditions.checkNotNull(databaseHelper);
        mStorageManager = Preconditions.checkNotNull(storageManager);
    }

    @NonNull
    @Override
    public Observable<List<Trip>> postGet(@NonNull final List<Trip> trips) {
        return Observable.just(trips)
                .flatMapIterable(new Func1<List<Trip>, Iterable<Trip>>() {
                    @Override
                    public Iterable<Trip> call(List<Trip> trips) {
                        return trips;
                    }
                })
                .doOnNext(new Action1<Trip>() {
                    @Override
                    public void call(Trip trip) {
                        mDatabaseHelper.getTripPriceAndDailyPrice(trip);
                    }
                })
                .toList();
    }

    @NonNull
    @Override
    public Observable<Trip> postInsert(@Nullable final Trip postInsertTrip)  {
        if (postInsertTrip == null) {
            return Observable.error(new Exception("Post insert failed due to a null trip"));
        } else {
            return makeTripDirectory(postInsertTrip)
                    .flatMap(new Func1<File, Observable<Trip>>() {
                        @Override
                        public Observable<Trip> call(@Nullable File directory) {
                            if (directory == null) {
                                return mTripsTable.delete(postInsertTrip, new DatabaseOperationMetadata(OperationFamilyType.Rollback))
                                        .flatMap(new Func1<Trip, Observable<Trip>>() {
                                            @Override
                                            public Observable<Trip> call(Trip trip) {
                                                return Observable.error(new IOException("Failed to create a trip directory... Rolling back and throwing an exception"));
                                            }
                                        });
                            } else {
                                return Observable.just(postInsertTrip);
                            }
                        }
                    })
                    .doOnNext(new Action1<Trip>() {
                        @Override
                        public void call(@Nullable Trip trip) {
                            backUpDatabase();
                        }
                    });
        }
    }


    @NonNull
    private Observable<File> makeTripDirectory(@NonNull final Trip trip) {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                final File dir = mStorageManager.mkdir(trip.getName());
                subscriber.onNext(dir);
                subscriber.onCompleted();
            }
        });
    }

    @NonNull
    @Override
    public Observable<Trip> postUpdate(@NonNull final Trip oldTrip, @Nullable final Trip newTrip) {
        if (newTrip == null) {
            return Observable.error(new Exception("Post update failed due to a null trip"));
        } else {
            return Observable.create(new Observable.OnSubscribe<Trip>() {
                        @Override
                        public void call(Subscriber<? super Trip> subscriber) {
                            newTrip.setPrice(oldTrip.getPrice());
                            newTrip.setDailySubTotal(oldTrip.getDailySubTotal());
                            subscriber.onNext(newTrip);
                            subscriber.onCompleted();
                        }
                    })
                    .flatMap(new Func1<Trip, Observable<Trip>>() {
                        @Override
                        public Observable<Trip> call(@NonNull Trip postUpdateNewTrip) {
                            if (!oldTrip.getDirectory().equals(postUpdateNewTrip.getDirectory())) {
                                mReceiptsTable.updateParentBlocking(oldTrip, postUpdateNewTrip);
                                mDistanceTable.updateParentBlocking(oldTrip, postUpdateNewTrip);
                                final File dir = mStorageManager.rename(oldTrip.getDirectory(), postUpdateNewTrip.getName());
                                if (dir.equals(oldTrip.getDirectory())) {
                                    Logger.error(this, "Failed to re-name the trip directory... Rolling back and throwing an exception");
                                    mTripsTable.update(postUpdateNewTrip, oldTrip, new DatabaseOperationMetadata()).toBlocking().first();
                                    mReceiptsTable.updateParentBlocking(postUpdateNewTrip, oldTrip);
                                    mDistanceTable.updateParentBlocking(postUpdateNewTrip, oldTrip);
                                    return Observable.error(new IOException("Failed to create trip directory"));
                                }
                            }
                            return Observable.just(postUpdateNewTrip);
                        }
                    });
        }
    }

    @NonNull
    @Override
    public Observable<Trip> postDelete(@Nullable final Trip trip) {
        if (trip == null) {
            return Observable.error(new Exception("Post delete failed due to a null trip"));
        } else {
            return Observable.create(new Observable.OnSubscribe<Trip>() {
                @Override
                public void call(Subscriber<? super Trip> subscriber) {
                    mReceiptsTable.deleteParentBlocking(trip);
                    mDistanceTable.deleteParentBlocking(trip);
                    if (!mStorageManager.deleteRecursively(trip.getDirectory())) {
                        // TODO: Create clean up script
                        Logger.error(this, "Failed to fully delete the underlying data. Create a clean up script to fix this later");
                    }
                    subscriber.onNext(trip);
                    subscriber.onCompleted();
                }
            });
        }
    }

    /**
     * Simple utility method that takes a snapshot backup of our database after all trip "insert'
     */
    private void backUpDatabase() {
        File sdDB = mStorageManager.getFile(DateUtils.getCurrentDateAsYYYY_MM_DDString() + "_" + DatabaseHelper.DATABASE_NAME + ".bak");
        try {
            if (mStorageManager.copy(mStorageManager.getFile(DatabaseHelper.DATABASE_NAME), sdDB, true)) {
                Logger.info(this, "Backed up database file to: {}", sdDB.getName());
            } else {
                Logger.error(this, "Failed to backup database: {}", sdDB.getName());
            }
        } catch (Exception e) {
            Logger.error(this, "Failed to back up database", e);
        }
    }
}
