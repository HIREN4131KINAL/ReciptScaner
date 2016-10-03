package co.smartreceipts.android.sync.drive.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.tables.AbstractSqlTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.manual.ManualBackupTask;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.model.impl.Identifier;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class DriveRestoreDataManager {

    private static final String TAG = DriveRestoreDataManager.class.getSimpleName();

    private final Context mContext;
    private final DriveStreamsManager mDriveStreamsManager;

    public DriveRestoreDataManager(@NonNull Context context, @NonNull DriveStreamsManager driveStreamsManager) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mDriveStreamsManager = Preconditions.checkNotNull(driveStreamsManager);
    }

    @NonNull
    public Observable<Boolean> restoreBackup(@NonNull final RemoteBackupMetadata remoteBackupMetadata, final boolean overwriteExistingData) {
        Log.i(TAG, "Initiating the restoration of a backup file for Google Drive with ID: " + remoteBackupMetadata.getId());

        deletePreviousTemporaryDatabase()
                .flatMap(new Func1<Boolean, Observable<DriveId>>() {
                    @Override
                    public Observable<DriveId> call(Boolean success) {
                        if (success) {
                            Log.d(TAG, "Fetching drive id");
                            return mDriveStreamsManager.getDriveId(remoteBackupMetadata.getId());
                        } else {
                            return Observable.just(null);
                        }
                    }
                })
                .filter(new Func1<DriveId, Boolean>() {
                    @Override
                    public Boolean call(DriveId driveId) {
                        return driveId != null;
                    }
                })
                .flatMap(new Func1<DriveId, Observable<DriveFolder>>() {
                    @Override
                    public Observable<DriveFolder> call(DriveId driveId) {
                        Log.d(TAG, "Converting drive id to smart receipts drive folder");
                        return Observable.just(driveId.asDriveFolder());
                    }
                })
                .flatMap(new Func1<DriveFolder, Observable<DriveId>>() {
                    @Override
                    public Observable<DriveId> call(DriveFolder driveFolder) {
                        Log.d(TAG, "Fetching receipts database in drive");
                        return mDriveStreamsManager.getFilesInFolder(driveFolder, ManualBackupTask.DATABASE_EXPORT_NAME);
                    }
                })
                .take(1)
                .flatMap(new Func1<DriveId, Observable<DriveFile>>() {
                    @Override
                    public Observable<DriveFile> call(DriveId driveId) {
                        Log.d(TAG, "Converting database drive id to drive file");
                        return Observable.just(driveId.asDriveFile());
                    }
                })
                .flatMap(new Func1<DriveFile, Observable<File>>() {
                    @Override
                    public Observable<File> call(DriveFile driveFile) {
                        Log.d(TAG, "Downloading database file");
                        final File tempDbFile = new File(mContext.getExternalFilesDir(null), ManualBackupTask.DATABASE_EXPORT_NAME);
                        return mDriveStreamsManager.download(driveFile, tempDbFile);
                    }
                })
                .flatMap(new Func1<File, Observable<PartialReceipt>>() {
                    @Override
                    public Observable<PartialReceipt> call(File file) {
                        Log.d(TAG, "Retrieving partial receipts from our temporary drive database");
                        return getPartialReceipts(file);
                    }
                })
                .flatMap(new Func1<PartialReceipt, Observable<PartialReceipt>>() {
                    @Override
                    public Observable<PartialReceipt> call(PartialReceipt partialReceipt) {
                        Log.d(TAG, "Creating trip folder for partial receipt: " + partialReceipt.parentTripName);
                        return createParentFolderIfNeeded(partialReceipt);
                    }
                })
                .filter(new Func1<PartialReceipt, Boolean>() {
                    @Override
                    public Boolean call(PartialReceipt partialReceipt) {
                        if (overwriteExistingData) {
                            return true;
                        } else {
                            final File receiptFile = new File(new File(mContext.getExternalFilesDir(null), partialReceipt.parentTripName), partialReceipt.fileName);
                            return !receiptFile.exists();
                        }
                    }
                })
                .flatMap(new Func1<PartialReceipt, Observable<File>>() {
                    @Override
                    public Observable<File> call(PartialReceipt partialReceipt) {
                        return downloadFileForReceipt(partialReceipt);
                    }
                });

        return Observable.just(false);
    }

    private Observable<Boolean> deletePreviousTemporaryDatabase() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                final File filesDir = mContext.getExternalFilesDir(null);
                if (filesDir != null) {
                    final File tempDbFile = new File(filesDir, ManualBackupTask.DATABASE_EXPORT_NAME);
                    if (tempDbFile.exists()) {
                        if (tempDbFile.delete()) {
                            subscriber.onNext(true);
                            subscriber.onCompleted();
                        } else {
                            subscriber.onError(new IOException("Failed to delete our temporary database file"));
                        }
                    } else {
                        subscriber.onNext(true);
                        subscriber.onCompleted();
                    }
                } else {
                    subscriber.onError(new IOException("Failed to connect to the external files dir"));
                }
            }
        });
    }

    private Observable<PartialReceipt> getPartialReceipts(@NonNull final File temporaryDatabaseFile) {
        Preconditions.checkNotNull(temporaryDatabaseFile);

        return Observable.create(new Observable.OnSubscribe<PartialReceipt>() {
            @Override
            public void call(Subscriber<? super PartialReceipt> subscriber) {
                SQLiteDatabase importDb = null;
                Cursor cursor = null;
                try {
                    importDb = SQLiteDatabase.openDatabase(temporaryDatabaseFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
                    final String[] selection = new String[] { AbstractSqlTable.COLUMN_DRIVE_SYNC_ID, ReceiptsTable.COLUMN_PARENT, ReceiptsTable.COLUMN_PATH };
                    cursor = importDb.query(ReceiptsTable.TABLE_NAME, selection, AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " IS NOT NULL AND " + ReceiptsTable.COLUMN_PATH + " IS NOT NULL AND " + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " = ?", new String[] { Integer.toString(0) }, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        final int driveIdIndex = cursor.getColumnIndex(AbstractSqlTable.COLUMN_DRIVE_SYNC_ID);
                        final int parentIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
                        final int pathIndex = cursor.getColumnIndex(ReceiptsTable.COLUMN_PATH);
                        do {
                            final String driveId = cursor.getString(driveIdIndex);
                            final String parent = cursor.getString(parentIndex);
                            final String path = cursor.getString(pathIndex);
                            if (driveId != null && parent != null && !TextUtils.isEmpty(path) && !DatabaseHelper.NO_DATA.equals(path)) {
                                subscriber.onNext(new PartialReceipt(driveId, parent, path));
                            }
                        }
                        while (cursor.moveToNext());
                        subscriber.onCompleted();
                    }
                } finally {
                    if (importDb != null) {
                        importDb.close();
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }
    
    private Observable<PartialReceipt> createParentFolderIfNeeded(@NonNull final PartialReceipt partialReceipt) {
        return Observable.create(new Observable.OnSubscribe<PartialReceipt>() {
            @Override
            public void call(Subscriber<? super PartialReceipt> subscriber) {
                final File parentTripFolder = new File(mContext.getExternalFilesDir(null), partialReceipt.parentTripName);
                if (!parentTripFolder.exists()) {
                    if (parentTripFolder.mkdir()) {
                        subscriber.onNext(partialReceipt);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new IOException("Failed to create the parent directory for this receipt"));
                    }
                } else {
                    subscriber.onNext(partialReceipt);
                    subscriber.onCompleted();
                }
            }
        });
    }

    private Observable<File> downloadFileForReceipt(@NonNull final PartialReceipt partialReceipt) {
        return mDriveStreamsManager.getDriveId(partialReceipt.driveId)
                .flatMap(new Func1<DriveId, Observable<DriveFile>>() {
                    @Override
                    public Observable<DriveFile> call(DriveId driveId) {
                        return Observable.just(driveId.asDriveFile());
                    }
                })
                .flatMap(new Func1<DriveFile, Observable<File>>() {
                    @Override
                    public Observable<File> call(DriveFile driveFile) {
                        final File receiptFile = new File(new File(mContext.getExternalFilesDir(null), partialReceipt.parentTripName), partialReceipt.fileName);
                        return mDriveStreamsManager.download(driveFile, receiptFile);
                    }
                });
    }

    /**
     * A subset of receipt metadata so we don't need to full new as many objects as normally required,
     * since this will have a lot of extra memory overhead
     */
    private static final class PartialReceipt {
        private final Identifier driveId;
        private final String parentTripName;
        private final String fileName;

        public PartialReceipt(@NonNull String driveId, @NonNull String parentTripName, @NonNull String fileName) {
            this.driveId = new Identifier(driveId);
            this.parentTripName = Preconditions.checkNotNull(parentTripName);
            this.fileName = Preconditions.checkNotNull(fileName);
        }
    }
}
