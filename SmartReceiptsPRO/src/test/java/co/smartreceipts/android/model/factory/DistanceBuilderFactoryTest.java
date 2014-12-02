package co.smartreceipts.android.model.factory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.utils.DistanceUtils;
import co.smartreceipts.android.utils.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class DistanceBuilderFactoryTest {

    DistanceBuilderFactory distanceBuilderFactory;

    @Before
    public void setUp() {
        distanceBuilderFactory = new DistanceBuilderFactory(DistanceUtils.Constants.ID);
    }

    @Test
    public void testComment() {
        distanceBuilderFactory.setComment(DistanceUtils.Constants.COMMENT);
        assertEquals(DistanceUtils.Constants.COMMENT, distanceBuilderFactory.build().getComment());
    }

    @Test
    public void testCurrencyWithCurrency() {
        distanceBuilderFactory.setCurrency(DistanceUtils.Constants.CURRENCY);
        assertEquals(DistanceUtils.Constants.CURRENCY, distanceBuilderFactory.build().getCurrency());
    }

    @Test
    public void testCurrencyWithString() {
        distanceBuilderFactory.setCurrency(DistanceUtils.Constants.CURRENCY_CODE);
        assertEquals(DistanceUtils.Constants.CURRENCY_CODE, distanceBuilderFactory.build().getCurrencyCode());
    }

    @Test
    public void testDateWithDate() {
        distanceBuilderFactory.setDate(DistanceUtils.Constants.DATE);
        assertEquals(DistanceUtils.Constants.DATE, distanceBuilderFactory.build().getDate());
    }

    @Test
    public void testDateWithMillis() {
        distanceBuilderFactory.setDate(DistanceUtils.Constants.DATE_MILLIS);
        assertEquals(DistanceUtils.Constants.DATE_MILLIS, distanceBuilderFactory.build().getDate().getTime());
    }

    @Test
    public void testDistanceWithBigDecimal() {
        distanceBuilderFactory.setDistance(DistanceUtils.Constants.DISTANCE);
        assertEquals(DistanceUtils.Constants.DISTANCE, distanceBuilderFactory.build().getDistance());
    }

    @Test
    public void testDistanceWithDouble() {
        distanceBuilderFactory.setDistance(DistanceUtils.Constants.DISTANCE_DOUBLE);
        assertEquals(DistanceUtils.Constants.DISTANCE_DOUBLE, distanceBuilderFactory.build().getDistance().doubleValue(), TestUtils.EPSILON);
    }

    @Test
    public void testId() {
        assertEquals(DistanceUtils.Constants.ID, distanceBuilderFactory.build().getId());
    }

    @Test
    public void testLocation() {
        distanceBuilderFactory.setLocation(DistanceUtils.Constants.LOCATION);
        assertEquals(DistanceUtils.Constants.LOCATION, distanceBuilderFactory.build().getLocation());
    }

    @Test
    public void testRateWithBigDecimal() {
        distanceBuilderFactory.setRate(DistanceUtils.Constants.RATE);
        assertEquals(DistanceUtils.Constants.RATE, distanceBuilderFactory.build().getRate());
    }

    @Test
    public void testRateWithDouble() {
        distanceBuilderFactory.setRate(DistanceUtils.Constants.RATE_DOUBLE);
        assertEquals(DistanceUtils.Constants.RATE_DOUBLE, distanceBuilderFactory.build().getRate().doubleValue(), TestUtils.EPSILON);
    }

    @Test
    public void testTimeZoneWithTimeZone() {
        distanceBuilderFactory.setTimezone(DistanceUtils.Constants.TIMEZONE);
        assertEquals(DistanceUtils.Constants.TIMEZONE, distanceBuilderFactory.build().getTimeZone());
    }

    @Test
    public void testTimeZoneWithString() {
        distanceBuilderFactory.setTimezone(DistanceUtils.Constants.TIMEZONE_CODE);
        assertEquals(DistanceUtils.Constants.TIMEZONE_CODE, distanceBuilderFactory.build().getTimeZone().getID());
    }

    @Test
    public void testTrip() {
        final Trip trip = mock(Trip.class);
        distanceBuilderFactory.setTrip(trip);
        assertEquals(trip, distanceBuilderFactory.build().getTrip());
    }


}
