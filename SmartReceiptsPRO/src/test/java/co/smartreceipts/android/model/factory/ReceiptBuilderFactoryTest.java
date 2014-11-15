package co.smartreceipts.android.model.factory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.testutils.ReceiptUtils;
import co.smartreceipts.android.testutils.TestUtils;
import co.smartreceipts.android.testutils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ReceiptBuilderFactoryTest {

    ReceiptBuilderFactory receiptBuilderFactory;

    @Before
    public void setUp() throws Exception {
        receiptBuilderFactory = new ReceiptBuilderFactory(ReceiptUtils.Constants.ID);
    }

    @Test
    public void testBuild() {
        assertNotNull(receiptBuilderFactory.build());
        assertEquals(ReceiptUtils.Constants.ID, receiptBuilderFactory.build().getId());
    }

    @Test
    public void testTrip() {
        final Trip trip = TripUtils.newSpyOfDefaultTrip();
        receiptBuilderFactory.setTrip(trip);
        assertEquals(trip, receiptBuilderFactory.build().getTrip());
    }

    @Test
    public void testName() {
        receiptBuilderFactory.setName(ReceiptUtils.Constants.NAME);
        assertEquals(ReceiptUtils.Constants.NAME, receiptBuilderFactory.build().getName());
    }

    @Test
    public void testPriceAsDouble() {
        receiptBuilderFactory.setPrice(ReceiptUtils.Constants.PRICE);
        assertEquals((float) ReceiptUtils.Constants.PRICE, receiptBuilderFactory.build().getPriceAsFloat(), TestUtils.EPSILON);
    }

    @Test
    public void testPriceAsString() {
        receiptBuilderFactory.setPrice(ReceiptUtils.Constants.PRICE);
        assertEquals(ReceiptUtils.Constants.PRICE, receiptBuilderFactory.build().getPrice());
    }

    @Test
    public void testTaxAsDouble() {
        receiptBuilderFactory.setTax(ReceiptUtils.Constants.TAX);
        assertEquals((float) ReceiptUtils.Constants.TAX, receiptBuilderFactory.build().getTaxAsFloat(), TestUtils.EPSILON);
    }

    @Test
    public void testTaxAsString() {
        receiptBuilderFactory.setTax(ReceiptUtils.Constants.TAX);
        assertEquals(ReceiptUtils.Constants.TAX, receiptBuilderFactory.build().getTax());
    }

    @Test
    public void testCurrencyWithCurrency() {
        receiptBuilderFactory.setCurrency(ReceiptUtils.Constants.CURRENCY);
        assertEquals(ReceiptUtils.Constants.CURRENCY, receiptBuilderFactory.build().getCurrency());
    }

    @Test
    public void testCurrencyWithString() {
        receiptBuilderFactory.setCurrency(ReceiptUtils.Constants.CURRENCY_CODE);
        assertEquals(ReceiptUtils.Constants.CURRENCY_CODE, receiptBuilderFactory.build().getCurrencyCode());
    }

    @Test
    public void testDateWithDate() {
        receiptBuilderFactory.setDate(ReceiptUtils.Constants.DATE);
        assertEquals(ReceiptUtils.Constants.DATE, receiptBuilderFactory.build().getDate());
    }

    @Test
    public void testDateWithMillis() {
        receiptBuilderFactory.setDate(ReceiptUtils.Constants.DATE_MILLIS);
        assertEquals(ReceiptUtils.Constants.DATE_MILLIS, receiptBuilderFactory.build().getDate().getTime());
    }

    @Test
    public void testTimeZoneWithTimeZome() {
        receiptBuilderFactory.setTimeZone(ReceiptUtils.Constants.TIMEZONE);
        assertEquals(ReceiptUtils.Constants.TIMEZONE, receiptBuilderFactory.build().getTimeZone());
    }

    @Test
    public void testTimeZoneWithString() {
        receiptBuilderFactory.setTimeZone(ReceiptUtils.Constants.TIMEZONE_CODE);
        assertEquals(ReceiptUtils.Constants.TIMEZONE_CODE, receiptBuilderFactory.build().getTimeZone().getID());
    }

    @Test
    public void testCategory() {
        receiptBuilderFactory.setCategory(ReceiptUtils.Constants.CATEGORY);
        assertEquals(ReceiptUtils.Constants.CATEGORY, receiptBuilderFactory.build().getCategory());
    }

    @Test
    public void testComment() {
        receiptBuilderFactory.setComment(ReceiptUtils.Constants.COMMENT);
        assertEquals(ReceiptUtils.Constants.COMMENT, receiptBuilderFactory.build().getComment());
    }

    @Test
    public void testPaymentMethod() {
        final PaymentMethod paymentMethod = mock(PaymentMethod.class);
        receiptBuilderFactory.setPaymentMethod(paymentMethod);
        assertEquals(paymentMethod, receiptBuilderFactory.build().getPaymentMethod());
    }

    @Test
    public void testFile() {
        final File file = mock(File.class);
        receiptBuilderFactory.setFile(file);
        assertEquals(file, receiptBuilderFactory.build().getFile());
    }

    @Test
    public void testImage() {
        final File image = mock(File.class);
        receiptBuilderFactory.setFile(image);
        assertEquals(image, receiptBuilderFactory.build().getImage());
    }

    @Test
    public void testPDF() {
        final File pdf = mock(File.class);
        receiptBuilderFactory.setFile(pdf);
        assertEquals(pdf, receiptBuilderFactory.build().getPDF());
    }

    @Test
    public void testIsExpensible() {
        final boolean isExpensible = true;
        receiptBuilderFactory.setIsExpenseable(isExpensible);
        assertEquals(isExpensible, receiptBuilderFactory.build().isExpensable());
    }

    @Test
    public void testIsNotExpensible() {
        final boolean isExpensible = false;
        receiptBuilderFactory.setIsExpenseable(isExpensible);
        assertEquals(isExpensible, receiptBuilderFactory.build().isExpensable());
    }

    @Test
    public void testIsFullPage() {
        final boolean isFullPage = true;
        receiptBuilderFactory.setIsFullPage(isFullPage);
        assertEquals(isFullPage, receiptBuilderFactory.build().isFullPage());
    }

    @Test
    public void testIsNotFullPage() {
        final boolean isFullPage = false;
        receiptBuilderFactory.setIsFullPage(isFullPage);
        assertEquals(isFullPage, receiptBuilderFactory.build().isFullPage());
    }

    @Test
    public void testIsSelected() {
        final boolean isSelected = true;
        receiptBuilderFactory.setIsSelected(isSelected);
        assertEquals(isSelected, receiptBuilderFactory.build().isSelected());
    }

    @Test
    public void testIsNotSelected() {
        final boolean isSelected = false;
        receiptBuilderFactory.setIsSelected(isSelected);
        assertEquals(isSelected, receiptBuilderFactory.build().isSelected());
    }

    @Test
    public void testIndex() {
        receiptBuilderFactory.setIndex(ReceiptUtils.Constants.INDEX);
        assertEquals(ReceiptUtils.Constants.INDEX, receiptBuilderFactory.build().getIndex());
    }

    @Test
    public void setSourceAsCache() {
        receiptBuilderFactory.setSourceAsCache();
        assertEquals(Source.Cache, receiptBuilderFactory.build().getSource());
    }

    @Test
    public void testExtraEditText1() {
        receiptBuilderFactory.setExtraEditText1(ReceiptUtils.Constants.EXTRA1);
        assertEquals(ReceiptUtils.Constants.EXTRA1, receiptBuilderFactory.build().getExtraEditText1());
    }

    @Test
    public void testExtraEditText2() {
        receiptBuilderFactory.setExtraEditText1(ReceiptUtils.Constants.EXTRA2);
        assertEquals(ReceiptUtils.Constants.EXTRA2, receiptBuilderFactory.build().getExtraEditText1());
    }

    @Test
    public void testExtraEditText3() {
        receiptBuilderFactory.setExtraEditText1(ReceiptUtils.Constants.EXTRA3);
        assertEquals(ReceiptUtils.Constants.EXTRA3, receiptBuilderFactory.build().getExtraEditText1());
    }

    @Test
    public void testDefaults() {
        final Receipt receipt = receiptBuilderFactory.build();
        assertNotNull(receipt.getName());
        assertNotNull(receipt.getComment());
        assertNotNull(receipt.getCategory());
        assertNotNull(receipt.getDate());
        assertNotNull(receipt.getTimeZone());
        assertNotNull(receipt.getDecimalFormattedPrice());
        assertNotNull(receipt.getDecimalFormattedTax());
        assertNotNull(receipt.getSource());

        // nullable
        assertNull(receipt.getTrip());
        assertNull(receipt.getFile());
        assertNull(receipt.getPaymentMethod());
        assertNull(receipt.getExtraEditText1());
        assertNull(receipt.getExtraEditText2());
        assertNull(receipt.getExtraEditText3());

        // Default Values
        assertEquals(ReceiptUtils.Constants.ID, receipt.getId());
        assertEquals(-1, receipt.getIndex());
        assertFalse(receipt.isExpensable());
        assertFalse(receipt.isFullPage());
        assertFalse(receipt.isSelected());
    }

}
