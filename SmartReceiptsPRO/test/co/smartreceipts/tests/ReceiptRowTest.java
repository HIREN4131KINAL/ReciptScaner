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

import android.text.TextUtils;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.model.WBCurrency;

@RunWith(RobolectricTestRunner.class)
public class ReceiptRowTest {

	private static final float EPSILON = 0.0001f;

	private static class Constants {
		public static final int ID = 0;
		public static final String CATEGORY = "Lunch";
		public static final String COMMENT = "";
		public static final String CURRENCY_CODE = "USD";
		public static final WBCurrency CURRENCY = WBCurrency.getInstance(CURRENCY_CODE); //1
		public static final long DATE_MILLIS = System.currentTimeMillis();
		public static final Date DATE = new Date(DATE_MILLIS);
		public static final String EXTRA1 = "extra1";
		public static final String EXTRA2 = "";
		public static final String EXTRA3 = "";
		public static final File IMAGE_FILE = new File("/Android/data/wb.receipts/files/Report/img.png");
		public static final File PDF_FILE = new File("/Android/data/wb.receipts/files/Report/pdf.pdf");
		public static final boolean IS_EXPENSABLE = true;
		public static final boolean IS_FULLPAGE = false;
		public static final String NAME = "Name";
		public static final String PRICE = "12.55";
		public static final String TAX = "0.37";
		public static final TimeZone TIMEZONE = TimeZone.getDefault();
		public static final String TIMEZONE_CODE = TIMEZONE.getID();
	}

	/*
	 * ReceiptRowA and ReceiptRowB should be expected as having all member variables be equal.
	 * The .equals method for ReceiptRow only tests their IDs and will not be a valid indicator
	 * of whether or not they are actually equal or not.
	 * The difference between the two is how their builders are constructed
	 */
	private ReceiptRow mReceiptRowA, mReceiptRowB;

	/*
	 * ReceiptRowC has a difference File (PDF) than ReceiptRow A or B. All other data is the same
	 */
	private ReceiptRow mReceiptRowC;

	/**
	 * Generates a builder for mReceiptRowA. This builder user primitives/Strings
	 * whenever possible as opposed to higher level objects
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
				.setFile(Constants.IMAGE_FILE)
				.setIsExpenseable(Constants.IS_EXPENSABLE)
				.setIsFullPage(Constants.IS_FULLPAGE)
				.setName(Constants.NAME)
				.setPrice(Constants.PRICE)
				.setTax(Constants.TAX)
				.setTimeZone(Constants.TIMEZONE_CODE);
		return builderA;
	}

	/**
	 * Generates a builder for mReceiptRowB. This builder users higher level objects
	 * as opposed to primitives/Strings whenever possible
	 * @return
	 */
	private ReceiptRow.Builder getReceiptRowBBuilder() {
		ReceiptRow.Builder builderB = new ReceiptRow.Builder(Constants.ID);
		builderB.setCategory(Constants.CATEGORY)
				.setComment(Constants.COMMENT)
				.setCurrency(Constants.CURRENCY)
				.setDate(Constants.DATE)
				.setExtraEditText1(Constants.EXTRA1)
				.setExtraEditText2(Constants.EXTRA2)
				.setExtraEditText3(Constants.EXTRA3)
				.setImage(Constants.IMAGE_FILE)
				.setIsExpenseable(Constants.IS_EXPENSABLE)
				.setIsFullPage(Constants.IS_FULLPAGE)
				.setName(Constants.NAME)
				.setPrice(Constants.PRICE)
				.setTax(Constants.TAX)
				.setTimeZone(Constants.TIMEZONE);
		return builderB;
	}

	/**
	 * Generates a builder for mReceiptRowC. This uses receiptRow A's builder
	 * but swaps out the image file stub with a pdf stub
	 * @return
	 */
	private ReceiptRow.Builder getReceiptRowCBuilder() {
		ReceiptRow.Builder builderC = getReceiptRowABuilder();
		builderC.setPDF(Constants.PDF_FILE);
		return builderC;
	}

	@Before
	public void setUp() throws Exception {
		mReceiptRowA = getReceiptRowABuilder().build();
		mReceiptRowB = getReceiptRowBBuilder().build();
		mReceiptRowC = getReceiptRowCBuilder().build();
	}

	@After
	public void tearDown() throws Exception {
		mReceiptRowA = null;
		mReceiptRowB = null;
		mReceiptRowC = null;
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
		//TODO: Add Context to get Formatted Dates
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
		assertEquals(mReceiptRowA.getFile(), Constants.IMAGE_FILE);
		assertEquals(mReceiptRowC.getFile(), Constants.PDF_FILE);
	}

	@Test
	public void testReceiptRowPriceAndCurrency() {
		assertEquals(mReceiptRowA.getPrice(), mReceiptRowB.getPrice());
		assertEquals(mReceiptRowA.getPriceAsFloat(), mReceiptRowB.getPriceAsFloat(), EPSILON);
		assertEquals(mReceiptRowA.getDecimalFormattedPrice(), mReceiptRowB.getDecimalFormattedPrice());
		assertEquals(mReceiptRowA.getCurrencyCode(), mReceiptRowB.getCurrencyCode());
		assertEquals(mReceiptRowA.getCurrencyFormattedPrice(), mReceiptRowB.getCurrencyFormattedPrice());
		assertEquals(mReceiptRowA.getPrice(), Constants.PRICE);
		assertEquals(mReceiptRowA.getCurrencyCode(), Constants.CURRENCY_CODE);
	}

	@Test
	public void testReceiptRowTaxAndCurrency() {
		assertEquals(mReceiptRowA.getTax(), mReceiptRowB.getTax());
		assertEquals(mReceiptRowA.getTaxAsFloat(), mReceiptRowB.getTaxAsFloat(), EPSILON);
		assertEquals(mReceiptRowA.getDecimalFormattedTax(), mReceiptRowB.getDecimalFormattedTax());
		assertEquals(mReceiptRowA.getCurrencyCode(), mReceiptRowB.getCurrencyCode());
		assertEquals(mReceiptRowA.getCurrencyFormattedTax(), mReceiptRowB.getCurrencyFormattedTax());
		assertEquals(mReceiptRowA.getTax(), Constants.TAX);
		assertEquals(mReceiptRowA.getCurrencyCode(), Constants.CURRENCY_CODE);
	}

}