package co.smartreceipts.android.sync.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.utils.FileUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;
import wb.android.storage.StorageManager;

public class RemoteBackupsDataCache {

    private final Context mContext;
    private final BackupProvidersManager mBackupProvidersManager;
    private final DatabaseHelper mDatabaseHelper;
    private RemoteBackupsResultsCacheHeadlessFragment mHeadlessFragment;

    public RemoteBackupsDataCache(@NonNull FragmentManager fragmentManager, @NonNull Context context,
                                  @NonNull BackupProvidersManager backupProvidersManager, @NonNull DatabaseHelper databaseHelper) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mBackupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
        mDatabaseHelper = Preconditions.checkNotNull(databaseHelper);
        Preconditions.checkNotNull(fragmentManager);

        RemoteBackupsResultsCacheHeadlessFragment headlessFragment = (RemoteBackupsResultsCacheHeadlessFragment) fragmentManager.findFragmentByTag(RemoteBackupsResultsCacheHeadlessFragment.TAG);
        if (headlessFragment == null) {
            headlessFragment = new RemoteBackupsResultsCacheHeadlessFragment();
            fragmentManager.beginTransaction().add(headlessFragment, RemoteBackupsResultsCacheHeadlessFragment.TAG).commit();
        }
        mHeadlessFragment = headlessFragment;
    }

    @NonNull
    public synchronized Observable<List<RemoteBackupMetadata>> getBackups(@NonNull SyncProvider syncProvider) {
        if (mHeadlessFragment.getBackupsReplaySubjectMap == null) {
            mHeadlessFragment.getBackupsReplaySubjectMap = new HashMap<>();
        }
        ReplaySubject<List<RemoteBackupMetadata>> backupsReplaySubject = mHeadlessFragment.getBackupsReplaySubjectMap.get(syncProvider);
        if (backupsReplaySubject == null) {
            backupsReplaySubject = ReplaySubject.create();
            mBackupProvidersManager.getRemoteBackups()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(backupsReplaySubject);
            mHeadlessFragment.getBackupsReplaySubjectMap.put(syncProvider, backupsReplaySubject);
        }
        return backupsReplaySubject;
    }

    public synchronized void clearGetBackupsResults() {
        if (mHeadlessFragment.getBackupsReplaySubjectMap != null) {
            mHeadlessFragment.getBackupsReplaySubjectMap.clear();
        }
    }

    @NonNull
    public synchronized Observable<Boolean> deleteBackup(@NonNull final RemoteBackupMetadata remoteBackupMetadata) {
        if (mHeadlessFragment.deleteBackupReplaySubjectMap == null) {
            mHeadlessFragment.deleteBackupReplaySubjectMap = new HashMap<>();
        }
        ReplaySubject<Boolean> deleteReplaySubject = mHeadlessFragment.deleteBackupReplaySubjectMap.get(remoteBackupMetadata);
        if (deleteReplaySubject == null) {
            deleteReplaySubject = ReplaySubject.create();
            deleteLocalSyncDataIfNeeded(remoteBackupMetadata)
                    .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(Boolean success) {
                            if (success) {
                                return mBackupProvidersManager.deleteBackup(remoteBackupMetadata);
                            } else {
                                return Observable.just(false);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(deleteReplaySubject);
            mHeadlessFragment.deleteBackupReplaySubjectMap.put(remoteBackupMetadata, deleteReplaySubject);
        }
        return deleteReplaySubject;
    }

    @NonNull
    public synchronized Observable<Boolean> restoreBackup(@NonNull final RemoteBackupMetadata remoteBackupMetadata, final boolean overwriteExistingData) {
        if (mHeadlessFragment.restoreBackupReplaySubjectMap == null) {
            mHeadlessFragment.restoreBackupReplaySubjectMap = new HashMap<>();
        }
        ReplaySubject<Boolean> restoreBackupReplaySubject = mHeadlessFragment.restoreBackupReplaySubjectMap.get(remoteBackupMetadata);
        if (restoreBackupReplaySubject == null) {
            restoreBackupReplaySubject = ReplaySubject.create();
            mBackupProvidersManager.restoreBackup(remoteBackupMetadata, overwriteExistingData)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(restoreBackupReplaySubject);
            mHeadlessFragment.restoreBackupReplaySubjectMap.put(remoteBackupMetadata, restoreBackupReplaySubject);
        }
        return restoreBackupReplaySubject;
    }

    @NonNull
    public synchronized Observable<File> downloadBackup(@NonNull final RemoteBackupMetadata remoteBackupMetadata) {
        if (mHeadlessFragment.downloadBackupReplaySubjectMap == null) {
            mHeadlessFragment.downloadBackupReplaySubjectMap = new HashMap<>();
        }

        ReplaySubject<File> downloadBackupReplaySubjectMap = mHeadlessFragment.downloadBackupReplaySubjectMap.get(remoteBackupMetadata);
        if (downloadBackupReplaySubjectMap == null) {
            final File cacheDir = new File(mContext.getCacheDir(), FileUtils.omitIllegalCharactersFromFileName(remoteBackupMetadata.getSyncDeviceName()));
            final File cacheDirZipFile = new File(mContext.getCacheDir(), FileUtils.omitIllegalCharactersFromFileName(remoteBackupMetadata.getSyncDeviceName()) + ".zip");
            downloadBackupReplaySubjectMap = ReplaySubject.create();
            Observable.create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        if ((cacheDir.exists() || cacheDir.mkdirs()) && (!cacheDirZipFile.exists() || cacheDirZipFile.delete())) {
                            subscriber.onNext(true);
                            subscriber.onCompleted();
                        } else {
                            subscriber.onError(new IOException("Failed to create cache directory to save the images"));
                        }
                    }
                })
                .flatMap(new Func1<Boolean, Observable<List<File>>>() {
                    @Override
                    public Observable<List<File>> call(Boolean aBoolean) {
                        return mBackupProvidersManager.downloadAllData(remoteBackupMetadata, cacheDir);
                    }
                })
                .map(new Func1<List<File>, File>() {
                    @Override
                    public File call(List<File> files) {
                        return StorageManager.getInstance(mContext).zip(cacheDir);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(downloadBackupReplaySubjectMap);
            mHeadlessFragment.downloadBackupReplaySubjectMap.put(remoteBackupMetadata, downloadBackupReplaySubjectMap);
        }
        return downloadBackupReplaySubjectMap;
    }

    public synchronized void removeCachedRestoreBackupFor(@NonNull final RemoteBackupMetadata remoteBackupMetadata) {
        if (mHeadlessFragment.restoreBackupReplaySubjectMap != null) {
            mHeadlessFragment.restoreBackupReplaySubjectMap.remove(remoteBackupMetadata);
        }
        if (mHeadlessFragment.downloadBackupReplaySubjectMap != null) {
            mHeadlessFragment.downloadBackupReplaySubjectMap.remove(remoteBackupMetadata);
        }
    }

    @NonNull
    private Observable<Boolean> deleteLocalSyncDataIfNeeded(@NonNull final RemoteBackupMetadata remoteBackupMetadata) {
        if (remoteBackupMetadata.getSyncDeviceId().equals(mBackupProvidersManager.getDeviceSyncId())) {
            return mDatabaseHelper.getReceiptsTable().deleteSyncData(mBackupProvidersManager.getSyncProvider());
        } else {
            return Observable.just(true);
        }
    }

    public static final class RemoteBackupsResultsCacheHeadlessFragment extends Fragment {

        private static final String TAG = RemoteBackupsDataCache.class.getName();

        private Map<SyncProvider, ReplaySubject<List<RemoteBackupMetadata>>> getBackupsReplaySubjectMap;
        private Map<RemoteBackupMetadata, ReplaySubject<Boolean>> deleteBackupReplaySubjectMap;
        private Map<RemoteBackupMetadata, ReplaySubject<Boolean>> restoreBackupReplaySubjectMap;
        private Map<RemoteBackupMetadata, ReplaySubject<File>> downloadBackupReplaySubjectMap;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

}
