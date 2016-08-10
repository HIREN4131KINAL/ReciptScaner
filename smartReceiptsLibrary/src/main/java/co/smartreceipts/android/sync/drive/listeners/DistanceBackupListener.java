package co.smartreceipts.android.sync.drive.listeners;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;

public class DistanceBackupListener extends StubTableEventsListener<Distance> {

    @Override
    public void onInsertSuccess(@NonNull Distance receipt) {

    }

    @Override
    public void onUpdateSuccess(@NonNull Distance oldDistance, @NonNull Distance newDistance) {

    }

    @Override
    public void onDeleteSuccess(@NonNull Distance receipt) {

    }
}
