package co.smartreceipts.android.model.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.TimeZone;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.model.WBCurrency;

/**
 * A utility class, which will be used to standard some common functions that are
 * shared across multiple model objects
 */
public class ModelUtils {

    private ModelUtils() {
        throw new RuntimeException("This class uses static calls only. It cannot be instantianted");
    }


    /**
     * Gets a formatted version of a date based on the timezone and locale for a given separator. In the US,
     * we might expect to see a result like "10/23/2014" returned if we set the separator as "/"
     *
     * @param date      - the {@link java.sql.Date} to format
     * @param timeZone  - the {@link java.util.TimeZone} to use for this date
     * @param context   - the current {@link android.content.Context}
     * @param separator - the date separator (e.g. "/", "-", ".")
     * @return the formatted date string for the start date
     */
    public static String getFormattedDate(@NonNull Date date, @NonNull TimeZone timeZone, @NonNull Context context, @NonNull String separator) {
        final java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
        format.setTimeZone(timeZone); // Hack to shift the timezone appropriately
        final String formattedDate = format.format(date);
        return formattedDate.replace(DateUtils.getDateSeparator(context), separator);
    }

    /**
     * Generates "decimal-formatted" value, which would appear to the end user as "25.20" or "25,20" instead of
     * showing naively as "25.2" or "25.2001910"
     *
     * @param decimal - the {@link java.math.BigDecimal} to format
     * @return the decimal formatted price {@link java.lang.String}
     */
    public static String getDecimalFormattedValue(@NonNull BigDecimal decimal) {
        final DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setGroupingUsed(false);
        return decimalFormat.format(decimal);
    }

    /**
     * The "currency-formatted" value, which would appear as "$25.20" or "$25,20" as determined by the user's locale.
     *
     * @param decimal  - the {@link java.math.BigDecimal} to format
     * @param currency - the {@link co.smartreceipts.android.model.WBCurrency} to use. If this is {@code null}, return {@link #getDecimalFormattedValue(java.math.BigDecimal)}
     * @return - the currency formatted price {@link java.lang.String}
     */
    public static String getCurrencyFormattedValue(@NonNull BigDecimal decimal, @Nullable WBCurrency currency) {
        if (currency != null) {
            return currency.format(decimal);
        } else {
            return getDecimalFormattedValue(decimal);
        }
    }
}
