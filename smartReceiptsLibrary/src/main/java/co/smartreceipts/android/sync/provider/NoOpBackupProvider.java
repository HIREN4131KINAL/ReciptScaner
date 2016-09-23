package co.smartreceipts.android.sync.provider;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import co.smartreceipts.android.sync.BackupProvider;
import co.smartreceipts.android.sync.BackupProvidersManager;

/**
 * A no-op implementation of the {@link BackupProvider} contract to help us to avoid dealing with nulls
 * in our {@link BackupProvidersManager} class
 */
class NoOpBackupProvider implements BackupProvider {

    @Override
    public void initialize(@Nullable FragmentActivity activity) {

    }

    @Override
    public void deinitialize() {

    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        return false;
    }
}
