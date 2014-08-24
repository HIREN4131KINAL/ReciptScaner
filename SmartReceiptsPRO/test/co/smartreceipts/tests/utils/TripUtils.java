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
		public static final WBCurrency CURRENCY = WBCurrency.getInstance(CURRENCY_CODE);
		public static final long START_DATE_MILLIS = System.currentTimeMillis();
		public static final Date START_DATE = new Date(START_DATE_MILLIS);
		public static final long END_DATE_MILLIS = System.currentTimeMillis();
		public static final Date END_DATE = new Date(END_DATE_MILLIS);
		public static final File DIRECTORY = new File(Environment.getExternalStorageDirectory(), "Report");
		public static final String DIRECTORY_NAME = "Report";
		public static final TimeZone START_TIMEZONE = TimeZone.getDefault();
		public static final String START_TIMEZONE_CODE = START_TIMEZONE.getID();
		public static final TimeZone END_TIMEZONE = TimeZone.getDefault();
		public static final String END_TIMEZONE_CODE = END_TIMEZONE.getID();
		public static final String COMMENT = "Comment";
		public static final String PRICE = "12.55";
		public static final float MILEAGE = 40.3121f;
	}
	
	public static final TripRow getDefaultTripRow() {
		TripRow.Builder builder = new TripRow.Builder();
		builder.setCurrency(Constants.CURRENCY_CODE)
			   .setDefaultCurrency(Constants.CURRENCY_CODE)
			   .setDirectory(Constants.DIRECTORY)
			   .setEndDate(Constants.END_DATE_MILLIS)
			   .setEndTimeZone(Constants.END_TIMEZONE_CODE)
			   .setMileage(Constants.MILEAGE)
			   .setPrice(Constants.PRICE)
			   .setStartDate(Constants.START_DATE_MILLIS)
			   .setStartTimeZone(Constants.START_TIMEZONE_CODE)
			   .setComment(Constants.COMMENT);
		return builder.build();
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
