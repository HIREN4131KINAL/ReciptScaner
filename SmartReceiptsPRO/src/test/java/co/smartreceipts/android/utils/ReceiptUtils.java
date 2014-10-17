package co.smartreceipts.android.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.model.WBCurrency;

public class ReceiptUtils {

	public static class Constants {
		public static final int ID = 0;
		public static final String CATEGORY = "Lunch";
		public static final String COMMENT = "";
		public static final String CURRENCY_CODE = "USD";
		public static final WBCurrency CURRENCY = WBCurrency.getInstance(CURRENCY_CODE); // 1
		public static final long DATE_MILLIS = System.currentTimeMillis();
		public static final Date DATE = new Date(DATE_MILLIS);
		public static final String EXTRA1 = "extra1";
		public static final String EXTRA2 = "";
		public static final String EXTRA3 = "";
		public static final File IMAGE_FILE = new File("/Android/data/wb.receipts/files/Report/img.jpg");
		public static final String IMAGE_FILE_NAME = "img.jpg";
		public static final File PDF_FILE = new File("/Android/data/wb.receipts/files/Report/pdf.pdf");
		public static final String PDF_FILE_NAME = "pdf.pdf";
		public static final boolean IS_EXPENSABLE = true;
		public static final boolean IS_FULLPAGE = false;
		public static final String NAME = "Name";
		public static final String PRICE = "12.55";
		public static final double PRICE_DOUBLE = 12.55d;
		public static final String TAX = "0.37";
		public static final double TAX_DOUBLE = 0.37d;
		public static final TimeZone TIMEZONE = TimeZone.getDefault();
		public static final String TIMEZONE_CODE = TIMEZONE.getID();
	}

	public static void assertFieldEquality(ReceiptRow receipt1, ReceiptRow receipt2) {
		assertEquals(receipt1.getComment(), receipt2.getComment());
		assertEquals(receipt1.getCategory(), receipt2.getCategory());
		assertEquals(receipt1.getCurrencyCode(), receipt2.getCurrencyCode());
		assertTrue(Math.abs(receipt1.getDate().getTime() - receipt2.getDate().getTime()) < 100); // Allow dates to be
																									// w/i 100ms of each
																									// other
		assertEquals(receipt1.getExtraEditText1(), receipt2.getExtraEditText1());
		assertEquals(receipt1.getExtraEditText2(), receipt2.getExtraEditText2());
		assertEquals(receipt1.getExtraEditText3(), receipt2.getExtraEditText3());
		assertEquals(receipt1.getFile(), receipt2.getFile());
		assertEquals(receipt1.getName(), receipt2.getName());
		assertEquals(receipt1.getPriceAsFloat(), receipt2.getPriceAsFloat(), TestUtils.EPSILON);
		assertEquals(receipt1.getTaxAsFloat(), receipt2.getTaxAsFloat(), TestUtils.EPSILON);
		assertEquals(receipt1.getTimeZone(), receipt2.getTimeZone());
		assertEquals(receipt1.getTrip(), receipt2.getTrip());
		assertEquals(receipt1.getPaymentMethod(), receipt2.getPaymentMethod());
	}

	/**
	 * We do not guarantee that indices are set, but this test still performs the full set
	 */
	public static void assertFieldEqualityPlusIdAndIndex(ReceiptRow receipt1, ReceiptRow receipt2) {
		assertFieldEquality(receipt1, receipt2);
		assertEquals(receipt1.getId(), receipt2.getId());
		assertEquals(receipt1.getIndex(), receipt2.getIndex());
	}

}
