package co.smartreceipts.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;
import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.tests.utils.TestUtils;
import co.smartreceipts.tests.utils.TripUtils;
import co.smartreceipts.tests.utils.TripUtils.Constants;

@Config(emulateSdk = 18) 
@RunWith(RobolectricTestRunner.class)
public class TripRowTest {

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
				.setDefaultCurrency(Constants.CURRENCY_CODE)
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
				.setDefaultCurrency(Constants.CURRENCY_CODE)
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
		assertEquals(mTripRowA.getPriceAsFloat(), mTripRowB.getPriceAsFloat(), TestUtils.EPSILON);
		assertEquals(mTripRowA.getDecimalFormattedPrice(), mTripRowB.getDecimalFormattedPrice());
		assertEquals(mTripRowA.getCurrencyCode(), mTripRowB.getCurrencyCode());
		assertEquals(mTripRowA.getCurrencyFormattedPrice(), mTripRowB.getCurrencyFormattedPrice());
		assertEquals(mTripRowA.getPrice(), Constants.PRICE);
		assertEquals(mTripRowA.getCurrencyCode(), Constants.CURRENCY_CODE);
	}

	@Test
	public void testTripRowMileage() {
		assertEquals(mTripRowA.getMileage(), mTripRowB.getMileage(), TestUtils.EPSILON);
		assertEquals(mTripRowA.getMilesAsString(), mTripRowB.getMilesAsString());
		assertEquals(mTripRowA.getMileage(), Constants.MILEAGE, TestUtils.EPSILON);
	}
	
	@Test
	public void parcelTest() {
		Parcel parcelA = Parcel.obtain();
		mTripRowA.writeToParcel(parcelA, 0);
		parcelA.setDataPosition(0);
		TripRow parcelTripRowA = TripRow.CREATOR.createFromParcel(parcelA);
		assertNotNull(parcelTripRowA);
		assertEquals(mTripRowA, parcelTripRowA);
		TripUtils.assertFieldEquality(mTripRowA, parcelTripRowA);
	}


}