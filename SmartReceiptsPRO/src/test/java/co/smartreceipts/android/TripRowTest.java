package co.smartreceipts.android;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.tests.utils.TestUtils;
import co.smartreceipts.tests.utils.TripUtils;
import co.smartreceipts.tests.utils.TripUtils.Constants;

@Config(emulateSdk = 18) 
@RunWith(RobolectricTestRunner.class)
public class TripRowTest {

	/**
	 * TripRowA and TripRowB should be expected as having all member variables be equal.
	 * The .equals method for Trip only tests their Directories and will not be a valid indicator
	 * of whether or not they are actually equal or not.
	 * The difference between the two is how their builders are constructed
	 **/
	private Trip mTripA, mTripB;

	/**
	 * Generates a builder for mTripA. This builder user primitives/Strings
	 * whenever possible as opposed to higher level objects
	 * @return
	 */
	private Trip.Builder getTripRowABuilder() {
		Trip.Builder builderA = new Trip.Builder();
		builderA.setCurrency(Constants.CURRENCY_CODE)
				.setDefaultCurrency(Constants.CURRENCY_CODE)
				.setDirectory(Constants.DIRECTORY)
				.setEndDate(Constants.END_DATE_MILLIS)
				.setEndTimeZone(Constants.END_TIMEZONE_CODE)
				.setMileage(Constants.MILEAGE)
				.setStartDate(Constants.START_DATE_MILLIS)
				.setStartTimeZone(Constants.START_TIMEZONE_CODE)
				.setComment(Constants.COMMENT);
		return builderA;
	}

	/**
	 * Generates a builder for mReceiptRowB. This builder users higher level objects
	 * as opposed to primitives/Strings whenever possible
	 * @return
	 */
	private Trip.Builder getTripRowBBuilder() {
		Trip.Builder builderB = new Trip.Builder();
		builderB.setCurrency(Constants.CURRENCY)
				.setDefaultCurrency(Constants.CURRENCY_CODE)
				.setDirectory(Constants.DIRECTORY)
				.setEndDate(Constants.END_DATE)
				.setEndTimeZone(Constants.END_TIMEZONE)
				.setMileage(Constants.MILEAGE)
				.setStartDate(Constants.START_DATE)
				.setStartTimeZone(Constants.START_TIMEZONE)
				.setComment(Constants.COMMENT);
		return builderB;
	}

	@Before
	public void setUp() throws Exception {
		mTripA = getTripRowABuilder().build();
		mTripB = getTripRowBBuilder().build();
		mTripA.setPrice(Constants.PRICE);
		mTripA.setDailySubTotal(Constants.DAILY_SUBTOTAL);
		mTripB.setPrice(Constants.PRICE);
		mTripB.setDailySubTotal(Constants.DAILY_SUBTOTAL);
	}

	@After
	public void tearDown() throws Exception {
		mTripA = null;
		mTripB = null;
	}

	@Test
	public void testTripRowEquality() {
		assertEquals(mTripA, mTripB);
	}

	@Test
	public void testTripRowName() {
		assertEquals(mTripA.getDirectory(), mTripB.getDirectory());
		assertEquals(mTripA.getDirectory(), Constants.DIRECTORY);
	}

	@Test
	public void testTripRowStartDates() {
		assertEquals(mTripA.getStartDate(), mTripB.getStartDate());
		assertEquals(mTripA.getStartTimeZone(), mTripB.getStartTimeZone());
		assertEquals(mTripA.getStartDate(), Constants.START_DATE);
		assertEquals(mTripA.getStartTimeZone(), Constants.START_TIMEZONE);
		// assertEquals(mTripA.getFormattedStartDate(Robolectric.application, "/"), Constants.SLASH_FORMATTED_START_DATE);
		// assertEquals(mTripA.getFormattedStartDate(Robolectric.application, "-"), Constants.DASH_FORMATTED_START_DATE);
	}

