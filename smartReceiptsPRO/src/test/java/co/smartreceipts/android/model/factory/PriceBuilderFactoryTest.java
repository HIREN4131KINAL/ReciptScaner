package co.smartreceipts.android.model.factory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Priceable;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.utils.ReceiptUtils;
import co.smartreceipts.android.utils.TestUtils;
import co.smartreceipts.android.utils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class PriceBuilderFactoryTest {

    private static final BigDecimal ONE_DOLLAR = new BigDecimal(1);
    private static final BigDecimal TWO_DOLLARS = new BigDecimal(2);

    PriceBuilderFactory priceBuilderFactory;

    @Mock
    Price price;

    @Mock
    Priceable priceable1, priceable2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        priceBuilderFactory = new PriceBuilderFactory();
        when(price.getPrice()).thenReturn(ONE_DOLLAR);
        when(priceable1.getPrice()).thenReturn(price);
        when(priceable2.getPrice()).thenReturn(price);
    }

    @Test
    public void testPriceAsPrice() {
        priceBuilderFactory.setPrice(price);
        assertEquals(price, priceBuilderFactory.build());
    }

    @Test
    public void testPriceAsDouble() {
        priceBuilderFactory.setPrice(ReceiptUtils.Constants.PRICE).setCurrency(ReceiptUtils.Constants.CURRENCY);
        assertEquals((float) ReceiptUtils.Constants.PRICE, priceBuilderFactory.build().getPriceAsFloat(), TestUtils.EPSILON);
    }

    @Test
    public void testPriceAsString() {
        priceBuilderFactory.setPrice(ReceiptUtils.Constants.DECIMAL_FORMATTED_PRICE).setCurrency(ReceiptUtils.Constants.CURRENCY);
        assertEquals(ReceiptUtils.Constants.DECIMAL_FORMATTED_PRICE, priceBuilderFactory.build().getDecimalFormattedPrice());
    }

    @Test
    public void testCurrencyWithCurrency() {
        priceBuilderFactory.setPrice(ReceiptUtils.Constants.DECIMAL_FORMATTED_PRICE).setCurrency(ReceiptUtils.Constants.CURRENCY);
        assertEquals(ReceiptUtils.Constants.CURRENCY, priceBuilderFactory.build().getCurrency());
    }

    @Test
    public void testCurrencyWithString() {
        priceBuilderFactory.setPrice(ReceiptUtils.Constants.DECIMAL_FORMATTED_PRICE).setCurrency(ReceiptUtils.Constants.CURRENCY_CODE);
        assertEquals(ReceiptUtils.Constants.CURRENCY_CODE, priceBuilderFactory.build().getCurrencyCode());
    }

    @Test
    public void testPrices() {
        final List<Price> prices = Arrays.asList(price, price);
        priceBuilderFactory.setPrices(prices);
        assertEquals(TWO_DOLLARS.floatValue(), priceBuilderFactory.build().getPriceAsFloat(), TestUtils.EPSILON);
    }

    @Test
    public void testPriceables() {
        final List<Priceable> priceables = Arrays.asList(priceable1, priceable2);
        priceBuilderFactory.setPriceables(priceables);
        assertEquals(TWO_DOLLARS.floatValue(), priceBuilderFactory.build().getPriceAsFloat(), TestUtils.EPSILON);
    }

}
