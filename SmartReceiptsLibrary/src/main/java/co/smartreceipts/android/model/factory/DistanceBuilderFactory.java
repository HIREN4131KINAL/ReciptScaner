package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.impl.ImmutableDistanceImpl;

/**
 * A {@link co.smartreceipts.android.model.Distance} {@link co.smartreceipts.android.model.factory.BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Distance} objects
 */
public final class DistanceBuilderFactory implements BuilderFactory<Distance> {

    private int _id;
    private Trip _trip;
    private String _location;
    private BigDecimal _distance;
    private Date _date;
    private TimeZone _timezone;
    private BigDecimal _rate;
    private WBCurrency _currency;
    private String _comment;

    public DistanceBuilderFactory() {
        this(MISSING_ID);
    }

    public DistanceBuilderFactory(int id) {
        _id = id;
        _location = "";
        _distance = new BigDecimal(0);
        _date = new Date(System.currentTimeMillis());
        _timezone = TimeZone.getDefault();
        _rate = new BigDecimal(0);
        _comment = "";
    }

    public DistanceBuilderFactory(int id, @NonNull Distance distance) {
        _id = id;
        _trip = distance.getTrip();
        _location = distance.getLocation();
        _distance = distance.getDistance();
        _date = distance.getDate();
        _timezone = distance.getTimeZone();
        _rate = distance.getRate();
        _currency = distance.getPrice().getCurrency();
        _comment = distance.getComment();
    }

    public DistanceBuilderFactory setTrip(final Trip trip) {
        _trip = trip;
        return this;
    }

    public DistanceBuilderFactory setLocation(String location) {
        _location = location;
        return this;
    }

    public DistanceBuilderFactory setDistance(BigDecimal distance) {
        _distance = distance;
        return this;
    }

    public DistanceBuilderFactory setDistance(double distance) {
        _distance = new BigDecimal(distance);
        return this;
    }

    public DistanceBuilderFactory setDate(Date date) {
        _date = date;
        return this;
    }

    public DistanceBuilderFactory setDate(long date) {
        _date = new Date(date);
        return this;
    }

    public DistanceBuilderFactory setTimezone(@Nullable String timezone) {
        // Our distance table doesn't have a default timezone, so protect for nulls
        if (timezone != null) {
            _timezone = TimeZone.getTimeZone(timezone);
        }
        return this;
    }

    public DistanceBuilderFactory setTimezone(TimeZone timezone) {
        _timezone = timezone;
        return this;
    }

    public DistanceBuilderFactory setRate(BigDecimal rate) {
        _rate = rate;
        return this;
    }

    public DistanceBuilderFactory setRate(double rate) {
        _rate = new BigDecimal(rate);
        return this;
    }

    public DistanceBuilderFactory setCurrency(WBCurrency currency) {
        _currency = currency;
        return this;
    }

    public DistanceBuilderFactory setCurrency(@NonNull String currencyCode) {
        if (TextUtils.isEmpty(currencyCode)) {
            throw new IllegalArgumentException("The currency code cannot be null or empty");
        }
        _currency = WBCurrency.getInstance(currencyCode);
        return this;
    }

    public DistanceBuilderFactory setComment(String comment) {
        _comment = comment;
        return this;
    }

    @Override
    @NonNull
    public Distance build() {
        return new ImmutableDistanceImpl(_id, _trip, _location, _distance, _rate, _currency, _date, _timezone, _comment);
    }
}
