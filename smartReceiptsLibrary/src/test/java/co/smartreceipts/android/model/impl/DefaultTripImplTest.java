package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.math.BigDecimal;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.utils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class DefaultTripImplTest {

    DefaultTripImpl trip;
    Price price, subtotal;

    @Before
    public void setUp() throws Exception {
        trip = new DefaultTripImpl(TripUtils.Constants.DIRECTORY,
                TripUtils.Constants.START_DATE,
                TripUtils.Constants.START_TIMEZONE,
                TripUtils.Constants.END_DATE,
                TripUtils.Constants.END_TIMEZONE,
                TripUtils.Constants.DEFAULT_CURRENCY,
                TripUtils.Constants.COMMENT,
                TripUtils.Constants.COST_CENTER,
                null, // filter
                Source.Undefined);
        price = new ImmutablePriceImpl(new BigDecimal(TripUtils.Constants.PRICE), TripUtils.Constants.CURRENCY, new ExchangeRateBuilderFactory().setBaseCurrency(TripUtils.Constants.CURRENCY).build());
        subtotal = new ImmutablePriceImpl(new BigDecimal(TripUtils.Constants.DAILY_SUBTOTAL), TripUtils.Constants.CURRENCY, new ExchangeRateBuilderFactory().setBaseCurrency(TripUtils.Constants.CURRENCY).build());
        trip.setPrice(price);
        trip.setDailySubTotal(subtotal);
    }


    @Test
    public void testName() {
        assertEquals(TripUtils.Constants.DIRECTORY, trip.getDirectory());
        assertEquals(TripUtils.Constants.DIRECTORY.getAbsolutePath(), trip.getDirectoryPath());
        assertEquals(TripUtils.Constants.DIRECTORY_NAME, trip.getName());
    }

    @Test
    public void testStartDatesAndTimeZones() {
        assertEquals(TripUtils.Constants.START_DATE, trip.getStartDate());
        assertEquals(TripUtils.Constants.START_DATE_MILLIS, trip.getStartDate().getTime());
        assertEquals(TripUtils.Constants.START_TIMEZONE, trip.getStartTimeZone());
        assertEquals(TripUtils.Constants.START_TIMEZONE_CODE, trip.getStartTimeZone().getID());
        assertEquals(TripUtils.Constants.SLASH_FORMATTED_START_DATE, trip.getFormattedStartDate(RuntimeEnvironment.application, "/"));
        assertEquals(TripUtils.Constants.DASH_FORMATTED_START_DATE, trip.getFormattedStartDate(RuntimeEnvironment.application, "-"));
    }

    @Test
    public void testEndDatesAndTimeZones() {
        assertEquals(TripUtils.Constants.END_DATE, trip.getEndDate());
        assertEquals(TripUtils.Constants.END_DATE_MILLIS, trip.getEndDate().getTime());
        assertEquals(TripUtils.Constants.END_TIMEZONE, trip.getEndTimeZone());
        assertEquals(TripUtils.Constants.END_TIMEZONE_CODE, trip.getEndTimeZone().getID());
        assertEquals(TripUtils.Constants.SLASH_FORMATTED_END_DATE, trip.getFormattedEndDate(RuntimeEnvironment.application, "/"));
        assertEquals(TripUtils.Constants.DASH_FORMATTED_END_DATE, trip.getFormattedEndDate(RuntimeEnvironment.application, "-"));
    }

    @Test
    public void testPrice() {
        assertEquals(price, trip.getPrice());
    }

    @Test
    public void testDailySubTotal() {
        assertEquals(subtotal, trip.getDailySubTotal());
    }

    @Test
    public void testComment() {
        assertEquals(TripUtils.Constants.COMMENT, trip.getComment());
    }

    @Test
    public void testCostCenter() {
        assertEquals(TripUtils.Constants.COST_CENTER, trip.getCostCenter());
    }

    @Test
    public void testFilter() {
        assertNull(trip.getFilter());
    }

    @Test
    public void parcelTest() {
        final Parcel parcel = Parcel.obtain();
        trip.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        final Trip parceledTrip = DefaultTripImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(parceledTrip);
        assertEquals(trip, parceledTrip);
        TripUtils.assertFieldEquality(trip, parceledTrip);
    }


}