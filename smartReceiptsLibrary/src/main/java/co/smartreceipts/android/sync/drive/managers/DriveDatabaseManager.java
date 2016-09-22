package co.smartreceipts.android.sync.drive.managers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.File;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.drive.device.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.model.impl.Identifier;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DriveDatabaseManager {

    private static final String TAG = DriveDatabaseManager.class.getSimpleName();

    private final Context mContext;
    private final DriveStreamsManager mDriveTaskManager;
    private final GoogleDriveSyncMetadata mGoogleDriveSyncMetadata;
    private final Scheduler mObserveOnScheduler;
    private final Scheduler mSubscribeOnScheduler;

    public DriveDatabaseManager(@NonNull Context context, @NonNull DriveStreamsManager driveTaskManager) {
        this(context, driveTaskManager, new GoogleDriveSyncMetadata(context), Schedulers.io(), Schedulers.io());
    }

    public DriveDatabaseManager(@NonNull Context context, @NonNull DriveStreamsManager driveTaskManager, @NonNull GoogleDriveSyncMetadata googleDriveSyncMetadata,
                                @NonNull Scheduler observeOnScheduler, @NonNull Scheduler subscribeOnScheduler) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mDriveTaskManager = Preconditions.checkNotNull(driveTaskManager);
        mGoogleDriveSyncMetadata = Preconditions.checkNotNull(googleDriveSyncMetadata);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    public void syncDatabase() {
        // TODO: Make sure the database is closed or inactive before performing this
        // TODO: We can trigger this off of our #close() method in DB helper
        final File filesDir = mContext.getExternalFilesDir(null);
        if (filesDir != null) {
            final File dbFile = new File(filesDir, DatabaseHelper.DATABASE_NAME);
            if (dbFile.exists()) {
                getSyncDatabaseObservable(dbFile)
                        .observeOn(mObserveOnScheduler)
                        .subscribeOn(mSubscribeOnScheduler)
                        .subscribe(new Action1<Identifier>() {
                            @Override
                            public void call(Identifier identifier) {
                                Log.i(TAG, "Successfully synced our database");
                                mGoogleDriveSyncMetadata.setDatabaseSyncIdentifier(identifier);
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(TAG, "Failed to synced our database", throwable);
                            }
                        });
            } else {
                Log.e(TAG, "Failed to find our main database");
            }
        } else {
            Log.e(TAG, "Failed to find our main database storage directory");
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
