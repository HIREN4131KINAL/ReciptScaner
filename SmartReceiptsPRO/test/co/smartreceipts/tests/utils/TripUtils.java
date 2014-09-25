package co.smartreceipts.tests.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.Date;
import java.util.TimeZone;

import android.os.Environment;
import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.android.model.WBCurrency;

public class TripUtils {

	public static class Constants {
		public static final String CURRENCY_CODE = "USD";
		public static final WBCurrency CURRENCY = WBCurrency.getInstance(CURRENCY_CODE); // 1
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
		public static final double PRICE = 12.55d;
		public static final String PRICE_STRING = "12.55";
		public static final double DAILY_SUBTOTAL = 1.25d;
		public static final String DAILY_SUBTOTAL_STRING = "1.25";
		public static final float MILEAGE = 40.3121f;
	}

	public static final TripRow newDefaultTripRowInstance() {
		TripRow.Builder builder = new TripRow.Builder();
		builder.setCurrency(Constants.CURRENCY_CODE)
			   .setDefaultCurrency(Constants.CURRENCY_CODE)
			   .setDirectory(Constants.DIRECTORY)
			   .setEndDate(Constants.END_DATE_MILLIS)
			   .setEndTimeZone(Constants.END_TIMEZONE_CODE)
			   .setMileage(Constants.MILEAGE)
			   .setStartDate(Constants.START_DATE_MILLIS)
			   .setStartTimeZone(Constants.START_TIMEZONE_CODE)
			   .setComment(Constants.COMMENT);
		final TripRow tripRow = builder.build();
		tripRow.setPrice(Constants.PRICE);
		tripRow.setDailySubTotal(Constants.DAILY_SUBTOTAL);
		return tripRow;
	}

	public static void assertFieldEquality(TripRow trip1, TripRow trip2) {
		assertEquals(trip1.getDirectory(), trip2.getDirectory());
		assertEquals(trip1.getStartDate(), trip2.getStartDate());
		assertEquals(trip1.getStartTimeZone(), trip2.getStartTimeZone());
		assertEquals(trip1.getEndDate(), trip2.getEndDate());
		assertEquals(trip1.getEndTimeZone(), trip2.getEndTimeZone());
		assertEquals(trip1.getPrice(), trip2.getPrice());
		assertEquals(trip1.getPriceAsFloat(), trip2.getPriceAsFloat(), TestUtils.EPSILON);
		assertEquals(trip1.getDecimalFormattedPrice(), trip2.getDecimalFormattedPrice());
		assertEquals(trip1.getCurrencyCode(), trip2.getCurrencyCode());
		assertEquals(trip1.getCurrencyFormattedPrice(), trip2.getCurrencyFormattedPrice());
		assertEquals(trip1.getMileage(), trip2.getMileage(), TestUtils.EPSILON);
		assertEquals(trip1.getMilesAsString(), trip2.getMilesAsString());
	}

}
