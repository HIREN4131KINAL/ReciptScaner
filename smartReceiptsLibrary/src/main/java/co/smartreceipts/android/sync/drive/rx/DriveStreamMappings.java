package co.smartreceipts.android.sync.drive.rx;

import android.support.annotation.NonNull;

import com.google.android.gms.drive.DriveFile;

import java.sql.Date;
import java.util.Collections;

import co.smartreceipts.android.sync.SyncProvider;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;
import co.smartreceipts.android.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.model.impl.IdentifierMap;
import co.smartreceipts.android.sync.model.impl.MarkedForDeletionMap;

public class DriveStreamMappings {

    @NonNull
    public SyncState insertSyncState(@NonNull SyncState oldSyncState, @NonNull DriveFile driveFile) {
        return new DefaultSyncState(getSyncIdentifierMap(driveFile), oldSyncState.getMarkedForDeletionMap(), new Date(System.currentTimeMillis()));
    }

    @NonNull
    public SyncState updateSyncState(@NonNull SyncState oldSyncState, @NonNull DriveFile driveFile) {
        return new DefaultSyncState(getSyncIdentifierMap(driveFile), oldSyncState.getMarkedForDeletionMap(), new Date(System.currentTimeMillis()));
    }

    @NonNull
    public SyncState deleteSyncState(@NonNull SyncState oldSyncState) {
        return new DefaultSyncState(oldSyncState.getIdentifierMap(), new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, false)), new Date(System.currentTimeMillis()));
    }

    @NonNull
    public IdentifierMap getSyncIdentifierMap(@NonNull DriveFile driveFile) {
        return new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, getSyncIdentifier(driveFile)));
    }

    @NonNull
    public Identifier getSyncIdentifier(@NonNull DriveFile driveFile) {
        return new Identifier(driveFile.getDriveId().getResourceId());
    }

}
