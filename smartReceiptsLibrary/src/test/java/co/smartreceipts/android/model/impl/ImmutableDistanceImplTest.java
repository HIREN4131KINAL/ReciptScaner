package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.utils.DistanceUtils;
import co.smartreceipts.android.utils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@RunWith(RobolectricGradleTestRunner.class)
public class ImmutableDistanceImplTest {

    Trip trip;
    ImmutableDistanceImpl distance1, distance2, distance3;

    @Before
    public void setUp() throws Exception {
        trip = TripUtils.newDefaultTrip();
        distance1 = new ImmutableDistanceImpl(DistanceUtils.Constants.ID,
                trip,
                DistanceUtils.Constants.LOCATION,
                DistanceUtils.Constants.DISTANCE,
                DistanceUtils.Constants.RATE,
                DistanceUtils.Constants.CURRENCY,
                DistanceUtils.Constants.DATE,
                DistanceUtils.Constants.TIMEZONE,
                DistanceUtils.Constants.COMMENT);
        distance2 = new ImmutableDistanceImpl(DistanceUtils.Constants.ID,
                trip,
                DistanceUtils.Constants.LOCATION,
                DistanceUtils.Constants.DISTANCE,
                DistanceUtils.Constants.RATE,
                DistanceUtils.Constants.CURRENCY,
                DistanceUtils.Constants.DATE,
                DistanceUtils.Constants.TIMEZONE,
                DistanceUtils.Constants.COMMENT);
        distance3 = new ImmutableDistanceImpl(-1,
                trip,
                DistanceUtils.Constants.LOCATION + "_new",
                DistanceUtils.Constants.DISTANCE,
                DistanceUtils.Constants.RATE,
                DistanceUtils.Constants.CURRENCY,
                DistanceUtils.Constants.DATE,
                DistanceUtils.Constants.TIMEZONE,
                DistanceUtils.Constants.COMMENT + "_new");
    }

    @Test
    public void testComment() {
        assertEquals(DistanceUtils.Constants.COMMENT, distance1.getComment());
    }

    @Test
    public void testCurrency() {
        assertEquals(DistanceUtils.Constants.CURRENCY, distance1.getPrice().getCurrency());
        assertEquals(DistanceUtils.Constants.CURRENCY_CODE, distance1.getPrice().getCurrencyCode());
    }

    @Test
    public void testDateAndTimeZone() {
        assertEquals(DistanceUtils.Constants.DATE, distance1.getDate());
        assertEquals(DistanceUtils.Constants.DATE_MILLIS, distance1.getDate().getTime());
        assertEquals(DistanceUtils.Constants.TIMEZONE, distance1.getTimeZone());
        assertEquals(DistanceUtils.Constants.TIMEZONE_CODE, distance1.getTimeZone().getID());
        assertEquals(DistanceUtils.Constants.SLASH_FORMATTED_DATE, distance1.getFormattedDate(RuntimeEnvironment.application, "/"));
        assertEquals(DistanceUtils.Constants.DASH_FORMATTED_DATE, distance1.getFormattedDate(RuntimeEnvironment.application, "-"));
    }

    @Test
    public void testDistance() {
        assertEquals(DistanceUtils.Constants.DISTANCE, distance1.getDistance());
        assertEquals(DistanceUtils.Constants.DECIMAL_FORMATTED_DISTANCE, distance1.getDecimalFormattedDistance());
    }

    @Test
    public void testId() {
        assertEquals(DistanceUtils.Constants.ID, distance1.getId());
    }

    @Test
    public void testLocation() {
        assertEquals(DistanceUtils.Constants.LOCATION, distance1.getLocation());
    }

    @Test
    public void testRate() {
        assertEquals(DistanceUtils.Constants.RATE, distance1.getRate());
        assertEquals(DistanceUtils.Constants.DECIMAL_FORMATTED_RATE, distance1.getDecimalFormattedRate());
        assertEquals(DistanceUtils.Constants.CURRENCY_FORMATTED_RATE, distance1.getCurrencyFormattedRate());
    }

    @Test
    public void parcelTest() {
        final Parcel parcel = Parcel.obtain();
        distance1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final Distance parcelledDistance = ImmutableDistanceImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(parcelledDistance);
        assertEquals(distance1, parcelledDistance);
        DistanceUtils.assertFieldEquality(parcelledDistance, distance1);
    }

    @Test
    public void equalityTest() {
        DistanceUtils.assertFieldEquality(distance1, distance2);
        assertEquals(distance1, distance2);
        assertEquals(distance2, distance1);
        assertNotSame(distance1, null);
        assertNotSame(distance1, new Object());
        assertNotSame(distance1, distance3);
    }

    @Test
    public void hashCodeTest() {
        assertEquals(distance1.hashCode(), distance2.hashCode());
        assertNotSame(distance1.hashCode(), distance3.hashCode());
    }

}
