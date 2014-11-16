package co.smartreceipts.android.testutils;

import android.os.Environment;

import java.io.File;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.factory.TripBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

public class TripUtils {

    public static class Constants {
        public static final String CURRENCY_CODE = "USD";
        public static final WBCurrency CURRENCY = WBCurrency.getInstance(CURRENCY_CODE); // 1
        public static final String DEFAULT_CURRENCY_CODE = "USD";
        public static final WBCurrency DEFAULT_CURRENCY = WBCurrency.getInstance(DEFAULT_CURRENCY_CODE); // 1
        public static final long START_DATE_MILLIS = 1409703721000L; // 09/02/2014 @ 8:22EDT
        public static final Date START_DATE = new Date(START_DATE_MILLIS);
        public static final String SLASH_FORMATTED_START_DATE = "09/02/2014";
        public static final String DASH_FORMATTED_START_DATE = "09-02-2014";
        public static final long END_DATE_MILLIS = 1409703794000L; // 09/02/2014 @ 8:23EDT
        public static final Date END_DATE = new Date(END_DATE_MILLIS);
        public static final String SLASH_FORMATTED_END_DATE = "09/02/2014";
        public static final String DASH_FORMATTED_END_DATE = "09-02-2014";
        public static final File DIRECTORY = new File(Environment.getExternalStorageDirectory(), "Report");
        public static final String DIRECTORY_NAME = "Report";
        public static final TimeZone START_TIMEZONE = TimeZone.getTimeZone("America/New_York");
        public static final String START_TIMEZONE_CODE = START_TIMEZONE.getID();
        public static final TimeZone END_TIMEZONE = TimeZone.getTimeZone("America/New_York");
        public static final String END_TIMEZONE_CODE = END_TIMEZONE.getID();
        public static final String COMMENT = "Comment";
        public static final String COST_CENTER = "Cost Center";
        public static final double PRICE = 12.55d;
        public static final String DECIMAL_FORMATTED_PRICE = "12.55";
        public static final String CURRENCY_FORMATTED_PRICE = "$12.55";
        public static final double DAILY_SUBTOTAL = 1.25d;
        public static final String DECIMAL_FORMATTED_SUBTOTAL = "1.25";
        public static final String CURRENCY_FORMATTED_SUBTOTAL = "1.25";
        public static final float MILEAGE = 40.3121f;
    }

    public static TripBuilderFactory newDefaultTripBuilderFactory() {
        final TripBuilderFactory factory = new TripBuilderFactory().setDirectory(Constants.DIRECTORY)
                .setComment(Constants.COMMENT)
                .setCostCenter(Constants.COST_CENTER)
                .setCurrency(Constants.CURRENCY)
                .setDefaultCurrency(Constants.DEFAULT_CURRENCY)
                .setStartDate(Constants.START_DATE)
                .setStartTimeZone(Constants.START_TIMEZONE)
                .setEndDate(Constants.END_DATE)
                .setEndTimeZone(Constants.END_TIMEZONE);
        return factory;
    }

    public static Trip newSpyOfDefaultTrip() {
        final Trip trip = newDefaultTripBuilderFactory().build();
        trip.setPrice(TripUtils.Constants.PRICE);
        trip.setDailySubTotal(TripUtils.Constants.DAILY_SUBTOTAL);
        return spy(trip);
    }

    public static void assertFieldEquality(Trip trip1, Trip trip2) {
        assertEquals(trip1.getDirectory(), trip2.getDirectory());
        assertEquals(trip1.getStartDate(), trip2.getStartDate());
        assertEquals(trip1.getStartTimeZone(), trip2.getStartTimeZone());
        assertEquals(trip1.getEndDate(), trip2.getEndDate());
        assertEquals(trip1.getEndTimeZone(), trip2.getEndTimeZone());
        assertEquals(trip1.getPriceAsFloat(), trip2.getPriceAsFloat(), TestUtils.EPSILON);
        assertEquals(trip1.getDecimalFormattedPrice(), trip2.getDecimalFormattedPrice());
        assertEquals(trip1.getCurrencyFormattedPrice(), trip2.getCurrencyFormattedPrice());
        assertEquals(trip1.getCurrencyCode(), trip2.getCurrencyCode());
        assertEquals(trip1.getDailySubTotalAsFloat(), trip2.getDailySubTotalAsFloat(), TestUtils.EPSILON);
        assertEquals(trip1.getDecimalFormattedDailySubTotal(), trip2.getDecimalFormattedDailySubTotal());
        assertEquals(trip1.getCurrencyFormattedDailySubTotal(), trip2.getCurrencyFormattedDailySubTotal());
        assertEquals(trip1.getComment(), trip2.getComment());
        assertEquals(trip1.getCostCenter(), trip2.getCostCenter());
    }

}
