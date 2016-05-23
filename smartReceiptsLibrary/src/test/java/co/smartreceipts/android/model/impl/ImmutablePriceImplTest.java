package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.math.BigDecimal;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.utils.ReceiptUtils;
import co.smartreceipts.android.utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@RunWith(RobolectricGradleTestRunner.class)
public class ImmutablePriceImplTest {

    ImmutablePriceImpl price1, price2, price3;
    BigDecimal bigDecimal1, bigDecimal2, bigDecimal3;
    WBCurrency currency1, currency2, currency3;
    ExchangeRate exchangeRate;

    @Before
    public void setUp() throws Exception {
        currency1 = ReceiptUtils.Constants.CURRENCY;
        currency2 = ReceiptUtils.Constants.CURRENCY;
        currency3 = ReceiptUtils.Constants.CURRENCY;
        bigDecimal1 = new BigDecimal(ReceiptUtils.Constants.PRICE);
        bigDecimal2 = new BigDecimal(ReceiptUtils.Constants.PRICE);
        bigDecimal3 = new BigDecimal(-1*ReceiptUtils.Constants.PRICE); // Note the negative here
        exchangeRate =  new ExchangeRateBuilderFactory().build();
        price1 = new ImmutablePriceImpl(bigDecimal1, currency1, exchangeRate);
        price2 = new ImmutablePriceImpl(bigDecimal2, currency2, exchangeRate);
        price3 = new ImmutablePriceImpl(bigDecimal3, currency3, exchangeRate);
    }

    @Test
    public void testGetCurrency() {
        assertEquals(ReceiptUtils.Constants.CURRENCY, price1.getCurrency());
    }

    @Test
    public void testGetCurrencyCode() {
        assertEquals(ReceiptUtils.Constants.CURRENCY_CODE, price1.getCurrencyCode());
    }

    @Test
    public void testGetPriceAsFloat() {
        assertEquals((float) ReceiptUtils.Constants.PRICE, price1.getPriceAsFloat(), TestUtils.EPSILON);
    }

    @Test
    public void testGetPrice() {
        assertEquals(bigDecimal1, price1.getPrice());
    }

    @Test
    public void testGetDecimalFormattedPrice() {
        assertEquals(ReceiptUtils.Constants.DECIMAL_FORMATTED_PRICE, price1.getDecimalFormattedPrice());
    }

    @Test
    public void testGetCurrencyFormattedPrice() {
        assertEquals(ReceiptUtils.Constants.CURRENCY_FORMATTED_PRICE, price1.getCurrencyFormattedPrice());
    }

    @Test
    public void testGetExchangeRate() {
        assertEquals(exchangeRate, price1.getExchangeRate());
    }

    @Test
    public void testHashCode() {
        assertEquals(price1.hashCode(), price2.hashCode());
        assertNotSame(price1.hashCode(), price3.hashCode());
    }

    @Test
    public void testEquals() {
        assertNotSame(price1, null);
        assertNotSame(price1, new Object());
        assertNotSame(price1, price3);
        assertEquals(price1, price2);
    }

    @Test
    public void testParcel() {
        final Parcel parcel = Parcel.obtain();
        price1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        final ImmutablePriceImpl parceledPrice = ImmutablePriceImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(parceledPrice);
        assertEquals(price1, parceledPrice);
    }

}
