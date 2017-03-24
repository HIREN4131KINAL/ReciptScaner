package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.alterations.TripTableActionAlterations;

public class TripTableController extends AbstractTableController<Trip> {

    public TripTableController(@NonNull PersistenceManager persistenceManager, @NonNull Analytics analytics) {
        super(persistenceManager.getDatabase().getTripsTable(), new TripTableActionAlterations(persistenceManager), analytics);
    }

}