package co.smartreceipts.android.model.factory;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.testutils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TripBuilderFactoryTest {

    TripBuilderFactory tripBuilderFactory;

    @Before
    public void setUp() throws Exception {
        tripBuilderFactory = new TripBuilderFactory();
    }

    @Test
    public void testBuild() {
        assertNotNull(tripBuilderFactory.build());
    }

    @Test
    public void testDirectory() {
        tripBuilderFactory.setDirectory(TripUtils.Constants.DIRECTORY);
        assertEquals(TripUtils.Constants.DIRECTORY, tripBuilderFactory.build().getDirectory());
    }

    @Test
    public void testStartDateWithDate() {
        tripBuilderFactory.setStartDate(TripUtils.Constants.START_DATE);
        assertEquals(TripUtils.Constants.START_DATE, tripBuilderFactory.build().getStartDate());
    }

    @Test
    public void testStartDateWithMillis() {
        tripBuilderFactory.setStartDate(TripUtils.Constants.START_DATE_MILLIS);
        assertEquals(TripUtils.Constants.START_DATE_MILLIS, tripBuilderFactory.build().getStartDate().getTime());
    }

    @Test
    public void testStartTimeZoneWithTimeZone() {
        tripBuilderFactory.setStartTimeZone(TripUtils.Constants.START_TIMEZONE);
        assertEquals(TripUtils.Constants.START_TIMEZONE, tripBuilderFactory.build().getStartTimeZone());
    }

    @Test
    public void testStartTimeZoneWithCode() {
        tripBuilderFactory.setStartTimeZone(TripUtils.Constants.START_TIMEZONE_CODE);
        assertEquals(TripUtils.Constants.START_TIMEZONE_CODE, tripBuilderFactory.build().getStartTimeZone().getID());
    }

    @Test
    public void testEndDateWithDate() {
        tripBuilderFactory.setEndDate(TripUtils.Constants.END_DATE);
        assertEquals(TripUtils.Constants.END_DATE, tripBuilderFactory.build().getEndDate());
    }

    @Test
    public void testEndDateWithMillis() {
        tripBuilderFactory.setEndDate(TripUtils.Constants.END_DATE_MILLIS);
        assertEquals(TripUtils.Constants.END_DATE_MILLIS, tripBuilderFactory.build().getEndDate().getTime());
    }

    @Test
    public void testEndTimeZoneWithTimeZone() {
        tripBuilderFactory.setEndTimeZone(TripUtils.Constants.END_TIMEZONE);
        assertEquals(TripUtils.Constants.END_TIMEZONE, tripBuilderFactory.build().getEndTimeZone());
    }

    @Test
    public void testEndTimeZoneWithCode() {
        tripBuilderFactory.setEndTimeZone(TripUtils.Constants.END_TIMEZONE_CODE);
        assertEquals(TripUtils.Constants.END_TIMEZONE_CODE, tripBuilderFactory.build().getEndTimeZone().getID());
    }

    @Test
    public void testCurrencyWithCurrency() {
        tripBuilderFactory.setCurrency(TripUtils.Constants.CURRENCY);
        assertEquals(TripUtils.Constants.CURRENCY, tripBuilderFactory.build().getCurrency());
    }

    @Test
    public void testCurrencyWithString() {
        tripBuilderFactory.setCurrency(TripUtils.Constants.CURRENCY_CODE);
        assertEquals(TripUtils.Constants.CURRENCY_CODE, tripBuilderFactory.build().getCurrencyCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCurrencyWithEmptyString() {
        tripBuilderFactory.setCurrency("");
    }

    @Test
    public void testDefaultCurrencyWithDefaultCurrency() {
        tripBuilderFactory.setDefaultCurrency(TripUtils.Constants.CURRENCY);
        assertEquals(TripUtils.Constants.CURRENCY, tripBuilderFactory.build().getDefaultCurrency());
    }

    @Test
    public void testDefaultCurrencyWithString() {
        tripBuilderFactory.setDefaultCurrency(TripUtils.Constants.CURRENCY_CODE);
        assertEquals(TripUtils.Constants.CURRENCY_CODE, tripBuilderFactory.build().getDefaultCurrencyCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDefaultCurrencyWithEmptyString() {
        tripBuilderFactory.setDefaultCurrency("");
    }

    @Test
    public void testDefaultCurrencyWithFallbackString() {
        tripBuilderFactory.setDefaultCurrency(TripUtils.Constants.CURRENCY_CODE, "NUL");
        assertEquals(TripUtils.Constants.CURRENCY_CODE, tripBuilderFactory.build().getDefaultCurrencyCode());
    }

    @Test
    public void testDefaultCurrencyUsingFallbackString() {
        tripBuilderFactory.setDefaultCurrency("", "NUL");
        assertEquals("NUL", tripBuilderFactory.build().getDefaultCurrencyCode());
    }

    @Test
    public void testComment() {
        tripBuilderFactory.setDefaultCurrency(TripUtils.Constants.COMMENT);
        assertEquals(TripUtils.Constants.COMMENT, tripBuilderFactory.build().getComment());
    }

    @Test
    public void testCostCenter() {
        tripBuilderFactory.setDefaultCurrency(TripUtils.Constants.COST_CENTER);
        assertEquals(TripUtils.Constants.COST_CENTER, tripBuilderFactory.build().getCostCenter());
    }

    @Test
    public void testDefaults() {
        final Trip trip = tripBuilderFactory.build();
        assertNotNull(trip.getDirectory());
        assertNotNull(trip.getComment());
        assertNotNull(trip.getCostCenter());
        assertNotNull(trip.getDefaultCurrency());
        assertNotNull(trip.getCurrency());
        assertNotNull(trip.getStartDate());
        assertNotNull(trip.getStartTimeZone());
        assertNotNull(trip.getEndDate());
        assertNotNull(trip.getEndTimeZone());
    }

}