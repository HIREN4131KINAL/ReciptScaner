package co.smartreceipts.android.sync.drive.managers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.List;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactoryFactory;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.sync.drive.rx.DriveStreamMappings;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.model.SyncState;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DriveReceiptsManager {

    private static final String TAG = DriveReceiptsManager.class.getSimpleName();

    private final TableController<Receipt> mReceiptTableController;
    private final ReceiptsTable mReceiptsTable;
    private final DriveStreamsManager mDriveTaskManager;
    private final DriveStreamMappings mDriveStreamMappings;
    private final ReceiptBuilderFactoryFactory mReceiptBuilderFactoryFactory;
    private final Scheduler mObserveOnScheduler;
    private final Scheduler mSubscribeOnScheduler;

    public DriveReceiptsManager(@NonNull TableController<Receipt> receiptsTableController, @NonNull ReceiptsTable receiptsTable,
                                @NonNull DriveStreamsManager driveTaskManager) {
        this(receiptsTableController, receiptsTable, driveTaskManager, new DriveStreamMappings(), new ReceiptBuilderFactoryFactory(), Schedulers.io(), Schedulers.io());
    }

    public DriveReceiptsManager(@NonNull TableController<Receipt> receiptsTableController, @NonNull ReceiptsTable receiptsTable,
                                @NonNull DriveStreamsManager driveTaskManager, @NonNull DriveStreamMappings driveStreamMappings,
                                @NonNull ReceiptBuilderFactoryFactory receiptBuilderFactoryFactory, @NonNull Scheduler observeOnScheduler,
                                @NonNull Scheduler subscribeOnScheduler) {
        mReceiptTableController = Preconditions.checkNotNull(receiptsTableController);
        mReceiptsTable = Preconditions.checkNotNull(receiptsTable);
        mDriveTaskManager = Preconditions.checkNotNull(driveTaskManager);
        mDriveStreamMappings = Preconditions.checkNotNull(driveStreamMappings);
        mReceiptBuilderFactoryFactory = Preconditions.checkNotNull(receiptBuilderFactoryFactory);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    public void initialize() {
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
                            handleDelete(receipt);
                        } else {
                            Log.i(TAG, "Handling insert/update action during initialization");
                            handleInsertOrUpdate(receipt);
                        }
                    }
                });
    }

    public void handleInsertOrUpdate(@NonNull final Receipt receipt) {
        Preconditions.checkNotNull(receipt);
        Preconditions.checkArgument(!receipt.getSyncState().isSynced(SyncProvider.GoogleDrive), "Cannot sync an already synced receipt");

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
                        mReceiptTableController.update(receipt, newReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
                    }
                });
    }

    public void handleDelete(@NonNull final Receipt receipt) {
        Preconditions.checkNotNull(receipt);
        Preconditions.checkArgument(!receipt.getSyncState().isSynced(SyncProvider.GoogleDrive), "Cannot delete an already synced receipt");
        Preconditions.checkArgument(receipt.getSyncState().isMarkedForDeletion(SyncProvider.GoogleDrive), "Cannot delete a receipt that isn't marked for deletion");

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
                        mReceiptTableController.delete(newReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
                    }
                });
    }

    @NonNull
    private Observable<SyncState> onInsertOrUpdateObservable(@NonNull final Receipt receipt) {
        final SyncState oldSyncState = receipt.getSyncState();
        final File receiptFile = receipt.getFile();

        if (oldSyncState.getSyncId(SyncProvider.GoogleDrive) == null) {
            if (receiptFile != null) {
                // This case is true for INSERTS or UPDATES (in which a new file was attached)
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
