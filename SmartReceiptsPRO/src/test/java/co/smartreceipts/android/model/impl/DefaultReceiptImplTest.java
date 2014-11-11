package co.smartreceipts.android.model.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Parcel;
import android.text.TextUtils;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.tests.utils.ReceiptUtils;
import co.smartreceipts.tests.utils.ReceiptUtils.Constants;
import co.smartreceipts.tests.utils.TestUtils;
import co.smartreceipts.tests.utils.TripUtils;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class DefaultReceiptImplTest {

	/*
	 * ReceiptRowA and ReceiptRowB should be expected as having all member variables be equal. The .equals method for
	 * ReceiptRow only tests their IDs and will not be a valid indicator of whether or not they are actually equal or
	 * not. The difference between the two is how their builders are constructed
	 */
	private Receipt mReceiptA, mReceiptB;

	/*
	 * ReceiptRowC has a difference File (PDF) than ReceiptRow A or B. All other data is the same
	 */
	private Receipt mReceiptC;

	private SmartReceiptsApplication mApp;

	/**
	 * Generates a builder for mReceiptRowA. This builder user primitives/Strings whenever possible as opposed to higher
	 * level objects
	 * 
	 * @return
	 */
	private Receipt.Builder getReceiptRowABuilder() {
		Receipt.Builder builderA = new Receipt.Builder(Constants.ID);
		builderA.setCategory(Constants.CATEGORY).setComment(Constants.COMMENT).setCurrency(Constants.CURRENCY_CODE)
				.setDate(Constants.DATE_MILLIS).setExtraEditText1(Constants.EXTRA1).setExtraEditText2(Constants.EXTRA2)
				.setExtraEditText3(Constants.EXTRA3).setFile(getFile(Constants.IMAGE_FILE_NAME))
				.setIsExpenseable(Constants.IS_EXPENSABLE).setIsFullPage(Constants.IS_FULLPAGE).setName(Constants.NAME)
				.setPrice(Constants.PRICE_DOUBLE).setTax(Constants.TAX).setTimeZone(Constants.TIMEZONE_CODE)
				.setTrip(TripUtils.newDefaultTripRowInstance());
		return builderA;
	}

	/**
	 * Generates a builder for mReceiptRowB. This builder users higher level objects as opposed to primitives/Strings
	 * whenever possible
	 * 
	 * @return
	 */
	private Receipt.Builder getReceiptRowBBuilder() {
		Receipt.Builder builderB = new Receipt.Builder(Constants.ID);
		mApp.getPersistenceManager().getStorageManager().createFile(Constants.IMAGE_FILE);
		builderB.setCategory(Constants.CATEGORY).setComment(Constants.COMMENT).setCurrency(Constants.CURRENCY)
				.setDate(Constants.DATE).setExtraEditText1(Constants.EXTRA1).setExtraEditText2(Constants.EXTRA2)
				.setExtraEditText3(Constants.EXTRA3).setFile(getFile(Constants.IMAGE_FILE_NAME))
				.setIsExpenseable(Constants.IS_EXPENSABLE).setIsFullPage(Constants.IS_FULLPAGE).setName(Constants.NAME)
				.setPrice(Constants.PRICE_DOUBLE).setTax(Constants.TAX_DOUBLE).setTimeZone(Constants.TIMEZONE)
				.setTrip(TripUtils.newDefaultTripRowInstance());
		return builderB;
	}

	/**
	 * Generates a builder for mReceiptRowC. This uses receiptRow A's builder but swaps out the image file stub with a
	 * pdf stub
	 * 
	 * @return
	 */
	private Receipt.Builder getReceiptRowCBuilder() {
		Receipt.Builder builderC = getReceiptRowABuilder();
		mApp.getPersistenceManager().getStorageManager().createFile(Constants.PDF_FILE);
		builderC.setPDF(getFile(Constants.PDF_FILE_NAME));
		return builderC;
	}

	private File getFile(String name) {
		File tripDir = mApp.getPersistenceManager().getStorageManager()
				.mkdir(co.smartreceipts.tests.utils.TripUtils.Constants.DIRECTORY_NAME);
		File file = new File(tripDir, name);
		mApp.getPersistenceManager().getStorageManager().createFile(file);
		return file;
	}

	@Before
	public void setUp() throws Exception {
		mApp = (SmartReceiptsApplication) Robolectric.application;
		mReceiptA = getReceiptRowABuilder().build();
		mReceiptB = getReceiptRowBBuilder().build();
		mReceiptC = getReceiptRowCBuilder().build();
	}

	@After
	public void tearDown() throws Exception {
		mReceiptA = null;
		mReceiptB = null;
		mReceiptC = null;
		mApp = null;
	}

	@Test
	public void fileCreationSuccess() {
		assertTrue(getFile(Constants.IMAGE_FILE_NAME).exists());
		assertTrue(getFile(Constants.PDF_FILE_NAME).exists());
	}

	@Test
	public void testReceiptRowEquality() {
		assertEquals(mReceiptA, mReceiptB);
		assertEquals(mReceiptA, mReceiptC);
	}

	@Test
	public void testReceiptRowCategories() {
		assertEquals(mReceiptA.getCategory(), mReceiptB.getCategory());
		assertEquals(mReceiptA.getCategory(), Constants.CATEGORY);
	}

	@Test
	public void testReceiptRowComments() {
		assertEquals(mReceiptA.getComment(), mReceiptB.getComment());
		assertEquals(mReceiptA.getComment(), Constants.COMMENT);
	}

	@Test
	public void testReceiptRowDates() {
		assertEquals(mReceiptA.getDate(), mReceiptB.getDate());
		assertEquals(mReceiptA.getTimeZone(), mReceiptB.getTimeZone());
		assertEquals(mReceiptA.getDate(), Constants.DATE);
		assertEquals(mReceiptA.getTimeZone(), Constants.TIMEZONE);
		// TODO: Add Context to get Formatted Dates
	}

	@Test
	public void testReceiptRowExtras() {
		assertEquals(mReceiptA.getExtraEditText1(), mReceiptB.getExtraEditText1());
		assertEquals(mReceiptA.getExtraEditText2(), mReceiptB.getExtraEditText2());
		assertEquals(mReceiptA.getExtraEditText3(), mReceiptB.getExtraEditText3());
		testExtra(mReceiptA.getExtraEditText1(), Constants.EXTRA1);
		testExtra(mReceiptA.getExtraEditText2(), Constants.EXTRA2);
		testExtra(mReceiptA.getExtraEditText3(), Constants.EXTRA3);
	}

	/**
	 * Extra Edit Texts get set to null if they are empty
	 * 
	 * @param extra
	 * @param constant
	 */
	private void testExtra(String extra, String constant) {
		if (TextUtils.isEmpty(constant)) {
			assertEquals(extra, null);
		}
		else {
			assertEquals(extra, constant);
		}
	}

	@Test
	public void testReceiptRowFiles() {
		assertEquals(mReceiptA.getFile(), mReceiptB.getFile());
		assertEquals(mReceiptA.getFileName(), mReceiptB.getFileName());
		assertEquals(mReceiptA.getFilePath(), mReceiptB.getFilePath());
		assertEquals(mReceiptA.hasImage(), mReceiptB.hasImage());
		assertEquals(mReceiptA.getFile(), getFile(Constants.IMAGE_FILE_NAME));
		assertEquals(mReceiptC.getFile(), getFile(Constants.PDF_FILE_NAME));
	}

	@Test
	public void testReceiptRowPriceAndCurrency() {
		assertNotNull(mReceiptA.getPrice());
		assertEquals(mReceiptA.getPrice(), mReceiptB.getPrice());
		assertEquals(mReceiptA.getPriceAsFloat(), mReceiptB.getPriceAsFloat(), TestUtils.EPSILON);
		assertEquals(mReceiptA.getDecimalFormattedPrice(), mReceiptB.getDecimalFormattedPrice());
		assertEquals(mReceiptA.getCurrencyCode(), mReceiptB.getCurrencyCode());
		assertEquals(mReceiptA.getCurrencyFormattedPrice(), mReceiptB.getCurrencyFormattedPrice());
		assertEquals(mReceiptA.getPrice(), Constants.PRICE);
		assertEquals(mReceiptA.getCurrencyCode(), Constants.CURRENCY_CODE);
	}

	@Test
	public void testReceiptRowTaxAndCurrency() {
		assertNotNull(mReceiptA.getTax());
		assertEquals(mReceiptA.getTax(), mReceiptB.getTax());
		assertEquals(mReceiptA.getTaxAsFloat(), mReceiptB.getTaxAsFloat(), TestUtils.EPSILON);
		assertEquals(mReceiptA.getDecimalFormattedTax(), mReceiptB.getDecimalFormattedTax());
		assertEquals(mReceiptA.getCurrencyCode(), mReceiptB.getCurrencyCode());
		assertEquals(mReceiptA.getCurrencyFormattedTax(), mReceiptB.getCurrencyFormattedTax());
		assertEquals(mReceiptA.getTax(), Constants.TAX);
		assertEquals(mReceiptA.getCurrencyCode(), Constants.CURRENCY_CODE);
	}

	@Test
	public void testTripRow() {
		assertEquals(mReceiptA.getTrip(), mReceiptB.getTrip());
	}

	@Test
	public void parcelTest() {
		Parcel parcelA = Parcel.obtain();
		mReceiptA.writeToParcel(parcelA, 0);
		parcelA.setDataPosition(0);
		Receipt parcelReceiptA = Receipt.CREATOR.createFromParcel(parcelA);
		assertNotNull(parcelReceiptA);
		assertEquals(mReceiptA, parcelReceiptA);
		ReceiptUtils.assertFieldEquality(parcelReceiptA, mReceiptA);

		Parcel parcelD = Parcel.obtain();
		Receipt receiptD = getReceiptRowABuilder().setFile(null).build();
		receiptD.writeToParcel(parcelD, 0);
		parcelD.setDataPosition(0);
		Receipt parcelReceiptD = Receipt.CREATOR.createFromParcel(parcelD);
		assertNotNull(parcelReceiptD);
		assertEquals(receiptD, parcelReceiptD);
		ReceiptUtils.assertFieldEqualityPlusIdAndIndex(parcelReceiptD, receiptD);
	}

}
