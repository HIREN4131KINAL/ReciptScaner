package co.smartreceipts.android.persistence.database.controllers.impl;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.TableController;

@ApplicationScope
public class DistanceTableController extends TripForeignKeyAbstractTableController<Distance> {

    @Inject
    public DistanceTableController(DatabaseHelper databaseHelper, Analytics analytics,
                                   @co.smartreceipts.android.di.qualifiers.TripTableController TableController<Trip> tripTableController) {
        super(databaseHelper.getDistanceTable(), analytics);
        subscribe(new RefreshTripPricesListener<Distance>(tripTableController));
    }
}
