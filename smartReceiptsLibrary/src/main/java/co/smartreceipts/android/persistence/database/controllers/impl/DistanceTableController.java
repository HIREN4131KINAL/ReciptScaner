package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.persistence.PersistenceManager;

public class DistanceTableController extends TripForeignKeyAbstractTableController<Distance> {

    public DistanceTableController(@NonNull PersistenceManager persistenceManager) {
        super(persistenceManager.getDatabase().getDistanceTable());
    }
}
