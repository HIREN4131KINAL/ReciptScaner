package co.smartreceipts.android.sync;

import android.support.annotation.NonNull;

import co.smartreceipts.android.sync.provider.SyncProvider;

public interface BackupProviderChangeListener {

    void onProviderChanged(@NonNull SyncProvider newProvider);
}
