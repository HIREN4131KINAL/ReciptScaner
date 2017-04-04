package co.smartreceipts.android.persistence.database.tables.keys;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.tables.TripsTable;

/**
 * Defines the primary key for the {@link TripsTable}
 */
public final class TripPrimaryKey implements PrimaryKey<Trip, String> {

    @Override
    @NonNull
    public String getPrimaryKeyColumn() {
        return TripsTable.COLUMN_NAME;
    }

    @Override
    @NonNull
    public Class<String> getPrimaryKeyClass() {
        return String.class;
    }

    @Override
    @NonNull
    public String getPrimaryKeyValue(@NonNull Trip trip) {
        return trip.getName();
    }
}
