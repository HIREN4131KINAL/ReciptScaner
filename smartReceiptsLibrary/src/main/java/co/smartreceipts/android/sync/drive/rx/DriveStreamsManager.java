package co.smartreceipts.android.sync.drive.rx;

import android.content.Context;
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
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.Identifier;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DriveStreamsManager implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = DriveStreamsManager.class.getSimpleName();

    private final DriveDataStreams mDriveDataStreams;
    private final DriveStreamMappings mDriveStreamMappings;
    private final ReceiptsTable mReceiptsTable;
    private final AtomicReference<CountDownLatch> mLatchReference;

    public DriveStreamsManager(@NonNull Context context, @NonNull GoogleApiClient googleApiClient, @NonNull ReceiptsTable receiptsTable) {
        this(new DriveDataStreams(context, googleApiClient), receiptsTable, new DriveStreamMappings());
    }

    public DriveStreamsManager(@NonNull DriveDataStreams driveDataStreams, @NonNull ReceiptsTable receiptsTable, @NonNull DriveStreamMappings driveStreamMappings) {
        mDriveDataStreams = Preconditions.checkNotNull(driveDataStreams);
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
                        return mDriveDataStreams.getSmartReceiptsFolder();
                    }
                })
                .flatMap(new Func1<DriveFolder, Observable<DriveFile>>() {
                    @Override
                    public Observable<DriveFile> call(DriveFolder driveFolder) {
                        return mDriveDataStreams.createFileInFolder(driveFolder, file);
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
                            return mDriveDataStreams.updateFile(driveIdentifier, file);
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
    public Observable<SyncState> deleteDriveFile(@NonNull final SyncState currentSyncState, final boolean isFullDelete) {
        Preconditions.checkNotNull(currentSyncState);

        return newBlockUntilConnectedObservable()
                .flatMap(new Func1<Void, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Void aVoid) {
                        final Identifier driveIdentifier = currentSyncState.getSyncId(SyncProvider.GoogleDrive);
                        if (driveIdentifier != null) {
                            return mDriveDataStreams.deleteFile(driveIdentifier);
                        } else {
                            return Observable.error(new Exception("This sync state doesn't include a valid Drive Identifier"));
                        }
                    }
                })
                .flatMap(new Func1<Boolean, Observable<SyncState>>() {
                    @Override
                    public Observable<SyncState> call(Boolean success) {
                        if (success) {
                            return Observable.just(mDriveStreamMappings.postDeleteSyncState(currentSyncState, isFullDelete));
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
                .flatMap(new Func1<List<Receipt>, Observable<Receipt>>() {
                    @Override
                    public Observable<Receipt> call(List<Receipt> receipts) {
                        return Observable.from(receipts);
                    }
                })
                .flatMap(new Func1<Receipt, Observable<SyncState>>() {
                    @Override
                    public Observable<SyncState> call(Receipt receipt) {
                        // TODO: JOIN RECEIPT with sync state here. zip() is no good
                        // TODO: Create drive "receipt" stream
                        final SyncState oldSyncState = receipt.getSyncState();
                        final File receiptFile = receipt.getFile();
                        final Identifier driveId = oldSyncState.getSyncId(SyncProvider.GoogleDrive);
                        final boolean markedForDeletion = oldSyncState.isMarkedForDeletion(SyncProvider.GoogleDrive);
                        if (driveId == null) {
                            if (receiptFile != null) {
                                // This case is true for INSERTS or UPDATES (in which a new file was attached)
                                Log.i(TAG, "Found receipt " + receipt.getId() + " with a non-uploaded file. Uploading");
                                return uploadFileToDrive(oldSyncState, receiptFile);
                            } else {
                                Log.i(TAG, "Found receipt " + receipt.getId() + " without a file. Marking as synced for Drive");
                                return Observable.just(mDriveStreamMappings.postInsertSyncState(oldSyncState, null));
                            }
                        } else {
                            if (markedForDeletion) {
                                Log.i(TAG, "Found receipt " + receipt.getId() + " as marked for deletion. Removing");
                                return deleteDriveFile(oldSyncState, true);
                            } else {
                                if (receiptFile != null) {
                                    Log.i(TAG, "Found receipt " + receipt.getId() + " with a new file. Updating");
                                    return updateDriveFile(oldSyncState, receiptFile);
                                } else {
                                    Log.i(TAG, "Found receipt " + receipt.getId() + " with a stale file reference. Removing");
                                    return deleteDriveFile(oldSyncState, false);
                                }
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
                /*
                .subscribe(new Action1<Receipt>() {
                    @Override
                    public void call(Receipt receipt) {
                        f
                    }
                });
                */
    }
}
