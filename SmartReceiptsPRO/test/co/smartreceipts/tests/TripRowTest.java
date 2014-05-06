package co.smartreceipts.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.sql.Date;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.android.model.WBCurrency;

@RunWith(RobolectricTestRunner.class)
public class TripRowTest {

	private static final float EPSILON = 0.0001f;

	private static class Constants {
		public static final String CURRENCY_CODE = "USD";
		public static final WBCurrency CURRENCY = WBCurrency.getInstance(CURRENCY_CODE); //1
		public static final long START_DATE_MILLIS = System.currentTimeMillis();
		public static final Date START_DATE = new Date(START_DATE_MILLIS);
		public static final long END_DATE_MILLIS = System.currentTimeMillis();
		public static final Date END_DATE = new Date(END_DATE_MILLIS);
		public static final File DIRECTORY = new File("Report");
		public static final TimeZone START_TIMEZONE = TimeZone.getDefault();
		public static final String START_TIMEZONE_CODE = START_TIMEZONE.getID();
		public static final TimeZone END_TIMEZONE = TimeZone.getDefault();
		public static final String END_TIMEZONE_CODE = END_TIMEZONE.getID();
		public static final String PRICE = "12.55";
		public static final float MILEAGE = 40.3121f;
	}

	/**
	 * TripRowA and TripRowB should be expected as having all member variables be equal.
	 * The .equals method for TripRow only tests their Directories and will not be a valid indicator
	 * of whether or not they are actually equal or not.
	 * The difference between the two is how their builders are constructed
	 **/
	private TripRow mTripRowA, mTripRowB;

	/**
	 * Generates a builder for mTripRowA. This builder user primitives/Strings
	 * whenever possible as opposed to higher level objects
	 * @return
	 */
	private TripRow.Builder getTripRowABuilder() {
		TripRow.Builder builderA = new TripRow.Builder();
		builderA.setCurrency(Constants.CURRENCY_CODE)
				.setDirectory(Constants.DIRECTORY)
				.setEndDate(Constants.END_DATE_MILLIS)
				.setEndTimeZone(Constants.END_TIMEZONE_CODE)
				.setMileage(Constants.MILEAGE)
				.setPrice(Constants.PRICE)
				.setStartDate(Constants.START_DATE_MILLIS)
				.setStartTimeZone(Constants.START_TIMEZONE_CODE);
		return builderA;
	}

	/**
	 * Generates a builder for mReceiptRowB. This builder users higher level objects
	 * as opposed to primitives/Strings whenever possible
	 * @return
	 */
	private TripRow.Builder getTripRowBBuilder() {
		TripRow.Builder builderB = new TripRow.Builder();
		builderB.setCurrency(Constants.CURRENCY)
				.setDirectory(Constants.DIRECTORY)
				.setEndDate(Constants.END_DATE)
				.setEndTimeZone(Constants.END_TIMEZONE)
				.setMileage(Constants.MILEAGE)
				.setPrice(Constants.PRICE)
				.setStartDate(Constants.START_DATE)
				.setStartTimeZone(Constants.START_TIMEZONE);
		return builderB;
	}

	@Before
	public void setUp() throws Exception {
		mTripRowA = getTripRowABuilder().build();
		mTripRowB = getTripRowBBuilder().build();
	}

	@After
	public void tearDown() throws Exception {
		mTripRowA = null;
		mTripRowB = null;
	}

	@Test
	public void testTripRowEquality() {
		assertEquals(mTripRowA, mTripRowB);
	}

	@Test
	public void testTripRowName() {
		assertEquals(mTripRowA.getDirectory(), mTripRowB.getDirectory());
		assertEquals(mTripRowA.getDirectory(), Constants.DIRECTORY);
	}

	@Test
	public void testTripRowStartDates() {
		assertEquals(mTripRowA.getStartDate(), mTripRowB.getStartDate());
		assertEquals(mTripRowA.getStartTimeZone(), mTripRowB.getStartTimeZone());
		assertEquals(mTripRowA.getStartDate(), Constants.START_DATE);
		assertEquals(mTripRowA.getStartTimeZone(), Constants.START_TIMEZONE);
		//TODO: Add Context to get Formatted Dates
	}

	@Test
	public void testTripRowEndDates() {
		assertEquals(mTripRowA.getEndDate(), mTripRowB.getEndDate());
		assertEquals(mTripRowA.getEndTimeZone(), mTripRowB.getEndTimeZone());
		assertEquals(mTripRowA.getEndDate(), Constants.END_DATE);
		assertEquals(mTripRowA.getEndTimeZone(), Constants.END_TIMEZONE);
		//TODO: Add Context to get Formatted Dates
	}

	@Test
	public void testReceiptRowPriceAndCurrency() {
		assertEquals(mTripRowA.getPrice(), mTripRowB.getPrice());
		assertEquals(mTripRowA.getPriceAsFloat(), mTripRowB.getPriceAsFloat(), EPSILON);
		assertEquals(mTripRowA.getDecimalFormattedPrice(), mTripRowB.getDecimalFormattedPrice());
		assertEquals(mTripRowA.getCurrencyCode(), mTripRowB.getCurrencyCode());
		assertEquals(mTripRowA.getCurrencyFormattedPrice(), mTripRowB.getCurrencyFormattedPrice());
		assertEquals(mTripRowA.getPrice(), Constants.PRICE);
		assertEquals(mTripRowA.getCurrencyCode(), Constants.CURRENCY_CODE);
	}

	@Test
	public void testTripRowMileage() {
		assertEquals(mTripRowA.getMileage(), mTripRowB.getMileage(), EPSILON);
		assertEquals(mTripRowA.getMilesAsString(), mTripRowB.getMilesAsString());
		assertEquals(mTripRowA.getMileage(), Constants.MILEAGE, EPSILON);
	}


}