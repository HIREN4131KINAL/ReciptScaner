package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.tables.adapters.DatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.adapters.TripDatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.persistence.database.tables.keys.TripPrimaryKey;

public final class TripsTable extends AbstractSqlTable<Trip, String> {

    public static final String TABLE_NAME = "trips";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FROM = "from_date";
    public static final String COLUMN_TO = "to_date";
    public static final String COLUMN_FROM_TIMEZONE = "from_timezone";
    public static final String COLUMN_TO_TIMEZONE = "to_timezone";
    public static final String COLUMN_MILEAGE = "miles_new";
    public static final String COLUMN_COMMENT = "trips_comment";
    public static final String COLUMN_COST_CENTER = "trips_cost_center";
    public static final String COLUMN_DEFAULT_CURRENCY = "trips_default_currency";
    public static final String COLUMN_FILTERS = "trips_filters";
    public static final String COLUMN_PROCESSING_STATUS = "trip_processing_status";

    @SuppressWarnings("unused")
    @Deprecated
    private static final String COLUMN_PRICE = "price"; // Once used but keeping to avoid future name conflicts

    public TripsTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull PersistenceManager persistenceManager) {
        super(sqLiteOpenHelper, TABLE_NAME, new TripDatabaseAdapter(persistenceManager), new TripPrimaryKey());
    }

}
