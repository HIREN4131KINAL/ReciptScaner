package co.smartreceipts.tests;

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
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.tests.utils.ReceiptUtils;
import co.smartreceipts.tests.utils.TripUtils;
import co.smartreceipts.tests.utils.ReceiptUtils.Constants;
import co.smartreceipts.tests.utils.TestUtils;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ReceiptRowTest {

	/*
	 * ReceiptRowA and ReceiptRowB should be expected as having all member variables be equal. The .equals method for
	 * ReceiptRow only tests their IDs and will not be a valid indicator of whether or not they are actually equal or
	 * not. The difference between the two is how their builders are constructed
	 */
	private ReceiptRow mReceiptRowA, mReceiptRowB;

	/*
	 * ReceiptRowC has a difference File (PDF) than ReceiptRow A or B. All other data is the same
	 */
	private ReceiptRow mReceiptRowC;

	private SmartReceiptsApplication mApp;

	/**
	 * Generates a builder for mReceiptRowA. This builder user primitives/Strings whenever possible as opposed to higher
	 * level objects
	 * 
	 * @return
	 */
	private ReceiptRow.Builder getReceiptRowABuilder() {
		ReceiptRow.Builder builderA = new ReceiptRow.Builder(Constants.ID);
		builderA.setCategory(Constants.CATEGORY)
				.setComment(Constants.COMMENT)
				.setCurrency(Constants.CURRENCY_CODE)
				.setDate(Constants.DATE_MILLIS)
				.setExtraEditText1(Constants.EXTRA1)
				.setExtraEditText2(Constants.EXTRA2)
				.setExtraEditText3(Constants.EXTRA3)
				.setFile(getFile(Constants.IMAGE_FILE_NAME))
				.setIsExpenseable(Constants.IS_EXPENSABLE)
				.setIsFullPage(Constants.IS_FULLPAGE)
				.setName(Constants.NAME)
				.setPrice(Constants.PRICE)
				.setTax(Constants.TAX)
				.setTimeZone(Constants.TIMEZONE_CODE)
				.setTrip(TripUtils.getDefaultTripRow());
		return builderA;
	}

	/**
	 * Generates a builder for mReceiptRowB. This builder users higher level objects as opposed to primitives/Strings
	 * whenever possible
	 * 
	 * @return
	 */
	private ReceiptRow.Builder getReceiptRowBBuilder() {
		ReceiptRow.Builder builderB = new ReceiptRow.Builder(Constants.ID);
		mApp.getPersistenceManager().getStorageManager().createFile(Constants.IMAGE_FILE);
		builderB.setCategory(Constants.CATEGORY)
				.setComment(Constants.COMMENT)
				.setCurrency(Constants.CURRENCY)
				.setDate(Constants.DATE)
				.setExtraEditText1(Constants.EXTRA1)
				.setExtraEditText2(Constants.EXTRA2)
				.setExtraEditText3(Constants.EXTRA3)
				.setFile(getFile(Constants.IMAGE_FILE_NAME))
				.setIsExpenseable(Constants.IS_EXPENSABLE)
				.setIsFullPage(Constants.IS_FULLPAGE)
				.setName(Constants.NAME)
				.setPrice(Constants.PRICE_DOUBLE)
				.setTax(Constants.TAX_DOUBLE)
				.setTimeZone(Constants.TIMEZONE)
				.setTrip(TripUtils.getDefaultTripRow());
		return builderB;
	}

	/**
	 * Generates a builder for mReceiptRowC. This uses receiptRow A's builder but swaps out the image file stub with a
	 * pdf stub
	 * 
	 * @return
	 */
	private ReceiptRow.Builder getReceiptRowCBuilder() {
		ReceiptRow.Builder builderC = getReceiptRowABuilder();
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
		mReceiptRowA = getReceiptRowABuilder().build();
		mReceiptRowB = getReceiptRowBBuilder().build();
		mReceiptRowC = getReceiptRowCBuilder().build();
	}

	@After
	public void tearDown() throws Exception {
		mReceiptRowA = null;
		mReceiptRowB = null;
		mReceiptRowC = null;
		mApp = null;
	}

	@Test
	public void fileCreationSuccess() {
		assertTrue(getFile(Constants.IMAGE_FILE_NAME).exists());
		assertTrue(getFile(Constants.PDF_FILE_NAME).exists());
	}

	@Test
	public void testReceiptRowEquality() {
		assertEquals(mReceiptRowA, mReceiptRowB);
		assertEquals(mReceiptRowA, mReceiptRowC);
	}

	@Test
	public void testReceiptRowCategories() {
		assertEquals(mReceiptRowA.getCategory(), mReceiptRowB.getCategory());
		assertEquals(mReceiptRowA.getCategory(), Constants.CATEGORY);
	}

	@Test
	public void testReceiptRowComments() {
		assertEquals(mReceiptRowA.getComment(), mReceiptRowB.getComment());
		assertEquals(mReceiptRowA.getComment(), Constants.COMMENT);
	}

	@Test
	public void testReceiptRowDates() {
		assertEquals(mReceiptRowA.getDate(), mReceiptRowB.getDate());
		assertEquals(mReceiptRowA.getTimeZone(), mReceiptRowB.getTimeZone());
		assertEquals(mReceiptRowA.getDate(), Constants.DATE);
		assertEquals(mReceiptRowA.getTimeZone(), Constants.TIMEZONE);
		// TODO: Add Context to get Formatted Dates
	}

	@Test
	public void testReceiptRowExtras() {
		assertEquals(mReceiptRowA.getExtraEditText1(), mReceiptRowB.getExtraEditText1());
		assertEquals(mReceiptRowA.getExtraEditText2(), mReceiptRowB.getExtraEditText2());
		assertEquals(mReceiptRowA.getExtraEditText3(), mReceiptRowB.getExtraEditText3());
		testExtra(mReceiptRowA.getExtraEditText1(), Constants.EXTRA1);
		testExtra(mReceiptRowA.getExtraEditText2(), Constants.EXTRA2);
		testExtra(mReceiptRowA.getExtraEditText3(), Constants.EXTRA3);
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
		assertEquals(mReceiptRowA.getFile(), mReceiptRowB.getFile());
		assertEquals(mReceiptRowA.getFileName(), mReceiptRowB.getFileName());
		assertEquals(mReceiptRowA.getFilePath(), mReceiptRowB.getFilePath());
		assertEquals(mReceiptRowA.hasImage(), mReceiptRowB.hasImage());
		assertEquals(mReceiptRowA.getFile(), getFile(Constants.IMAGE_FILE_NAME));
		assertEquals(mReceiptRowC.getFile(), getFile(Constants.PDF_FILE_NAME));
	}

	@Test
	public void testReceiptRowPriceAndCurrency() {
		assertEquals(mReceiptRowA.getPrice(), mReceiptRowB.getPrice());
		assertEquals(mReceiptRowA.getPriceAsFloat(), mReceiptRowB.getPriceAsFloat(), TestUtils.EPSILON);
		assertEquals(mReceiptRowA.getDecimalFormattedPrice(), mReceiptRowB.getDecimalFormattedPrice());
		assertEquals(mReceiptRowA.getCurrencyCode(), mReceiptRowB.getCurrencyCode());
		assertEquals(mReceiptRowA.getCurrencyFormattedPrice(), mReceiptRowB.getCurrencyFormattedPrice());
		assertEquals(mReceiptRowA.getPrice(), Constants.PRICE);
		assertEquals(mReceiptRowA.getCurrencyCode(), Constants.CURRENCY_CODE);
	}

	@Test
	public void testReceiptRowTaxAndCurrency() {
		assertEquals(mReceiptRowA.getTax(), mReceiptRowB.getTax());
		assertEquals(mReceiptRowA.getTaxAsFloat(), mReceiptRowB.getTaxAsFloat(), TestUtils.EPSILON);
		assertEquals(mReceiptRowA.getDecimalFormattedTax(), mReceiptRowB.getDecimalFormattedTax());
		assertEquals(mReceiptRowA.getCurrencyCode(), mReceiptRowB.getCurrencyCode());
		assertEquals(mReceiptRowA.getCurrencyFormattedTax(), mReceiptRowB.getCurrencyFormattedTax());
		assertEquals(mReceiptRowA.getTax(), Constants.TAX);
		assertEquals(mReceiptRowA.getCurrencyCode(), Constants.CURRENCY_CODE);
	}

	@Test
	public void testTripRow() {
		assertEquals(mReceiptRowA.getTrip(), mReceiptRowB.getTrip());
	}
	
	@Test
	public void parcelTest() {
		Parcel parcelA = Parcel.obtain();
		mReceiptRowA.writeToParcel(parcelA, 0);
		parcelA.setDataPosition(0);
		ReceiptRow parcelReceiptRowA = ReceiptRow.CREATOR.createFromParcel(parcelA);
		assertNotNull(parcelReceiptRowA);
		assertEquals(mReceiptRowA, parcelReceiptRowA);
		ReceiptUtils.assertFieldEquality(parcelReceiptRowA, mReceiptRowA);

		Parcel parcelD = Parcel.obtain();
		ReceiptRow receiptRowD = getReceiptRowABuilder().setFile(null).build();
		receiptRowD.writeToParcel(parcelD, 0);
		parcelD.setDataPosition(0);
		ReceiptRow parcelReceiptRowD = ReceiptRow.CREATOR.createFromParcel(parcelD);
		assertNotNull(parcelReceiptRowD);
		assertEquals(receiptRowD, parcelReceiptRowD);
		ReceiptUtils.assertFieldEqualityPlusIdAndIndex(parcelReceiptRowD, receiptRowD);
	}

}
