package co.smartreceipts.android.sync.drive.managers;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactoryFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.sync.drive.rx.DriveStreamMappings;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.model.SyncState;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DriveReceiptsManager {

    private static final String TAG = DriveReceiptsManager.class.getSimpleName();

    private final TableController<Receipt> mReceiptTableController;
    private final ReceiptsTable mReceiptsTable;
    private final DriveStreamsManager mDriveTaskManager;
    private final DriveDatabaseManager mDriveDatabaseManager;
    private final NetworkManager mNetworkManager;
    private final DriveStreamMappings mDriveStreamMappings;
    private final ReceiptBuilderFactoryFactory mReceiptBuilderFactoryFactory;
    private final Scheduler mObserveOnScheduler;
    private final Scheduler mSubscribeOnScheduler;
    private final AtomicBoolean mIsIntializing = new AtomicBoolean(false);

    public DriveReceiptsManager(@NonNull TableController<Receipt> receiptsTableController, @NonNull ReceiptsTable receiptsTable,
                                @NonNull DriveStreamsManager driveTaskManager, @NonNull DriveDatabaseManager driveDatabaseManager,
                                @NonNull NetworkManager networkManager) {
        this(receiptsTableController, receiptsTable, driveTaskManager, driveDatabaseManager, networkManager, new DriveStreamMappings(), new ReceiptBuilderFactoryFactory(), Schedulers.io(), Schedulers.io());
    }

    public DriveReceiptsManager(@NonNull TableController<Receipt> receiptsTableController, @NonNull ReceiptsTable receiptsTable,
                                @NonNull DriveStreamsManager driveTaskManager, @NonNull DriveDatabaseManager driveDatabaseManager,
                                @NonNull NetworkManager networkManager, @NonNull DriveStreamMappings driveStreamMappings,
                                @NonNull ReceiptBuilderFactoryFactory receiptBuilderFactoryFactory, @NonNull Scheduler observeOnScheduler,
                                @NonNull Scheduler subscribeOnScheduler) {
        mReceiptTableController = Preconditions.checkNotNull(receiptsTableController);
        mReceiptsTable = Preconditions.checkNotNull(receiptsTable);
        mDriveTaskManager = Preconditions.checkNotNull(driveTaskManager);
        mDriveDatabaseManager = Preconditions.checkNotNull(driveDatabaseManager);
        mNetworkManager = Preconditions.checkNotNull(networkManager);
        mDriveStreamMappings = Preconditions.checkNotNull(driveStreamMappings);
        mReceiptBuilderFactoryFactory = Preconditions.checkNotNull(receiptBuilderFactoryFactory);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    public synchronized void initialize() {
        if (mNetworkManager.isNetworkAvailable()) {
            if (!mIsIntializing.getAndSet(true)) {
                mReceiptsTable.getUnsynced(SyncProvider.GoogleDrive)
                        .flatMap(new Func1<List<Receipt>, Observable<Receipt>>() {
                            @Override
                            public Observable<Receipt> call(List<Receipt> receipts) {
                                return Observable.from(receipts);
                            }
                        })
                        .subscribeOn(mSubscribeOnScheduler)
                        .observeOn(mObserveOnScheduler)
                        .subscribe(new Action1<Receipt>() {
                            @Override
                            public void call(Receipt receipt) {
                                if (receipt.getSyncState().isMarkedForDeletion(SyncProvider.GoogleDrive)) {
                                    Log.i(TAG, "Handling delete action during initialization");
                                    handleDeleteInternal(receipt);
                                } else {
                                    Log.i(TAG, "Handling insert/update action during initialization");
                                    handleInsertOrUpdateInternal(receipt);
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(TAG, "Failed to fetch our unsynced receipt data", throwable);
                                mIsIntializing.set(false);
                            }
                        }, new Action0() {
                            @Override
                            public void call() {
                                mDriveDatabaseManager.syncDatabase();
                                mIsIntializing.set(false);
                            }
                        });
            }
        }
    }

    public synchronized void handleInsertOrUpdate(@NonNull final Receipt receipt) {
        if (!mIsIntializing.get()) {
            handleInsertOrUpdateInternal(receipt);
        }
    }

    @VisibleForTesting
    synchronized void handleInsertOrUpdateInternal(@NonNull final Receipt receipt) {
        Preconditions.checkNotNull(receipt);
        Preconditions.checkArgument(!receipt.getSyncState().isSynced(SyncProvider.GoogleDrive), "Cannot sync an already synced receipt");
        Preconditions.checkArgument(!receipt.getSyncState().isMarkedForDeletion(SyncProvider.GoogleDrive), "Cannot insert/update a receipt that is marked for deletion");

        if (mNetworkManager.isNetworkAvailable()) {
            onInsertOrUpdateObservable(receipt)
                    .flatMap(new Func1<SyncState, Observable<Receipt>>() {
                        @Override
                        public Observable<Receipt> call(SyncState syncState) {
                            return Observable.just(mReceiptBuilderFactoryFactory.build(receipt).setSyncState(syncState).build());
                        }
                    })
                    .observeOn(mObserveOnScheduler)
                    .subscribeOn(mSubscribeOnScheduler)
                    .subscribe(new Action1<Receipt>() {
                        @Override
                        public void call(Receipt newReceipt) {
                            Log.i(TAG, "Updating receipt " + receipt.getId() + " to reflect its sync state");
                            mReceiptTableController.update(receipt, newReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e(TAG, "Failed to handle insert/update for " + receipt.getId() + " to reflect its sync state", throwable);
                        }
                    });
        } else {
            Log.w(TAG, "No network. Skipping insert/update");
        }
    }

    public synchronized void handleDelete(@NonNull final Receipt receipt) {
        if (!mIsIntializing.get()) {
            handleDeleteInternal(receipt);
        }
    }

    @VisibleForTesting
    synchronized void handleDeleteInternal(@NonNull final Receipt receipt) {
        Preconditions.checkNotNull(receipt);
        Preconditions.checkArgument(!receipt.getSyncState().isSynced(SyncProvider.GoogleDrive), "Cannot delete an already synced receipt");
        Preconditions.checkArgument(receipt.getSyncState().isMarkedForDeletion(SyncProvider.GoogleDrive), "Cannot delete a receipt that isn't marked for deletion");

        if (mNetworkManager.isNetworkAvailable()) {
            mDriveTaskManager.deleteDriveFile(receipt.getSyncState(), true)
                    .flatMap(new Func1<SyncState, Observable<Receipt>>() {
                        @Override
                        public Observable<Receipt> call(SyncState syncState) {
                            return Observable.just(mReceiptBuilderFactoryFactory.build(receipt).setSyncState(syncState).build());
                        }
                    })
                    .observeOn(mObserveOnScheduler)
                    .subscribeOn(mSubscribeOnScheduler)
                    .subscribe(new Action1<Receipt>() {
                        @Override
                        public void call(Receipt newReceipt) {
                            Log.i(TAG, "Attempting to fully delete receipt " + newReceipt.getId() + " that is marked for deletion");
                            mReceiptTableController.delete(newReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.e(TAG, "Failed to handle delete for " + receipt.getId() + " to reflect its sync state", throwable);
                        }
                    });
        } else {
            Log.w(TAG, "No network. Skipping delete");
        }
    }

    @NonNull
    private Observable<SyncState> onInsertOrUpdateObservable(@NonNull final Receipt receipt) {
        final SyncState oldSyncState = receipt.getSyncState();
        final File receiptFile = receipt.getFile();

        if (oldSyncState.getSyncId(SyncProvider.GoogleDrive) == null) {
            if (receiptFile != null && receiptFile.exists()) {
                Log.i(TAG, "Found receipt " + receipt.getId() + " with a non-uploaded file. Uploading");
                return mDriveTaskManager.uploadFileToDrive(oldSyncState, receiptFile);
            } else {
                Log.i(TAG, "Found receipt " + receipt.getId() + " without a file. Marking as synced for Drive");
                return Observable.just(mDriveStreamMappings.postInsertSyncState(oldSyncState, null));
            }
        } else {
            if (receiptFile != null) {
                Log.i(TAG, "Found receipt " + receipt.getId() + " with a new file. Updating");
                return mDriveTaskManager.updateDriveFile(oldSyncState, receiptFile);
            } else {
                Log.i(TAG, "Found receipt " + receipt.getId() + " with a stale file reference. Removing");
                return mDriveTaskManager.deleteDriveFile(oldSyncState, false);
            }
        }
    }

}
