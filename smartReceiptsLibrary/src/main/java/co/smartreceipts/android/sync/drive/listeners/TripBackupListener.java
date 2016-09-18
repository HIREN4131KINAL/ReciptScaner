package co.smartreceipts.android.sync.drive.listeners;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;

public class TripBackupListener extends StubTableEventsListener<Trip> {

    @Override
    public void onInsertSuccess(@NonNull Trip receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onUpdateSuccess(@NonNull Trip oldTrip, @NonNull Trip newTrip, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onDeleteSuccess(@NonNull Trip receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }
}
