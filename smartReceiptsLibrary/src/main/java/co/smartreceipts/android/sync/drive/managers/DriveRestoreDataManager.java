package co.smartreceipts.android.sync.drive.managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.tables.AbstractSqlTable;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.android.sync.manual.ManualBackupTask;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;

public class DriveRestoreDataManager {

    private final Context mContext;
    private final DriveStreamsManager mDriveStreamsManager;
    private final DriveDatabaseManager mDriveDatabaseManager;
    private final DatabaseHelper mDatabaseHelper;
    private final File mStorageDirectory;

    @SuppressWarnings("ConstantConditions")
    public DriveRestoreDataManager(@NonNull Context context, @NonNull DriveStreamsManager driveStreamsManager, @NonNull DatabaseHelper databaseHelper,
                                   @NonNull DriveDatabaseManager driveDatabaseManager) {
        this(context, driveStreamsManager, databaseHelper, driveDatabaseManager, context.getExternalFilesDir(null));
    }

    public DriveRestoreDataManager(@NonNull Context context, @NonNull DriveStreamsManager driveStreamsManager, @NonNull DatabaseHelper databaseHelper,
                                   @NonNull DriveDatabaseManager driveDatabaseManager, @NonNull File storageDirectory) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mDriveStreamsManager = Preconditions.checkNotNull(driveStreamsManager);
        mDatabaseHelper = Preconditions.checkNotNull(databaseHelper);
        mDriveDatabaseManager = Preconditions.checkNotNull(driveDatabaseManager);
        mStorageDirectory = Preconditions.checkNotNull(storageDirectory);
    }

    @NonNull
    public Observable<Boolean> restoreBackup(@NonNull final RemoteBackupMetadata remoteBackupMetadata, final boolean overwriteExistingData) {
        Logger.info(this, "Initiating the restoration of a backup file for Google Drive with ID: {}", remoteBackupMetadata.getId());

        return downloadBackupMetadataImages(remoteBackupMetadata, overwriteExistingData, mStorageDirectory)
                .flatMap(new Func1<List<File>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(List<File> files) {
                        Logger.debug(this, "Performing database merge");
                        final File tempDbFile = new File(mStorageDirectory, ManualBackupTask.DATABASE_EXPORT_NAME);
                        return Observable.just(mDatabaseHelper.merge(tempDbFile.getAbsolutePath(), mContext.getPackageName(), overwriteExistingData));
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        Logger.debug(this, "Syncing database following merge operation");
                        mDriveDatabaseManager.syncDatabase();
                    }
                });
    }

    @NonNull
    public Observable<List<File>> downloadAllBackupMetadataImages(@NonNull final RemoteBackupMetadata remoteBackupMetadata, @NonNull final File downloadLocation) {
        return downloadBackupMetadataImages(remoteBackupMetadata, true, downloadLocation);
    }

    @NonNull
    public Observable<List<File>> downloadAllFilesInDriveFolder(@NonNull final RemoteBackupMetadata remoteBackupMetadata, @NonNull final File downloadLocation) {
        Preconditions.checkNotNull(remoteBackupMetadata);
        Preconditions.checkNotNull(downloadLocation);

        return mDriveStreamsManager.getDriveId(remoteBackupMetadata.getId())
                .map(new Func1<DriveId, DriveFolder>() {
                    @Override
                    public DriveFolder call(DriveId driveId) {
                        Logger.debug(DriveRestoreDataManager.this, "Converting drive id to smart receipts drive folder");
                        return driveId.asDriveFolder();
                    }
                })
                .flatMap(new Func1<DriveFolder, Observable<DriveId>>() {
                    @Override
                    public Observable<DriveId> call(DriveFolder driveFolder) {
                        return mDriveStreamsManager.getFilesInFolder(driveFolder);
                    }
                })
                .map(new Func1<DriveId, DriveFile>() {
                    @Override
                    public DriveFile call(DriveId driveId) {
                        return driveId.asDriveFile();
                    }
                })
                .flatMap(new Func1<DriveFile, Observable<File>>() {
                    @Override
                    public Observable<File> call(final DriveFile driveFile) {
                        return mDriveStreamsManager.getMetadata(driveFile)
                                .map(new Func1<Metadata, String>() {
                                    @Override
                                    public String call(Metadata metadata) {
                                        return System.currentTimeMillis() + "__" + metadata.getOriginalFilename();
                                    }
                                })
                                .flatMap(new Func1<String, Observable<File>>() {
                                    @Override
                                    public Observable<File> call(String filename) {
                                        return mDriveStreamsManager.download(driveFile, new File(downloadLocation, filename));
                                    }
                                });
                    }
                })
                .toList();
    }

    @NonNull
    private Observable<List<File>> downloadBackupMetadataImages(@NonNull final RemoteBackupMetadata remoteBackupMetadata, final boolean overwriteExistingData,
                                                                @NonNull final File downloadLocation) {
        Preconditions.checkNotNull(remoteBackupMetadata);
        Preconditions.checkNotNull(downloadLocation);

        return deletePreviousTemporaryDatabase(downloadLocation)
                .flatMap(new Func1<Boolean, Observable<DriveId>>() {
                    @Override
                    public Observable<DriveId> call(Boolean success) {
                        if (success) {
                            Logger.debug(DriveRestoreDataManager.this, "Fetching drive id");
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
                        Logger.debug(DriveRestoreDataManager.this, "Converting drive id to smart receipts drive folder");
                        return Observable.just(driveId.asDriveFolder());
                    }
                })
                .flatMap(new Func1<DriveFolder, Observable<DriveId>>() {
                    @Override
                    public Observable<DriveId> call(DriveFolder driveFolder) {
                        Logger.debug(DriveRestoreDataManager.this, "Fetching receipts database in drive for this folder");
                        return mDriveStreamsManager.getFilesInFolder(driveFolder, DatabaseHelper.DATABASE_NAME);
                    }
                })
                .take(1)
                .flatMap(new Func1<DriveId, Observable<DriveFile>>() {
                    @Override
                    public Observable<DriveFile> call(DriveId driveId) {
                        Logger.debug(DriveRestoreDataManager.this, "Converting database drive id to drive file");
                        return Observable.just(driveId.asDriveFile());
                    }
                })
                .flatMap(new Func1<DriveFile, Observable<File>>() {
                    @Override
                    public Observable<File> call(DriveFile driveFile) {
                        Logger.debug(DriveRestoreDataManager.this, "Downloading database file");
                        final File tempDbFile = new File(downloadLocation, ManualBackupTask.DATABASE_EXPORT_NAME);
                        return mDriveStreamsManager.download(driveFile, tempDbFile);
                    }
                })
                .flatMap(new Func1<File, Observable<PartialReceipt>>() {
                    @Override
                    public Observable<PartialReceipt> call(File file) {
                        Logger.debug(DriveRestoreDataManager.this, "Retrieving partial receipts from our temporary drive database");
                        return getPartialReceipts(file);
                    }
                })
                .flatMap(new Func1<PartialReceipt, Observable<PartialReceipt>>() {
                    @Override
                    public Observable<PartialReceipt> call(PartialReceipt partialReceipt) {
                        Logger.debug(DriveRestoreDataManager.this, "Creating trip folder for partial receipt: {}", partialReceipt.parentTripName);
                        return createParentFolderIfNeeded(partialReceipt, downloadLocation);
                    }
                })
                .filter(new Func1<PartialReceipt, Boolean>() {
                    @Override
                    public Boolean call(PartialReceipt partialReceipt) {
                        if (overwriteExistingData) {
                            return true;
                        } else {
                            final File receiptFile = new File(new File(downloadLocation, partialReceipt.parentTripName), partialReceipt.fileName);
                            Logger.debug(DriveRestoreDataManager.this, "Filtering out receipt? " + !receiptFile.exists());
                            return !receiptFile.exists();
                        }
                    }
                })
                .flatMap(new Func1<PartialReceipt, Observable<File>>() {
                    @Override
                    public Observable<File> call(PartialReceipt partialReceipt) {
                        Logger.debug(DriveRestoreDataManager.this, "Downloading file for partial receipt: {}", partialReceipt.driveId);
                        return downloadFileForReceipt(partialReceipt, downloadLocation);
                    }
                })
                .toList();
    }

    private Observable<Boolean> deletePreviousTemporaryDatabase(@NonNull final File inDirectory) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                final File tempDbFile = new File(inDirectory, ManualBackupTask.DATABASE_EXPORT_NAME);
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
                    }
                    subscriber.onCompleted();
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
    
    private Observable<PartialReceipt> createParentFolderIfNeeded(@NonNull final PartialReceipt partialReceipt, @NonNull final File inDirectory) {
        return Observable.create(new Observable.OnSubscribe<PartialReceipt>() {
            @Override
            public void call(Subscriber<? super PartialReceipt> subscriber) {
                final File parentTripFolder = new File(inDirectory, partialReceipt.parentTripName);
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

    private Observable<File> downloadFileForReceipt(@NonNull final PartialReceipt partialReceipt, @NonNull final File inDirectory) {
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
                        final File receiptFile = new File(new File(inDirectory, partialReceipt.parentTripName), partialReceipt.fileName);
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
