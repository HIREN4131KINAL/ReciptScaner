package co.smartreceipts.android.model;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;

public interface Distance extends Parcelable {

    public static final String PARCEL_KEY = Distance.class.getName();

    /**
     * Gets the primary key id for this distance
     *
     * @return the distance's autoincrement id
     */
    public long getId();

    /**
     * Gets the parent trip for this distance. This should never be {@code null}.
     *
     * @return - the parent {@link co.smartreceipts.android.model.Trip}
     */
    @NonNull
    public Trip getTrip();

    /**
     * Gets the location to which this distance occurred (e.g. drove to Atlanta)
     *
     * @return the location as a {@link java.lang.String}
     */
    public String getLocation();

    /**
     * Gets the decimal representation of the distance travelled
     *
     * @return - a {@link java.math.BigDecimal} containing the distance travelled
     */
    public BigDecimal getDistance();

    /**
     * A "decimal-formatted" distance, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2" or "25.2123144444"
     *
     * @return the decimal formatted distance {@link java.lang.String}
     */
    public String getDecimalFormattedDistance();

    /**
     * Returns the date on which this distance occurred
     *
     * @return the {@link java.sql.Date} this distance occurred
     */
    Date getDate();

    /**
     * Gets a formatted version of the date based on the timezone and locale for a given separator. In the US,
     * we might expect to see a result like "10/23/2014" returned if we set the separator as "/"
     *
     * @param context   - the current {@link android.content.Context}
     * @param separator - the date separator (e.g. "/", "-", ".")
     * @return the formatted date string for this distance
     */
    String getFormattedDate(Context context, String separator);

    /**
     * Gets the time zone in which the date was set
     *
     * @return - the {@link java.util.TimeZone} for the date
     */
    public TimeZone getTimezone();

    /**
     * The rate for which this distance may be reimbursed
     *
     * @return a {@link java.math.BigDecimal} representation of the reimbursement rate
     */
    public BigDecimal getRate();

    /**
     * A "decimal-formatted" rate, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2"
     *
     * @return the decimal formatted rate {@link java.lang.String}
     */
    String getDecimalFormattedRate();

    /**
     * The "currency-formatted" rate, which would appear as "$25.20" or "$25,20" as determined by the user's locale
     *
     * @return - the currency formatted rate {@link java.lang.String}
     */
    String getCurrencyFormattedRate();

    /**
     * Gets the currency which this distance's rate is tracked in
     *
     * @return - the {@link co.smartreceipts.android.model.WBCurrency} currency representation
     */
    WBCurrency getCurrency();

    /**
     * Gets the currency code representation for this distance's rate or {@link co.smartreceipts.android.model.WBCurrency#MISSING_CURRENCY}
     * if it cannot be found
     *
     * @return the currency code {@link java.lang.String} for this distance
     */
    String getCurrencyCode();

    /**
     * Gets the user defined comment for this receipt
     *
     * @return - the current comment as a {@link java.lang.String}
     */
    public String getComment();

}
