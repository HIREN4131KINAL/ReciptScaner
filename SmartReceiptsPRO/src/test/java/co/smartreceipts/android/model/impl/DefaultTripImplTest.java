package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.utils.TestUtils;
import co.smartreceipts.android.utils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class DefaultTripImplTest {

    DefaultTripImpl trip;


    @Before
    public void setUp() throws Exception {
        trip = new DefaultTripImpl(TripUtils.Constants.DIRECTORY,
                TripUtils.Constants.START_DATE,
                TripUtils.Constants.START_TIMEZONE,
                TripUtils.Constants.END_DATE,
                TripUtils.Constants.END_TIMEZONE,
                TripUtils.Constants.CURRENCY,
                TripUtils.Constants.DEFAULT_CURRENCY,
                TripUtils.Constants.COMMENT,
                TripUtils.Constants.COST_CENTER,
                null, // filter
                Source.Undefined);
        trip.setPrice(TripUtils.Constants.PRICE);
        trip.setDailySubTotal(TripUtils.Constants.DAILY_SUBTOTAL);
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
        assertEquals(TripUtils.Constants.SLASH_FORMATTED_START_DATE, trip.getFormattedStartDate(Robolectric.application, "/"));
        assertEquals(TripUtils.Constants.DASH_FORMATTED_START_DATE, trip.getFormattedStartDate(Robolectric.application, "-"));
    }

    @Test
    public void testEndDatesAndTimeZones() {
        assertEquals(TripUtils.Constants.END_DATE, trip.getEndDate());
        assertEquals(TripUtils.Constants.END_DATE_MILLIS, trip.getEndDate().getTime());
        assertEquals(TripUtils.Constants.END_TIMEZONE, trip.getEndTimeZone());
        assertEquals(TripUtils.Constants.END_TIMEZONE_CODE, trip.getEndTimeZone().getID());
        assertEquals(TripUtils.Constants.SLASH_FORMATTED_END_DATE, trip.getFormattedEndDate(Robolectric.application, "/"));
        assertEquals(TripUtils.Constants.DASH_FORMATTED_END_DATE, trip.getFormattedEndDate(Robolectric.application, "-"));
    }

    @Test
    public void testCurrency() {
        assertEquals(TripUtils.Constants.CURRENCY, trip.getCurrency());
        assertEquals(TripUtils.Constants.CURRENCY_CODE, trip.getCurrencyCode());
    }

    @Test
    public void testDefaultCurrency() {
        assertEquals(TripUtils.Constants.DEFAULT_CURRENCY, trip.getDefaultCurrency());
        assertEquals(TripUtils.Constants.DEFAULT_CURRENCY_CODE, trip.getDefaultCurrencyCode());
    }

    @Test
    public void testPriceAndCurrency() {
        assertEquals((float) TripUtils.Constants.PRICE, trip.getPriceAsFloat(), TestUtils.EPSILON);
        assertEquals(TripUtils.Constants.DECIMAL_FORMATTED_PRICE, trip.getDecimalFormattedPrice());
        assertEquals(TripUtils.Constants.CURRENCY_FORMATTED_PRICE, trip.getCurrencyFormattedPrice());
    }

    @Test
    public void testDailySubtotalAndCurrency() {
        assertEquals((float) TripUtils.Constants.DAILY_SUBTOTAL, trip.getDailySubTotalAsFloat(), TestUtils.EPSILON);
        assertEquals(TripUtils.Constants.DECIMAL_FORMATTED_SUBTOTAL, trip.getDecimalFormattedDailySubTotal());
        assertEquals(TripUtils.Constants.CURRENCY_FORMATTED_SUBTOTAL, trip.getCurrencyFormattedDailySubTotal());
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