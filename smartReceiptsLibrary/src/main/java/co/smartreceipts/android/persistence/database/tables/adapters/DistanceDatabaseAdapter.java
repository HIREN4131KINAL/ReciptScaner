package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.DistanceBuilderFactory;
import co.smartreceipts.android.persistence.database.tables.DistanceTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link DistanceTable}
 */
public final class DistanceDatabaseAdapter implements DatabaseAdapter<Distance, PrimaryKey<Distance, Integer>> {

    private final Table<Trip, String> mTripsTable;

    public DistanceDatabaseAdapter(@NonNull Table<Trip, String> tripsTable) {
        mTripsTable = Preconditions.checkNotNull(tripsTable);
    }

    @NonNull
    @Override
    public Distance read(@NonNull Cursor cursor) {
        final int idIndex = cursor.getColumnIndex(DistanceTable.COLUMN_ID);
        final int parentIndex = cursor.getColumnIndex(DistanceTable.COLUMN_PARENT);
        final int locationIndex = cursor.getColumnIndex(DistanceTable.COLUMN_LOCATION);
        final int distanceIndex = cursor.getColumnIndex(DistanceTable.COLUMN_DISTANCE);
        final int dateIndex = cursor.getColumnIndex(DistanceTable.COLUMN_DATE);
        final int timezoneIndex = cursor.getColumnIndex(DistanceTable.COLUMN_TIMEZONE);
        final int rateIndex = cursor.getColumnIndex(DistanceTable.COLUMN_RATE);
        final int rateCurrencyIndex = cursor.getColumnIndex(DistanceTable.COLUMN_RATE_CURRENCY);
        final int commentIndex = cursor.getColumnIndex(DistanceTable.COLUMN_COMMENT);

        final int id = cursor.getInt(idIndex);
        final Trip trip = mTripsTable.findByPrimaryKey(cursor.getString(parentIndex)).toBlocking().first();
        final String location = cursor.getString(locationIndex);
        final BigDecimal distance = BigDecimal.valueOf(cursor.getDouble(distanceIndex));
        final long date = cursor.getLong(dateIndex);
        final String timezone = cursor.getString(timezoneIndex);
        final BigDecimal rate = BigDecimal.valueOf(cursor.getDouble(rateIndex));
        final String rateCurrency = cursor.getString(rateCurrencyIndex);
        final String comment = cursor.getString(commentIndex);

        return new DistanceBuilderFactory(id)
                .setTrip(trip)
                .setLocation(location)
                .setDistance(distance)
                .setDate(date)
                .setTimezone(timezone)
                .setRate(rate)
                .setCurrency(rateCurrency)
                .setComment(comment)
                .build();
    }

    @NonNull
    @Override
    public ContentValues write(@NonNull Distance distance) {
        final ContentValues values = new ContentValues();

        values.put(DistanceTable.COLUMN_PARENT, distance.getTrip().getName());
        values.put(DistanceTable.COLUMN_LOCATION, distance.getLocation().trim());
        values.put(DistanceTable.COLUMN_DISTANCE, distance.getDistance().doubleValue());
        values.put(DistanceTable.COLUMN_TIMEZONE, distance.getTimeZone().getID());
        values.put(DistanceTable.COLUMN_DATE, distance.getDate().getTime());
        values.put(DistanceTable.COLUMN_RATE, distance.getRate().doubleValue());
        values.put(DistanceTable.COLUMN_RATE_CURRENCY, distance.getPrice().getCurrencyCode());
        values.put(DistanceTable.COLUMN_COMMENT, distance.getComment().trim());

        return values;
    }

    @NonNull
    @Override
    public Distance build(@NonNull Distance distance, @NonNull PrimaryKey<Distance, Integer> primaryKey) {
        return new DistanceBuilderFactory(primaryKey.getPrimaryKeyValue(distance), distance).build();
    }
}
