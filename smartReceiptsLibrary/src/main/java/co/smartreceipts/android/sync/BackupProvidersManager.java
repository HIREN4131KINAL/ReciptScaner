package co.smartreceipts.android.sync;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.common.base.Preconditions;

/**
 * A global manager for whatever our current backup provider may (or may not) be
 */
public class BackupProvidersManager implements BackupProvider {

    private BackupProvider mBackupProvider;

    public BackupProvidersManager() {
        this(new NoOpBackupProvider());
    }

    public BackupProvidersManager(@NonNull BackupProvider backupProvider) {
        mBackupProvider = Preconditions.checkNotNull(backupProvider);
    }

    public synchronized void setBackupProvider(@NonNull BackupProvider backupProvider) {
        Preconditions.checkNotNull(backupProvider);
        mBackupProvider = backupProvider;
    }

    @Override
    public synchronized void initialize(@Nullable FragmentActivity activity) {
        mBackupProvider.initialize(activity);
    }

    @Override
    public synchronized  void deinitialize() {
        mBackupProvider.deinitialize();
    }

    @Override
    public synchronized boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        return mBackupProvider.onActivityResult(requestCode, resultCode, data);
    }
}
