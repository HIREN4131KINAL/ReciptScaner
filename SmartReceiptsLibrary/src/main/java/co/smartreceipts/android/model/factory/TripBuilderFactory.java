package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.filters.FilterFactory;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.impl.DefaultTripImpl;

/**
 * A {@link co.smartreceipts.android.model.Trip} {@link co.smartreceipts.android.model.factory.BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Trip} objects
 */
public final class TripBuilderFactory implements BuilderFactory<Trip> {

    private File _dir;
    private String _comment;
    private Date _startDate, _endDate;
    private TimeZone _startTimeZone, _endTimeZone;
    private WBCurrency _currency, _defaultCurrency;
    private Filter<ReceiptRow> _filter;
    private Source _source;

    public TripBuilderFactory() {
        _comment = "";
        _startDate = new Date(System.currentTimeMillis());
        _endDate = _startDate;
        _source = Source.Undefined;
        _startTimeZone = TimeZone.getDefault();
        _endTimeZone = TimeZone.getDefault();
    }

    public TripBuilderFactory setDirectory(@NonNull File directory) {
        _dir = directory;
        return this;
    }

    public TripBuilderFactory setStartDate(@NonNull Date startDate) {
        _startDate = startDate;
        return this;
    }

    public TripBuilderFactory setStartDate(long startDate) {
        _startDate = new Date(startDate);
        return this;
    }

    public TripBuilderFactory setEndDate(@NonNull Date endDate) {
        _endDate = endDate;
        return this;
    }

    public TripBuilderFactory setEndDate(long endDate) {
        _endDate = new Date(endDate);
        return this;
    }

    public TripBuilderFactory setStartTimeZone(TimeZone startTimeZone) {
        _startTimeZone = startTimeZone;
        return this;
    }

    public TripBuilderFactory setStartTimeZone(String timeZoneId) {
        if (timeZoneId != null) {
            _startTimeZone = TimeZone.getTimeZone(timeZoneId);
        }
        return this;
    }

    public TripBuilderFactory setEndTimeZone(TimeZone endTimeZone) {
        _endTimeZone = endTimeZone;
        return this;
    }

    public TripBuilderFactory setEndTimeZone(String timeZoneId) {
        if (timeZoneId != null) {
            _endTimeZone = TimeZone.getTimeZone(timeZoneId);
        }
        return this;
    }

    public TripBuilderFactory setCurrency(WBCurrency currency) {
        _currency = currency;
        return this;
    }

    public TripBuilderFactory setCurrency(@NonNull String currencyCode) {
        if (TextUtils.isEmpty(currencyCode)) {
            throw new IllegalArgumentException("The currency code cannot be null or empty");
        }
        _currency = WBCurrency.getInstance(currencyCode);
        return this;
    }

    public TripBuilderFactory setDefaultCurrency(WBCurrency currency) {
        _defaultCurrency = currency;
        return this;
    }

    public TripBuilderFactory setDefaultCurrency(String currencyCode) {
        _defaultCurrency = WBCurrency.getInstance(currencyCode);
        return this;
    }

    public TripBuilderFactory setDefaultCurrency(String currencyCode, String missingCodeDefault) {
        if (TextUtils.isEmpty(currencyCode)) {
            _defaultCurrency = WBCurrency.getInstance(missingCodeDefault);
        } else {
            _defaultCurrency = WBCurrency.getInstance(currencyCode);
        }
        return this;
    }

    public TripBuilderFactory setComment(String comment) {
        _comment = comment;
        return this;
    }

    public TripBuilderFactory setFilter(Filter<ReceiptRow> filter) {
        _filter = filter;
        return this;
    }

    public TripBuilderFactory setFilter(JSONObject json) {
        if (json != null) {
            try {
                _filter = FilterFactory.getReceiptFilter(json);
            } catch (JSONException e) {
            }
        }
        return this;
    }

    public TripBuilderFactory setFilter(String json) {
        if (!TextUtils.isEmpty(json)) {
            try {
                _filter = FilterFactory.getReceiptFilter(new JSONObject(json));
            } catch (JSONException e) {
            }
        }
        return this;
    }

    public TripBuilderFactory setSourceAsCache() {
        _source = Source.Cache;
        return this;
    }

    @Override
    public Trip build() {
        return new DefaultTripImpl(_dir, _startDate, _startTimeZone, _endDate, _endTimeZone, _currency, _defaultCurrency, _comment, _filter, _source);
    }
}
