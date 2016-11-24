package co.smartreceipts.android.sync.manual;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;

public class ManualBackupTask {

    public static final String DATABASE_EXPORT_NAME = "receipts_backup.db";

    private static final String TAG = ManualBackupTask.class.getSimpleName();
    private static final String EXPORT_FILENAME = DateUtils.getCurrentDateAsYYYY_MM_DDString() + "_SmartReceipts.smr";
    private static final String DATABASE_JOURNAL = "receipts.db-journal";

    private final PersistenceManager mPersistenceManager;
    private final Scheduler mObserveOnScheduler;
    private final Scheduler mSubscribeOnScheduler;
    private ReplaySubject<Uri> mBackupBehaviorSubject;

    ManualBackupTask(@NonNull PersistenceManager persistenceManager) {
        this(persistenceManager, Schedulers.io(), Schedulers.io());
    }

    ManualBackupTask(@NonNull PersistenceManager persistenceManager, @NonNull Scheduler observeOnScheduler, @NonNull Scheduler subscribeOnScheduler) {
        mPersistenceManager = Preconditions.checkNotNull(persistenceManager);
        mObserveOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        mSubscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    @NonNull
    public synchronized ReplaySubject<Uri> backupData() {
        if (mBackupBehaviorSubject == null) {
            mBackupBehaviorSubject = ReplaySubject.create();
            backupDataToObservable()
                    .observeOn(mObserveOnScheduler)
                    .subscribeOn(mSubscribeOnScheduler)
                    .subscribe(mBackupBehaviorSubject);
        }
        return mBackupBehaviorSubject;
    }

    @NonNull
    private Observable<Uri> backupDataToObservable() {
        return Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(Subscriber<? super Uri> subscriber) {
                try {
                    final StorageManager external = mPersistenceManager.getExternalStorageManager();
                    final StorageManager internal = mPersistenceManager.getInternalStorageManager();
                    external.delete(external.getFile(EXPORT_FILENAME)); //Remove old export

                    external.copy(external.getFile(DatabaseHelper.DATABASE_NAME), external.getFile(DATABASE_EXPORT_NAME), true);
                    final File prefs = internal.getFile(internal.getRoot().getParentFile(), "shared_prefs");

                    //Preferences File
                    if (prefs != null && prefs.exists()) {
                        File sdPrefs = external.getFile("shared_prefs");
                        Logger.debug(ManualBackupTask.this,
                                "Copying the prefs file from: {} to {}", prefs.getAbsolutePath(), sdPrefs.getAbsolutePath());
                        try {
                            external.copy(prefs, sdPrefs, true);
                        } catch (IOException e) {
                            Logger.error(this, e);
                        }
                    }

                    //Internal Files
                    final File[] internalFiles = internal.listFilesAndDirectories();
                    if (internalFiles != null && internalFiles.length > 0) {
                        Logger.debug(ManualBackupTask.this, "Copying {} files/directories to the SD Card.", internalFiles.length);
                        final File internalOnSD = external.mkdir("Internal");
                        internal.copy(internalOnSD, true);
                    }

                    //Finish
                    File zip = external.zipBuffered(8192, new BackupFileFilter());
                    zip = external.rename(zip, EXPORT_FILENAME);
                    subscriber.onNext(Uri.fromFile(zip));
                    subscriber.onCompleted();
                } catch (IOException | SDCardStateException e) {
                    Logger.error(this, e);
                    subscriber.onError(e);
                }
            }
        });
    }

    private static final class BackupFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            return !file.getName().equalsIgnoreCase(DatabaseHelper.DATABASE_NAME) &&
                    !file.getName().equalsIgnoreCase(DATABASE_JOURNAL) &&
                    !file.getName().endsWith(".smr"); //Ignore previous backups
        }
    }
}
