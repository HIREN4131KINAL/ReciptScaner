package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.tables.adapters.DatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;

/**
 * Stores all database operations related to the {@link Distance} model objects
 */
public final class DistanceTable extends TripForeignKeyAbstractSqlTable<Distance, Integer> {

    // SQL Definitions:
    public static final String TABLE_NAME = "distance";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PARENT = "parent";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIMEZONE = "timezone";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_RATE = "rate";
    public static final String COLUMN_RATE_CURRENCY = "rate_currency";

    public DistanceTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull DatabaseAdapter<Distance, PrimaryKey<Distance, Integer>> databaseAdapter,
                         @NonNull PrimaryKey<Distance, Integer> primaryKey) {
        super(sqLiteOpenHelper, TABLE_NAME, databaseAdapter, primaryKey, COLUMN_PARENT, COLUMN_DATE);
    }

    @NonNull
    @Override
    protected Trip getTripFor(@NonNull Distance distance) {
        return distance.getTrip();
    }
}
