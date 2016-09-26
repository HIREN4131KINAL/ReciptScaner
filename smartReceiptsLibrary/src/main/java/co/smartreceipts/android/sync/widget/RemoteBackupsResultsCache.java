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

import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.provider.SyncProvider;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

public class RemoteBackupsResultsCache {

    private final BackupProvidersManager mBackupProvidersManager;
    private RemoteBackupsResultsCacheHeadlessFragment mHeadlessFragment;

    public RemoteBackupsResultsCache(@NonNull FragmentManager fragmentManager, @NonNull BackupProvidersManager backupProvidersManager) {
        mBackupProvidersManager = Preconditions.checkNotNull(backupProvidersManager);
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
        if (mHeadlessFragment.replaySubjectMap == null) {
            mHeadlessFragment.replaySubjectMap = new HashMap<>();
        }
        ReplaySubject<List<RemoteBackupMetadata>> backupsReplaySubject = mHeadlessFragment.replaySubjectMap.get(syncProvider);
        if (backupsReplaySubject == null) {
            backupsReplaySubject = ReplaySubject.create();
            mBackupProvidersManager.getRemoteBackups()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(backupsReplaySubject);
            mHeadlessFragment.replaySubjectMap.put(syncProvider, backupsReplaySubject);
        }
        return backupsReplaySubject;
    }



    public static final class RemoteBackupsResultsCacheHeadlessFragment extends Fragment {

        private static final String TAG = RemoteBackupsResultsCache.class.getName();

        private Map<SyncProvider, ReplaySubject<List<RemoteBackupMetadata>>> replaySubjectMap;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
