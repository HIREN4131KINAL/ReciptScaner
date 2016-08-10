package co.smartreceipts.android.sync.drive.listeners;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;

public class TripBackupListener extends StubTableEventsListener<Trip> {

    @Override
    public void onInsertSuccess(@NonNull Trip receipt) {

    }

    @Override
    public void onUpdateSuccess(@NonNull Trip oldTrip, @NonNull Trip newTrip) {

    }

    @Override
    public void onDeleteSuccess(@NonNull Trip receipt) {

    }
}
