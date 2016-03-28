package co.smartreceipts.android.model;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.filters.Filter;

public interface Trip extends Parcelable, Priceable {

    public static final String PARCEL_KEY = Trip.class.getName();

    /**
     * Gets the name of this trip
     *
     * @return the {@link String} object, contain the name of this trip (this will be the name of {@link #getDirectory()}
     */
    String getName();

    /**
     * Gets the directory in which all this trip's images are stored
     *
     * @return the {@link java.io.File} directory containing this trips images
     */
    File getDirectory();

    /**
     * Gets the absolute path of this Trip's directory from {@link #getDirectory()}
     *
     * @return a representation of the directory path via {@link #getDirectory()} and {@link java.io.File#getAbsolutePath()}.
     */
    String getDirectoryPath();

    /**
     * Gets the date upon which this trip began
     *
     * @return the start {@link java.sql.Date}
     */
    Date getStartDate();

    /**
     * Gets a formatted version of the start date based on the timezone and locale for a given separator. In the US,
     * we might expect to see a result like "10/23/2014" returned if we set the separator as "/"
     *
     * @param context   - the current {@link android.content.Context}
     * @param separator - the date separator (e.g. "/", "-", ".")
     * @return the formatted date string for the start date
     */
    String getFormattedStartDate(Context context, String separator);

    /**
     * Gets the time zone in which the start date was set
     *
     * @return - the {@link java.util.TimeZone} for the start date
     */
    TimeZone getStartTimeZone();

    /**
     * Gets the date upon which this trip will end
     *
     * @return the end {@link java.sql.Date}
     */
    Date getEndDate();

    /**
     * Gets a formatted version of the end date based on the timezone and locale for a given separator. In the US,
     * we might expect to see a result like "10/23/2014" returned if we set the separator as "/"
     *
     * @param context   - the current {@link android.content.Context}
     * @param separator - the date separator (e.g. "/", "-", ".")
     * @return the formatted date string for the end date
     */
    String getFormattedEndDate(Context context, String separator);

    /**
     * Gets the time zone in which the end date was set
     *
     * @return - the {@link java.util.TimeZone} for the start date
     */
    TimeZone getEndTimeZone();

    /**
     * Tests if a particular date is included with the bounds of this particular trip. When performing the test, it uses
     * the local time zone for the date, and the defined time zones for the start and end date bounds. The start date
     * time is assumed to occur at 00:01 of the start day and the end date is assumed to occur at 23:59 of the end day.
     * The reasoning behind this is to ensure that it appears properly from a UI perspective. Since the initial date
     * only shows the day itself, it may include an arbitrary time that is never shown to the user. Setting the time
     * aspect manually accounts for this. This returns false if the date is null.
     *
     * @param date - the {@link java.sql.Date} to test
     * @return {@code true} if it is contained within. {@code false} otherwise
     */
    boolean isDateInsideTripBounds(Date date);

    /**
     * As the price of a trip exists as a function of it's receipt children (and not itself), this method can
     * be used to update the price representation
     *
     * @param price - the new price to use
     */
    void setPrice(@NonNull Price price);

    /**
     * Gets the daily sub-total price (i.e. all expenditures that occurred today) for this trip
     *
     * @return the daily sub-total {@link co.smartreceipts.android.model.Price}
     */
    Price getDailySubTotal();

    /**
     * As the daily sub-total of a trip exists as a function of it's receipt children (and not itself), this method can
     * be used to update the sub-total representation
     *
     * @param dailySubTotal - the new daily sub-total {@link co.smartreceipts.android.model.Price} to use
     */
    void setDailySubTotal(@NonNull Price dailySubTotal);

    /**
     * Gets the currency which this trip is tracked in
     *
     * @return - the default {@link co.smartreceipts.android.model.WBCurrency} currency representation
     */
    @NonNull
    WBCurrency getTripCurrency();

    /**
     * Gets the default currency code representation for this trip or {@link co.smartreceipts.android.model.WBCurrency#MISSING_CURRENCY_CODE}
     * if it cannot be found
     *
     * @return the default currency code {@link java.lang.String} for this trip
     */
    @NonNull
    String getDefaultCurrencyCode();

    /**
     * Gets the total mileage for this trip. This has been deprecated in favor of {@link Distance}s.
     *
     * @return the total mileage as a float
     */
    @Deprecated
    float getMileage();

    /**
     * Gets the total mileage for this trip. This has been deprecated in favor of {@link Distance}s.
     *
     * @return the total mileage as a {@link java.lang.String}
     */
    @Deprecated
    String getMilesAsString();

    /**
     * Sets the mileage for this trip. This has been deprecated in favor of {@link Distance}s.
     *
     * @param mileage the current mileage
     */
    @Deprecated
    void setMileage(float mileage);

    /**
     * Gets the user defined comment for this trip
     *
     * @return - the current comment as a {@link java.lang.String}
     */
    String getComment();

    /**
     * Gets the cost center for this particular trip
     *
     * @return the {@link java.lang.String} containing the cost center
     */
    String getCostCenter();

    /**
     * Gets the source from which this trip was built for debugging purposes
     *
     * @return the {@link co.smartreceipts.android.model.Source}
     */
    Source getSource();

    /**
     * Tests if this particular trip has a {@link co.smartreceipts.android.filters.Filter} attached
     *
     * @return {@code true} if it has a {@link co.smartreceipts.android.filters.Filter}, {@code false} otherwise
     */
    boolean hasFilter();

    /**
     * Gets the {@link co.smartreceipts.android.filters.Filter} that is associated with this trip and can be applied
     * to multiple {@link Receipt} objects that are children of this trip
     *
     * @return the {@link co.smartreceipts.android.filters.Filter} or {@code null} if one does not exist
     */
    Filter<Receipt> getFilter();

}
