package co.smartreceipts.android.sync.widget;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.provider.SyncProvider;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

public class RemoteBackupsDataCache {

    private final BackupProvidersManager mBackupProvidersManager;
    private final DatabaseHelper mDatabaseHelper;
    private RemoteBackupsResultsCacheHeadlessFragment mHeadlessFragment;

    public RemoteBackupsDataCache(@NonNull FragmentManager fragmentManager, @NonNull BackupProvidersManager backupProvidersManager, @NonNull DatabaseHelper databaseHelper) {
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


    public static final class RemoteBackupsResultsCacheHeadlessFragment extends Fragment {

        private static final String TAG = RemoteBackupsDataCache.class.getName();

        private Map<SyncProvider, ReplaySubject<List<RemoteBackupMetadata>>> getBackupsReplaySubjectMap;
        private Map<RemoteBackupMetadata, ReplaySubject<Boolean>> deleteBackupReplaySubjectMap;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
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
}
