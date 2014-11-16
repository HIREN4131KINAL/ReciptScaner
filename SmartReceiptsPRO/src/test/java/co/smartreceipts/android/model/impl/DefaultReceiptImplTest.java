package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.math.BigDecimal;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.testutils.ReceiptUtils;
import co.smartreceipts.android.testutils.TestUtils;
import co.smartreceipts.android.testutils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class DefaultReceiptImplTest {

    Trip parent;
    DefaultReceiptImpl receipt1, receipt2, receipt3;

    @Before
    public void setUp() throws Exception {
        parent = TripUtils.newSpyOfDefaultTrip();
        final File img = ReceiptUtils.newMockedFile(ReceiptUtils.Constants.IMAGE_FILE_NAME);
        receipt1 = new DefaultReceiptImpl(ReceiptUtils.Constants.ID,
                ReceiptUtils.Constants.INDEX,
                parent,
                img,
                null, // TODO: Add Payment method
                ReceiptUtils.Constants.NAME,
                ReceiptUtils.Constants.CATEGORY,
                ReceiptUtils.Constants.COMMENT,
                new BigDecimal(ReceiptUtils.Constants.PRICE),
                new BigDecimal(ReceiptUtils.Constants.TAX),
                ReceiptUtils.Constants.CURRENCY,
                ReceiptUtils.Constants.DATE,
                ReceiptUtils.Constants.TIMEZONE,
                ReceiptUtils.Constants.IS_EXPENSABLE,
                ReceiptUtils.Constants.IS_FULLPAGE,
                ReceiptUtils.Constants.IS_SELECTED,
                Source.Undefined,
                ReceiptUtils.Constants.EXTRA1,
                ReceiptUtils.Constants.EXTRA2,
                ReceiptUtils.Constants.EXTRA3);
        receipt2 = new DefaultReceiptImpl(ReceiptUtils.Constants.ID,
                ReceiptUtils.Constants.INDEX,
                parent,
                img,
                null, // TODO: Add Payment method
                ReceiptUtils.Constants.NAME,
                ReceiptUtils.Constants.CATEGORY,
                ReceiptUtils.Constants.COMMENT,
                new BigDecimal(ReceiptUtils.Constants.PRICE),
                new BigDecimal(ReceiptUtils.Constants.TAX),
                ReceiptUtils.Constants.CURRENCY,
                ReceiptUtils.Constants.DATE,
                ReceiptUtils.Constants.TIMEZONE,
                ReceiptUtils.Constants.IS_EXPENSABLE,
                ReceiptUtils.Constants.IS_FULLPAGE,
                ReceiptUtils.Constants.IS_SELECTED,
                Source.Undefined,
                ReceiptUtils.Constants.EXTRA1,
                ReceiptUtils.Constants.EXTRA2,
                ReceiptUtils.Constants.EXTRA3);
        receipt3 = new DefaultReceiptImpl(-1, // Note: mismatched ID
                ReceiptUtils.Constants.INDEX,
                parent,
                null,
                null, // TODO: Add Payment method
                ReceiptUtils.Constants.NAME,
                ReceiptUtils.Constants.CATEGORY,
                ReceiptUtils.Constants.COMMENT,
                new BigDecimal(ReceiptUtils.Constants.PRICE),
                new BigDecimal(ReceiptUtils.Constants.TAX),
                ReceiptUtils.Constants.CURRENCY,
                ReceiptUtils.Constants.DATE,
                ReceiptUtils.Constants.TIMEZONE,
                ReceiptUtils.Constants.IS_EXPENSABLE,
                ReceiptUtils.Constants.IS_FULLPAGE,
                ReceiptUtils.Constants.IS_SELECTED,
                Source.Undefined,
                null, // Note: No Extras
                null, // Note: No Extras
                null); // Note: No Extras
    }

    @Test
    public void testId() {
        assertEquals(ReceiptUtils.Constants.ID, receipt1.getId());
    }

    @Test
    public void testIndex() {
        assertEquals(ReceiptUtils.Constants.INDEX, receipt1.getIndex());
    }

    @Test
    public void testTrip() {
        assertEquals(parent, receipt1.getTrip());
    }

    @Test
    public void testPaymentMethod() {
        // TODO: Add Payment Method
        assertNull(receipt1.getPaymentMethod());
        assertFalse(receipt1.hasPaymentMethod());
    }

    @Test
    public void testName() {
        assertEquals(ReceiptUtils.Constants.NAME, receipt1.getName());
    }

    @Test
    public void testCategory() {
        assertEquals(ReceiptUtils.Constants.CATEGORY, receipt1.getCategory());
    }

    @Test
    public void testComment() {
        assertEquals(ReceiptUtils.Constants.COMMENT, receipt1.getComment());
    }

    @Test
    public void testCurrency() {
        assertEquals(ReceiptUtils.Constants.CURRENCY, receipt1.getCurrency());
        assertEquals(ReceiptUtils.Constants.CURRENCY_CODE, receipt1.getCurrencyCode());
    }

    @Test
    public void testPriceAndCurrency() {
        assertEquals((float) ReceiptUtils.Constants.PRICE, receipt1.getPriceAsFloat(), TestUtils.EPSILON);
        assertEquals(ReceiptUtils.Constants.DECIMAL_FORMATTED_PRICE, receipt1.getDecimalFormattedPrice());
        assertEquals(ReceiptUtils.Constants.CURRENCY_FORMATTED_PRICE, receipt1.getCurrencyFormattedPrice());
    }

    @Test
    public void testTaxAndCurrency() {
        assertEquals((float) ReceiptUtils.Constants.TAX, receipt1.getTaxAsFloat(), TestUtils.EPSILON);
        assertEquals(ReceiptUtils.Constants.DECIMAL_FORMATTED_TAX, receipt1.getDecimalFormattedPrice());
        assertEquals(ReceiptUtils.Constants.CURRENCY_FORMATTED_TAX, receipt1.getCurrencyFormattedPrice());
    }

    @Test
    public void testDateAndTimeZone() {
        assertEquals(ReceiptUtils.Constants.DATE, receipt1.getDate());
        assertEquals(ReceiptUtils.Constants.DATE_MILLIS, receipt1.getDate().getTime());
        assertEquals(ReceiptUtils.Constants.TIMEZONE, receipt1.getTimeZone());
        assertEquals(ReceiptUtils.Constants.TIMEZONE_CODE, receipt1.getTimeZone().getID());
        assertEquals(ReceiptUtils.Constants.SLASH_FORMATTED_DATE, receipt1.getFormattedDate(Robolectric.application, "/"));
        assertEquals(ReceiptUtils.Constants.DASH_FORMATTED_DATE, receipt1.getFormattedDate(Robolectric.application, "-"));
    }

    @Test
    public void testIsFullPage() {
        assertEquals(ReceiptUtils.Constants.IS_FULLPAGE, receipt1.isFullPage());
    }

    @Test
    public void testIsExpensable() {
        assertEquals(ReceiptUtils.Constants.IS_EXPENSABLE, receipt1.isExpensable());
    }

    @Test
    public void testIsSelected() {
        assertEquals(ReceiptUtils.Constants.IS_SELECTED, receipt1.isExpensable());
    }

    @Test
    public void testConstructorFile() {
        assertNotNull(receipt1.getFile());
        assertTrue(receipt1.hasFile());
        assertTrue(receipt1.hasImage());
        assertFalse(receipt1.hasPDF());
        assertNull(receipt3.getFile());
        assertFalse(receipt3.hasFile());
        assertFalse(receipt3.hasImage());
        assertFalse(receipt3.hasPDF());
    }

    @Test
    public void testSetEmptyFile() {
        receipt1.setFile(null);
        assertNull(receipt1.getFile());
        assertFalse(receipt1.hasFile());
        assertFalse(receipt1.hasImage());
        assertFalse(receipt1.hasPDF());
    }

    @Test
    public void testSetImageFile() {
        final File img = ReceiptUtils.newMockedFile(ReceiptUtils.Constants.IMAGE_FILE_NAME);
        receipt1.setFile(img);
        assertNotNull(receipt1.getFile());
        assertEquals(img, receipt1.getFile());
        assertTrue(receipt1.hasFile());
        assertTrue(receipt1.hasImage());
        assertFalse(receipt1.hasPDF());
    }

    @Test
    public void testSetPDFFile() {
        final File pdf = ReceiptUtils.newMockedFile(ReceiptUtils.Constants.PDF_FILE_NAME);
        receipt1.setFile(pdf);
        assertNotNull(receipt1.getFile());
        assertEquals(pdf, receipt1.getFile());
        assertTrue(receipt1.hasFile());
        assertFalse(receipt1.hasImage());
        assertTrue(receipt1.hasPDF());
    }

    @Test
    public void testExtra1() {
        assertEquals(ReceiptUtils.Constants.EXTRA1, receipt1.getExtraEditText1());
        assertTrue(receipt1.hasExtraEditText1());
        assertNull(receipt3.getExtraEditText1());
        assertFalse(receipt3.hasExtraEditText1());
    }

    @Test
    public void testExtra2() {
        assertEquals(ReceiptUtils.Constants.EXTRA2, receipt1.getExtraEditText2());
        assertTrue(receipt1.hasExtraEditText2());
        assertNull(receipt3.getExtraEditText2());
        assertFalse(receipt3.hasExtraEditText2());
    }

    @Test
    public void testExtra3() {
        assertEquals(ReceiptUtils.Constants.EXTRA3, receipt1.getExtraEditText3());
        assertTrue(receipt1.hasExtraEditText3());
        assertNull(receipt3.getExtraEditText3());
        assertFalse(receipt3.hasExtraEditText3());
    }

    @Test
    public void testParcel() {
        final Parcel parcel = Parcel.obtain();
        receipt1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Receipt parceledReceipt = DefaultReceiptImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(parceledReceipt);
        assertEquals(receipt1, parceledReceipt);
        ReceiptUtils.assertFieldEqualityPlusIdAndIndex(parceledReceipt, receipt1);
    }

    @Test
    public void testHashCode() {
        assertEquals(receipt1.hashCode(), receipt2.hashCode());
        assertNotSame(receipt1.hashCode(), receipt3.hashCode());
    }

    @Test
    public void testEquals() {
        assertEquals(receipt1, receipt2);
        assertNotSame(receipt1, receipt3);
        ReceiptUtils.assertFieldEqualityPlusIdAndIndex(receipt1, receipt2);
    }

}
