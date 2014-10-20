package co.smartreceipts.android.model.utils;

import android.content.Context;

import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.date.DateUtils;

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
    public static String getFormattedDate(Date date, TimeZone timeZone, Context context, String separator) {
        final java.text.DateFormat format = android.text.format.DateFormat.getDateFormat(context);
        format.setTimeZone(timeZone); // Hack to shift the timezone appropriately
        final String formattedDate = format.format(date);
        return formattedDate.replace(DateUtils.getDateSeparator(context), separator);
    }
}
