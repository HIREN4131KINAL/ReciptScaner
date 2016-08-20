package co.smartreceipts.android.sync.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.sql.Date;

import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.sync.model.impl.IdentifierMap;
import co.smartreceipts.android.sync.model.impl.MarkedForDeletionMap;
import co.smartreceipts.android.sync.model.impl.Identifier;

public interface SyncState extends Parcelable {

    /**
     * Gets the unique id associated with the "cloud" version of this object
     *
     * @param provider the {@link SyncProvider} for this identifier
     * @return the {@link Identifier} instance or {@code null} if this is an unknown provider
     */
    @Nullable
    Identifier getSyncId(@NonNull SyncProvider provider);

    /**
     * @return the {@link IdentifierMap} associated with the current sync state
     */
    @Nullable
    IdentifierMap getIdentifierMap();

    /**
     * Checks if this item has been marked for deletion
     *
     * @param provider the {@link SyncProvider} for to check for
     * @return {@code true} if this item is marked for remote deletion
     */
    boolean isMarkedForDeletion(@NonNull SyncProvider provider);

    /**
     * @return the {@link MarkedForDeletionMap} associated with the current sync state
     */
    @Nullable
    MarkedForDeletionMap getMarkedForDeletionMap();

    /**
     * Gets the last time (in UTC) that this item was modified locally
     *
     * @return the last {@link Date} time this item was modified
     */
    @NonNull
    Date getLastLocalModificationTime();


}
