package co.smartreceipts.android.persistence.database.controllers.impl;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.TableController;

public class DistanceTableController extends TripForeignKeyAbstractTableController<Distance> {

    public DistanceTableController(@NonNull PersistenceManager persistenceManager, @NonNull TableController<Trip> tripTableController) {
        super(persistenceManager.getDatabase().getDistanceTable());
        subscribe(new RefreshTripPricesListener<Distance>(Preconditions.checkNotNull(tripTableController)));
    }
}