	@Test
	public void testTripRowEndDates() {
		assertEquals(mTripA.getEndDate(), mTripB.getEndDate());
		assertEquals(mTripA.getEndTimeZone(), mTripB.getEndTimeZone());
		assertEquals(mTripA.getEndDate(), Constants.END_DATE);
		assertEquals(mTripA.getEndTimeZone(), Constants.END_TIMEZONE);
		// assertEquals(mTripA.getFormattedEndDate(Robolectric.application, "/"), Constants.SLASH_FORMATTED_END_DATE);
		// assertEquals(mTripA.getFormattedEndDate(Robolectric.application, "-"), Constants.DASH_FORMATTED_END_DATE);
	}

	@Test
	public void testReceiptRowPriceAndCurrency() {
		assertEquals(mTripA.getPrice(), mTripB.getPrice());
		assertEquals(mTripA.getPriceAsFloat(), mTripB.getPriceAsFloat(), TestUtils.EPSILON);
		assertEquals(mTripA.getDecimalFormattedPrice(), mTripB.getDecimalFormattedPrice());
		assertEquals(mTripA.getCurrencyCode(), mTripB.getCurrencyCode());
		assertEquals(mTripA.getCurrencyFormattedPrice(), mTripB.getCurrencyFormattedPrice());
		assertEquals(mTripA.getPrice(), Constants.PRICE_STRING);
		assertEquals(mTripA.getPriceAsFloat(), Constants.PRICE, TestUtils.EPSILON);
		assertEquals(mTripA.getCurrencyCode(), Constants.CURRENCY_CODE);
		
		mTripA.setPrice((float) Constants.PRICE);
		assertEquals(mTripA.getPriceAsFloat(), mTripB.getPriceAsFloat(), TestUtils.EPSILON);
		assertEquals(mTripA.getPrice(), Constants.PRICE_STRING);
		assertEquals(mTripA.getCurrencyCode(), Constants.CURRENCY_CODE);
	}
	
	@Test
	public void testReceiptDailySubTotalAndCurrency() {
		assertEquals(mTripA.getDailySubTotal(), mTripB.getDailySubTotal());
		assertEquals(mTripA.getDailySubTotalAsFloat(), mTripB.getDailySubTotalAsFloat(), TestUtils.EPSILON);
		assertEquals(mTripA.getCurrencyFormattedDailySubTotal(), mTripB.getCurrencyFormattedDailySubTotal());
		assertEquals(mTripA.getDailySubTotal(), Constants.DAILY_SUBTOTAL_STRING);
		assertEquals(mTripA.getDailySubTotalAsFloat(), Constants.DAILY_SUBTOTAL, TestUtils.EPSILON);
		
		mTripA.setPrice((float) Constants.PRICE);
		assertEquals(mTripA.getDailySubTotalAsFloat(), mTripB.getDailySubTotalAsFloat(), TestUtils.EPSILON);
		assertEquals(mTripA.getDailySubTotal(), Constants.DAILY_SUBTOTAL_STRING);
	}

	@Test
	public void testTripRowMileage() {
		assertEquals(mTripA.getMileage(), mTripB.getMileage(), TestUtils.EPSILON);
		assertEquals(mTripA.getMilesAsString(), mTripB.getMilesAsString());
		assertEquals(mTripA.getMileage(), Constants.MILEAGE, TestUtils.EPSILON);
	}
	
	@Test
	public void testTripRowComment() {
		assertEquals(mTripA.getComment(), mTripB.getComment());
		assertEquals(mTripA.getComment(), Constants.COMMENT);
	}
	
	@Test
	public void parcelTest() {
		Parcel parcelA = Parcel.obtain();
		mTripA.writeToParcel(parcelA, 0);
		parcelA.setDataPosition(0);
		Trip parcelTripA = Trip.CREATOR.createFromParcel(parcelA);
		assertNotNull(parcelTripA);
		assertEquals(mTripA, parcelTripA);
		TripUtils.assertFieldEquality(mTripA, parcelTripA);
	}


}