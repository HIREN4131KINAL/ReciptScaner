package co.smartreceipts.android.sync.drive.listeners;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;

public class DatabaseBackupListener<ModelType> extends StubTableEventsListener<ModelType> {

    private final DriveStreamsManager mDriveTaskManager;

    public DatabaseBackupListener(@NonNull DriveStreamsManager driveTaskManager) {
        mDriveTaskManager = Preconditions.checkNotNull(driveTaskManager);
    }

    @Override
    public void onInsertSuccess(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mDriveTaskManager.updateDatabase();
        }
    }

    @Override
    public void onUpdateSuccess(@NonNull ModelType oldT, @NonNull ModelType newT, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mDriveTaskManager.updateDatabase();
        }
    }

    @Override
    public void onDeleteSuccess(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mDriveTaskManager.updateDatabase();
        }
    }
}
