package co.smartreceipts.android.sync.drive;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.common.base.Preconditions;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.sync.drive.rx.DriveStreamMappings;
import co.smartreceipts.android.sync.drive.rx.RxDriveStreams;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.Identifier;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class GoogleDriveTaskManager implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = GoogleDriveTaskManager.class.getSimpleName();

    private final RxDriveStreams mRxDriveStreams;
    private final DriveStreamMappings mDriveStreamMappings;
    private final ReceiptsTable mReceiptsTable;
    private final AtomicReference<CountDownLatch> mLatchReference;

    public GoogleDriveTaskManager(@NonNull RxDriveStreams rxDriveStreams, @NonNull ReceiptsTable receiptsTable) {
        this(rxDriveStreams, receiptsTable, new DriveStreamMappings());
    }

    public GoogleDriveTaskManager(@NonNull RxDriveStreams rxDriveStreams, @NonNull ReceiptsTable receiptsTable, @NonNull DriveStreamMappings driveStreamMappings) {
        mRxDriveStreams = Preconditions.checkNotNull(rxDriveStreams);
        mReceiptsTable = Preconditions.checkNotNull(receiptsTable);
        mDriveStreamMappings = Preconditions.checkNotNull(driveStreamMappings);
        mLatchReference = new AtomicReference<>(new CountDownLatch(1));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connection succeeded.");
        syncLocalData();
        mLatchReference.get().countDown();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended with cause " + cause);
        mLatchReference.set(new CountDownLatch(1));
    }

    @NonNull
    public Observable<Boolean> updateDatabase() {
        return Observable.just(true);
    }

    @NonNull
    public Observable<SyncState> uploadFileToDrive(@NonNull final SyncState currentSyncState, @NonNull final File file) {
        Preconditions.checkNotNull(currentSyncState);
        Preconditions.checkNotNull(file);

        return newBlockUntilConnectedObservable()
                .flatMap(new Func1<Void, Observable<DriveFolder>>() {
                    @Override
                    public Observable<DriveFolder> call(Void aVoid) {
                        return mRxDriveStreams.getSmartReceiptsFolder();
                    }
                })
                .flatMap(new Func1<DriveFolder, Observable<DriveFile>>() {
                    @Override
                    public Observable<DriveFile> call(DriveFolder driveFolder) {
                        return mRxDriveStreams.createFileInFolder(driveFolder, file);
                    }
                })
                .flatMap(new Func1<DriveFile, Observable<SyncState>>() {
                    @Override
                    public Observable<SyncState> call(DriveFile driveFile) {
                        return Observable.just(mDriveStreamMappings.postInsertSyncState(currentSyncState, driveFile));
                    }
                });
    }

    @NonNull
    public Observable<SyncState> updateDriveFile(@NonNull final SyncState currentSyncState, @NonNull final File file) {
        Preconditions.checkNotNull(currentSyncState);
        Preconditions.checkNotNull(file);

        return newBlockUntilConnectedObservable()
                .flatMap(new Func1<Void, Observable<DriveFile>>() {
                    @Override
                    public Observable<DriveFile> call(Void aVoid) {
                        final Identifier driveIdentifier = currentSyncState.getSyncId(SyncProvider.GoogleDrive);
                        if (driveIdentifier != null) {
                            return mRxDriveStreams.updateFile(driveIdentifier, file);
                        } else {
                            return Observable.error(new Exception("This sync state doesn't include a valid Drive Identifier"));
                        }
                    }
                })
                .flatMap(new Func1<DriveFile, Observable<SyncState>>() {
                    @Override
                    public Observable<SyncState> call(DriveFile driveFile) {
                        return Observable.just(mDriveStreamMappings.postUpdateSyncState(currentSyncState, driveFile));
                    }
                });
    }

    @NonNull
    public Observable<SyncState> deleteDriveFile(@NonNull final SyncState currentSyncState) {
        Preconditions.checkNotNull(currentSyncState);

        return newBlockUntilConnectedObservable()
                .flatMap(new Func1<Void, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Void aVoid) {
                        final Identifier driveIdentifier = currentSyncState.getSyncId(SyncProvider.GoogleDrive);
                        if (driveIdentifier != null) {
                            return mRxDriveStreams.deleteFile(driveIdentifier);
                        } else {
                            return Observable.error(new Exception("This sync state doesn't include a valid Drive Identifier"));
                        }
                    }
                })
                .flatMap(new Func1<Boolean, Observable<SyncState>>() {
                    @Override
                    public Observable<SyncState> call(Boolean success) {
                        if (success) {
                            return Observable.just(mDriveStreamMappings.postDeleteSyncState(currentSyncState));
                        } else {
                            return Observable.just(currentSyncState);
                        }
                    }
                });
    }

    private Observable<Void> newBlockUntilConnectedObservable() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                final CountDownLatch countDownLatch = mLatchReference.get();
                try {
                    countDownLatch.await();
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (InterruptedException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private void syncLocalData() {
        mReceiptsTable.getUnsynced(SyncProvider.GoogleDrive)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<List<Receipt>>() {
                    @Override
                    public void call(List<Receipt> receipts) {
                        for (final Receipt receipt : receipts) {
                            final SyncState syncState = receipt.getSyncState();
                            final File receiptFile = receipt.getFile();
                            final Identifier driveId = syncState.getSyncId(SyncProvider.GoogleDrive);
                            final boolean markedForDeletion = syncState.isMarkedForDeletion(SyncProvider.GoogleDrive);
                            if (driveId == null) {
                                if (receiptFile != null) {
                                    // This case is true for INSERTS or UPDATES (in which a new file was attached)
                                    Log.i(TAG, "Found receipt " + receipt.getId() + " with a non-uploaded file. Uploading");
                                    uploadFileToDrive(syncState, receiptFile); // TODO: Check if anything happens here...
                                    // TODO: Set sync == true
                                } else {
                                    Log.i(TAG, "Found receipt " + receipt.getId() + " without a file. Marking as synced for Drive");
                                    // TODO: Save silently with synced == true
                                }
                            } else {
                                if (markedForDeletion) {
                                    Log.i(TAG, "Found receipt " + receipt.getId() + " as marked for deletion. Removing");
                                    deleteDriveFile(syncState); // TODO: Check if anything happens here...
                                    // TODO: Set sync == true and full delete()
                                } else {
                                    if (receiptFile != null) {
                                        Log.i(TAG, "Found receipt " + receipt.getId() + " with a new file. Updating");
                                        updateDriveFile(syncState, receiptFile); // TODO: Check if anything happens here...
                                        // TODO: Set sync == true
                                    } else {
                                        Log.i(TAG, "Found receipt " + receipt.getId() + " with a stale file reference. Removing");
                                        deleteDriveFile(syncState); // TODO: Check if anything happens here...
                                        // TODO: Set ID to null and sync == true
                                    }
                                }
                            }
                        }
                    }
                });
    }
}
