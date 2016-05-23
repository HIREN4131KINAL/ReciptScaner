package co.smartreceipts.android.utils;

import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ReceiptUtils {

    public static class Constants {
        public static final int ID = 0;
        public static final int INDEX = 4;
        public static final String CATEGORY = "Lunch";
        public static final String COMMENT = "";
        public static final String CURRENCY_CODE = "USD";
        public static final WBCurrency CURRENCY = WBCurrency.getInstance(CURRENCY_CODE);
        public static final long DATE_MILLIS = 1409703721000L; // 09/02/2014 @ 8:22EDT
        public static final Date DATE = new Date(DATE_MILLIS);
        public static final String SLASH_FORMATTED_DATE = "Sep/02/2014";
        public static final String DASH_FORMATTED_DATE = "Sep-02-2014";
        public static final String EXTRA1 = "extra1";
        public static final String EXTRA2 = "extra2";
        public static final String EXTRA3 = "extra3";
        public static final File IMAGE_FILE = new File("/Android/data/wb.receipts/files/Report/img.jpg");
        public static final String IMAGE_FILE_NAME = "img.jpg";
        public static final File PDF_FILE = new File("/Android/data/wb.receipts/files/Report/pdf.pdf");
        public static final String PDF_FILE_NAME = "pdf.pdf";
        public static final boolean IS_EXPENSABLE = true;
        public static final boolean IS_FULLPAGE = false;
        public static final boolean IS_SELECTED = true;
        public static final String NAME = "Name";
        public static final double PRICE = 12.55d;
        public static final String DECIMAL_FORMATTED_PRICE = "12.55";
        public static final String CURRENCY_FORMATTED_PRICE = "$12.55";
        public static final double TAX = 0.37d;
        public static final String DECIMAL_FORMATTED_TAX = "0.37";
        public static final String CURRENCY_FORMATTED_TAX = "$0.37";
        public static final TimeZone TIMEZONE = TimeZone.getDefault();
        public static final String TIMEZONE_CODE = TIMEZONE.getID();
    }

    public static ReceiptBuilderFactory newDefaultReceiptBuilderFactory() {
        final File img = createRoboElectricStubFile(Constants.IMAGE_FILE_NAME);
        final ReceiptBuilderFactory factory = new ReceiptBuilderFactory(Constants.ID);
        factory.setTrip(TripUtils.newDefaultTrip());
        factory.setName(Constants.NAME);
        factory.setPrice(Constants.PRICE);
        factory.setTax(Constants.TAX);
        factory.setCurrency(Constants.CURRENCY);
        factory.setDate(Constants.DATE);
        factory.setTimeZone(Constants.TIMEZONE);
        factory.setCategory(Constants.CATEGORY);
        factory.setComment(Constants.COMMENT);
        factory.setIsExpenseable(Constants.IS_EXPENSABLE);
        factory.setIsFullPage(Constants.IS_FULLPAGE);
        factory.setImage(img);
        factory.setIndex(Constants.INDEX);
        factory.setExtraEditText1(Constants.EXTRA1);
        factory.setExtraEditText2(Constants.EXTRA2);
        factory.setExtraEditText3(Constants.EXTRA3);
        // TODO: Add Payment Method DefaultBuilderHere
        return factory;
    }

    public static Receipt newDefaultReceipt() {
        return newDefaultReceiptBuilderFactory().build();
    }

    public static File createRoboElectricStubFile(String filename) {
        final File root = RuntimeEnvironment.application.getExternalFilesDir(null);
        final File newFile = new File(root, filename);
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            return newFile;
        }
        catch (IOException e) {
            return root; // Stub fallback case
        }
    }

    public static void assertFieldEquality(Receipt receipt1, Receipt receipt2) {
        assertFieldEqualityHelper(receipt1, receipt2);
        assertEquals(receipt1.getTrip(), receipt2.getTrip());
        assertEquals(receipt1.getDate(), receipt2.getDate());
    }

    public static void assertFieldEqualityIgnoringParent(Receipt receipt1, Receipt receipt2) {
        assertFieldEqualityHelper(receipt1, receipt2);
        assertEquals(receipt1.getDate(), receipt2.getDate());
    }

    public static void assertFieldEqualityWithDateFuzzing(Receipt receipt1, Receipt receipt2) {
        assertFieldEqualityHelper(receipt1, receipt2);
        assertEquals(receipt1.getTrip(), receipt2.getTrip());
        assertTrue(Math.abs(receipt1.getDate().getTime() - receipt2.getDate().getTime()) < 100L);
    }

    private static void assertFieldEqualityHelper(Receipt receipt1, Receipt receipt2) {
        assertEquals(receipt1.getComment(), receipt2.getComment());
        assertEquals(receipt1.getCategory(), receipt2.getCategory());
        assertEquals(receipt1.getExtraEditText1(), receipt2.getExtraEditText1());
        assertEquals(receipt1.getExtraEditText2(), receipt2.getExtraEditText2());
        assertEquals(receipt1.getExtraEditText3(), receipt2.getExtraEditText3());
        assertEquals(receipt1.getFile(), receipt2.getFile());
        assertEquals(receipt1.isExpensable(), receipt2.isExpensable());
        assertEquals(receipt1.isFullPage(), receipt2.isFullPage());
        assertEquals(receipt1.isSelected(), receipt2.isSelected());
        assertEquals(receipt1.getName(), receipt2.getName());
        assertEquals(receipt1.getPrice(), receipt2.getPrice());
        assertEquals(receipt1.getTax(), receipt2.getTax());
        assertEquals(receipt1.getTimeZone(), receipt2.getTimeZone());
        assertEquals(receipt1.getPaymentMethod(), receipt2.getPaymentMethod());
    }

    /**
     * We do not guarantee that indices are set, but this test still performs the full set
     */
    public static void assertFieldEqualityPlusIdAndIndex(Receipt receipt1, Receipt receipt2) {
        assertFieldEquality(receipt1, receipt2);
        assertEquals(receipt1.getId(), receipt2.getId());
        assertEquals(receipt1.getIndex(), receipt2.getIndex());
    }

    public static void assertFieldEqualityWithDateFuzzingPlusIdAndIndex(Receipt receipt1, Receipt receipt2) {
        assertFieldEqualityWithDateFuzzing(receipt1, receipt2);
        assertEquals(receipt1.getId(), receipt2.getId());
        assertEquals(receipt1.getIndex(), receipt2.getIndex());
    }

}
