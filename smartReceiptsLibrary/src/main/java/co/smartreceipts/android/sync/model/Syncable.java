package co.smartreceipts.android.sync.model;

import android.support.annotation.NonNull;

/**
 * Marks a particular model object as capable of being synced with a remote server environment
 */
public interface Syncable {

    /**
     * @return the current {@link SyncState} associated with this item
     */
    @NonNull
    SyncState getSyncState();
}
