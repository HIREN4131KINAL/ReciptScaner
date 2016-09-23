package co.smartreceipts.android.sync.manual;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.PersistenceManager;

public class ManualBackupTaskCache {

    private final BackupTaskCacheHeadlessFragment mHeadlessFragment;
    private final PersistenceManager mPersistenceManager;

    public ManualBackupTaskCache(@NonNull FragmentManager fragmentManager, @NonNull PersistenceManager persistenceManager) {
        Preconditions.checkNotNull(fragmentManager);
        mPersistenceManager = Preconditions.checkNotNull(persistenceManager);

        BackupTaskCacheHeadlessFragment headlessFragment = (BackupTaskCacheHeadlessFragment) fragmentManager.findFragmentByTag(BackupTaskCacheHeadlessFragment.TAG);
        if (headlessFragment == null) {
            headlessFragment = new BackupTaskCacheHeadlessFragment();
            fragmentManager.beginTransaction().add(headlessFragment, BackupTaskCacheHeadlessFragment.TAG).commit();
        }
        mHeadlessFragment = headlessFragment;
    }

    @NonNull
    public synchronized ManualBackupTask getManualBackupTask() {
        if (mHeadlessFragment.manualBackupTask == null) {
            mHeadlessFragment.manualBackupTask = new ManualBackupTask(mPersistenceManager);
        }
        return mHeadlessFragment.manualBackupTask;
    }

    public static final class BackupTaskCacheHeadlessFragment extends Fragment {

        private static final String TAG = ManualBackupTaskCache.class.getName();

        private ManualBackupTask manualBackupTask;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
