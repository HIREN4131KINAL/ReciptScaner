package co.smartreceipts.android.sync.drive.listeners;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;

public class DistanceBackupListener extends StubTableEventsListener<Distance> {

    @Override
    public void onInsertSuccess(@NonNull Distance receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onUpdateSuccess(@NonNull Distance oldDistance, @NonNull Distance newDistance, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onDeleteSuccess(@NonNull Distance receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }
}
