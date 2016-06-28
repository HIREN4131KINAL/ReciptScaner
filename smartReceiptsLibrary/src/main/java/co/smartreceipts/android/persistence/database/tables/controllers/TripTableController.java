package co.smartreceipts.android.persistence.database.tables.controllers;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;

public class TripTableController extends AbstractTableController<Trip, String> {

    public TripTableController(@NonNull PersistenceManager persistenceManager) {
        super(persistenceManager.getDatabase().getTripsTable(), new TripTableActionAlterations(persistenceManager));
    }

}