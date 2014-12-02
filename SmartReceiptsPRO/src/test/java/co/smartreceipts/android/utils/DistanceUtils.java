package co.smartreceipts.android.utils;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.WBCurrency;

import static org.junit.Assert.assertEquals;

public class DistanceUtils {

    public static class Constants {
        public static final String COMMENT = "Comment";
        public static final String CURRENCY_CODE = "USD";
        public static final WBCurrency CURRENCY = WBCurrency.getInstance(CURRENCY_CODE);
        public static final long DATE_MILLIS = 1409703721000L; // 09/02/2014 @ 8:22EDT
        public static final Date DATE = new Date(DATE_MILLIS);
        public static final String SLASH_FORMATTED_DATE = "Sep/02/2014";
        public static final String DASH_FORMATTED_DATE = "Sep-02-2014";
        public static final double DISTANCE_DOUBLE = 12.55d;
        public static final BigDecimal DISTANCE = new BigDecimal(DISTANCE_DOUBLE);
        public static final String DECIMAL_FORMATTED_DISTANCE = "12.55";
        public static final int ID = 5;
        public static final String LOCATION = "Location";
        public static final double RATE_DOUBLE = 0.33d;
        public static final BigDecimal RATE = new BigDecimal(RATE_DOUBLE);
        public static final String DECIMAL_FORMATTED_RATE = "0.33";
        public static final String CURRENCY_FORMATTED_RATE = "$0.33";
        public static final TimeZone TIMEZONE = TimeZone.getDefault();
        public static final String TIMEZONE_CODE = TIMEZONE.getID();
    }

    public static void assertFieldEquality(Distance distance1, Distance distance2) {
        assertEquals(distance1.getComment(), distance2.getComment());
        assertEquals(distance1.getDate(), distance2.getDate());
        assertEquals(distance1.getDistance(), distance2.getDistance());
        assertEquals(distance1.getId(), distance2.getId());
        assertEquals(distance1.getLocation(), distance2.getLocation());
        assertEquals(distance1.getRate(), distance2.getRate());
        assertEquals(distance1.getTimeZone(), distance2.getTimeZone());
        assertEquals(distance1.getTrip(), distance2.getTrip());
    }

}
