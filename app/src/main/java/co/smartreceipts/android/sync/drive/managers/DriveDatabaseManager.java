package co.smartreceipts.android.sync.drive.managers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.drive.device.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DriveDatabaseManager {

    private final Context mContext;
    private final DriveStreamsManager mDriveTaskManager;
    private final GoogleDriveSyncMetadata mGoogleDriveSyncMetadata;
    private final NetworkManager mNetworkManager;
    private final Analytics mAnalytics;
    private final Scheduler mObserveOnScheduler;
    private final Scheduler mSubscribeOnScheduler;
    private final AtomicBoolean mIsSyncInProgress = new AtomicBoolean(false);

    public DriveDatabaseManager(@NonNull Context context, @NonNull DriveStreamsManager driveTaskManager,
                                @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                                @NonNull NetworkManager networkManager, @NonNull Analytics analytics) {
        this(context, driveTaskManager, googleDriveSyncMetadata, networkManager, analytics, Schedulers.io(), Schedulers.io());
    }

    public DriveDatabaseManager(@NonNull Context context, @NonNull DriveStreamsManager driveTaskManager, @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                                @NonNull NetworkManager networkManager, @NonNull Analytics analytics, @NonNull Scheduler observeOnScheduler, @NonNull Scheduler subscribeOnScheduler) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mDriveTaskManager = Preconditions.checkNotNull(driveTaskManager);
        mGoogleDriveSyncMetadata = Preconditions.checkNotNull(googleDriveSyncMetadata);
        mNetworkManager = Preconditions.checkNotNull(networkManager);
        mAnalytics = Preconditions.checkNotNull(analytics);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    public void syncDatabase() {
        if (mNetworkManager.isNetworkAvailable()) {
            // TODO: Make sure the database is closed or inactive before performing this
            // TODO: We can trigger this off of our #close() method in DB helper
            final File filesDir = mContext.getExternalFilesDir(null);
            if (filesDir != null) {
                final File dbFile = new File(filesDir, DatabaseHelper.DATABASE_NAME);
                if (dbFile.exists()) {
                    if (!mIsSyncInProgress.getAndSet(true)) {
                        getSyncDatabaseObservable(dbFile)
                                .observeOn(mObserveOnScheduler)
                                .subscribeOn(mSubscribeOnScheduler)
                                .subscribe(new Action1<Identifier>() {
                                    @Override
                                    public void call(Identifier identifier) {
                                        Logger.info(DriveDatabaseManager.this, "Successfully synced our database");
                                        mGoogleDriveSyncMetadata.setDatabaseSyncIdentifier(identifier);
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        mIsSyncInProgress.set(false);
                                        mAnalytics.record(new ErrorEvent(DriveDatabaseManager.this, throwable));
                                        Logger.error(DriveDatabaseManager.this, "Failed to synced our database", throwable);
                                    }
                                }, new Action0() {
                                    @Override
                                    public void call() {
                                        mIsSyncInProgress.set(false);
                                    }
                                });
                    } else {
                        Logger.debug(DriveDatabaseManager.this, "A sync is already in progress. Ignoring subsequent one for now");
                    }
                } else {
                    Logger.error(DriveDatabaseManager.this, "Failed to find our main database");
                }
            } else {
                Logger.error(DriveDatabaseManager.this, "Failed to find our main database storage directory");
            }
        } else {
            Logger.error(DriveDatabaseManager.this, "Network not available to sync our database");
        }
    }

    @NonNull
    private Observable<Identifier> getSyncDatabaseObservable(@NonNull final File dbFile) {
        final Identifier driveDatabaseId = mGoogleDriveSyncMetadata.getDatabaseSyncIdentifier();
        if (driveDatabaseId != null) {
            return mDriveTaskManager.updateDriveFile(driveDatabaseId, dbFile);
        } else {
            return mDriveTaskManager.uploadFileToDrive(dbFile);
        }
    }
}
