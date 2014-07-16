package co.smartreceipts.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Date;
import java.util.ArrayList;
import java.util.TimeZone;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.filters.FilterFactory;
import co.smartreceipts.android.filters.TripAndFilter;
import co.smartreceipts.android.filters.TripEndsOnOrAfterDayFilter;
import co.smartreceipts.android.filters.TripEndsOnOrBeforeDayFilter;
import co.smartreceipts.android.filters.TripMaximumPriceFilter;
import co.smartreceipts.android.filters.TripMinimumPriceFilter;
import co.smartreceipts.android.filters.TripNotFilter;
import co.smartreceipts.android.filters.TripOrFilter;
import co.smartreceipts.android.filters.TripStartsOnOrAfterDayFilter;
import co.smartreceipts.android.filters.TripStartsOnOrBeforeDayFilter;
import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.tests.utils.TripUtils.Constants;

@Config(emulateSdk = 18, manifest = "../SmartReceiptsPRO/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class TripFilterTest {

	// Test constants for Price checking
	private static final String CURRENCY = Constants.CURRENCY_CODE;
	private static final String PRICE_NORMAL = "100.00";
	private static final String PRICE_HIGH = "150.00";
	private static final String PRICE_LOW = "50.00";

	// Test constants for Date checking
	private static final TimeZone TZ = TimeZone.getDefault();
	private static final long MILLIS = new java.util.Date().getTime();
	private static final Date NOW = new Date(MILLIS);
	private static final Date FUTURE = new Date(MILLIS + 1000);
	private static final Date PAST = new Date(MILLIS - 1000);

	private TripRow.Builder getGenericTripRowBuilder() {
		TripRow.Builder builderA = new TripRow.Builder()
				.setComment(Constants.COMMENT)
				.setPrice(Constants.PRICE)
				.setCurrency(Constants.CURRENCY)
				.setCurrency(Constants.CURRENCY_CODE)
				.setStartDate(Constants.START_DATE)
				.setStartTimeZone(Constants.START_TIMEZONE)
				.setEndDate(Constants.END_DATE)
				.setEndTimeZone(Constants.END_TIMEZONE)
				.setMileage(Constants.MILEAGE);
		return builderA;
	}

	@Test
	public void tripMinimumPriceFilterTest() throws JSONException {
		final TripRow tripNormal = getGenericTripRowBuilder().setPrice(PRICE_NORMAL).build();
		final TripRow tripHigh = getGenericTripRowBuilder().setPrice(PRICE_HIGH).build();
		final TripRow tripLow = getGenericTripRowBuilder().setPrice(PRICE_LOW).build();

		final TripMinimumPriceFilter filter = new TripMinimumPriceFilter(
				Float.parseFloat(PRICE_NORMAL),
				Constants.CURRENCY_CODE);

		assertTrue(filter.accept(tripNormal));
		assertTrue(filter.accept(tripHigh));
		assertFalse(filter.accept(tripLow));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
	}

	@Test
	public void tripMaximumPriceFilterTest() throws JSONException {
		final TripRow tripNormal = getGenericTripRowBuilder().setPrice(PRICE_NORMAL).build();
		final TripRow tripHigh = getGenericTripRowBuilder().setPrice(PRICE_HIGH).build();
		final TripRow tripLow = getGenericTripRowBuilder().setPrice(PRICE_LOW).build();

		final TripMaximumPriceFilter filter = new TripMaximumPriceFilter(
				Float.parseFloat(PRICE_NORMAL),
				Constants.CURRENCY_CODE);

		assertTrue(filter.accept(tripNormal));
		assertFalse(filter.accept(tripHigh));
		assertTrue(filter.accept(tripLow));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
	}

	@Test
	public void tripStartsOnOrAfterDayFilterTest() throws JSONException {
		final TripRow tripNow = getGenericTripRowBuilder().setStartDate(NOW).build();
		final TripRow tripFuture = getGenericTripRowBuilder().setStartDate(FUTURE).build();
		final TripRow tripPast = getGenericTripRowBuilder().setStartDate(PAST).build();
		final TripStartsOnOrAfterDayFilter filter = new TripStartsOnOrAfterDayFilter(NOW, TZ);

		assertTrue(filter.accept(tripNow));
		assertTrue(filter.accept(tripFuture));
		assertFalse(filter.accept(tripPast));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
	}

	@Test
	public void tripStartsOnOrBeforeDayFilterTest() throws JSONException {
		final TripRow tripNow = getGenericTripRowBuilder().setStartDate(NOW).build();
		final TripRow tripFuture = getGenericTripRowBuilder().setStartDate(FUTURE).build();
		final TripRow tripPast = getGenericTripRowBuilder().setStartDate(PAST).build();
		final TripStartsOnOrBeforeDayFilter filter = new TripStartsOnOrBeforeDayFilter(NOW, TZ);

		assertTrue(filter.accept(tripNow));
		assertFalse(filter.accept(tripFuture));
		assertTrue(filter.accept(tripPast));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
	}

	@Test
	public void tripEndsOnOrAfterDayFilterTest() throws JSONException {
		final TripRow tripNow = getGenericTripRowBuilder().setEndDate(NOW).build();
		final TripRow tripFuture = getGenericTripRowBuilder().setEndDate(FUTURE).build();
		final TripRow tripPast = getGenericTripRowBuilder().setEndDate(PAST).build();
		final TripEndsOnOrAfterDayFilter filter = new TripEndsOnOrAfterDayFilter(NOW, TZ);

		assertTrue(filter.accept(tripNow));
		assertTrue(filter.accept(tripFuture));
		assertFalse(filter.accept(tripPast));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
	}

	@Test
	public void tripEndsOnOrBeforeDayFilterTest() throws JSONException {
		final TripRow tripNow = getGenericTripRowBuilder().setEndDate(NOW).build();
		final TripRow tripFuture = getGenericTripRowBuilder().setEndDate(FUTURE).build();
		final TripRow tripPast = getGenericTripRowBuilder().setEndDate(PAST).build();
		final TripEndsOnOrBeforeDayFilter filter = new TripEndsOnOrBeforeDayFilter(NOW, TZ);

		assertTrue(filter.accept(tripNow));
		assertFalse(filter.accept(tripFuture));
		assertTrue(filter.accept(tripPast));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
	}

	@Test
	public void tripOrFilterTest() throws JSONException {
		// in this test scenario, trip with normal price will NOT be accepted
		// accept rule: price >= high OR price <= LOW

		final TripRow tripNormal = getGenericTripRowBuilder().setPrice(PRICE_NORMAL).build();
		final TripRow tripHigh = getGenericTripRowBuilder().setPrice(PRICE_HIGH).build();
		final TripRow tripLow = getGenericTripRowBuilder().setPrice(PRICE_LOW).build();

		final TripMinimumPriceFilter minPrice = new TripMinimumPriceFilter(Float.parseFloat(PRICE_HIGH), CURRENCY);
		final TripMaximumPriceFilter maxPrice = new TripMaximumPriceFilter(Float.parseFloat(PRICE_LOW), CURRENCY);
		final TripOrFilter orFilter = new TripOrFilter();
		orFilter.or(minPrice);
		orFilter.or(maxPrice);

		assertTrue(orFilter.accept(tripHigh));
		assertTrue(orFilter.accept(tripLow));
		assertFalse(orFilter.accept(tripNormal)); // rejected
		
		assertEquals(orFilter, FilterFactory.getTripFilter(orFilter.getJsonRepresentation()));
	}

	@Test
	public void tripAndFilterTest() throws JSONException {
		// in this test scenario, we will only accept cheap trip started in the past (i.e. tripPastLow)
		// accept rule: starts on or before past AND price is low

		final TripRow tripPastNormal = getGenericTripRowBuilder().setStartDate(PAST).setPrice(PRICE_NORMAL).build();
		final TripRow tripPastHigh = getGenericTripRowBuilder().setStartDate(PAST).setPrice(PRICE_HIGH).build();
		final TripRow tripPastLow = getGenericTripRowBuilder().setStartDate(PAST).setPrice(PRICE_LOW).build();
		final TripRow tripFutureNormal = getGenericTripRowBuilder().setStartDate(FUTURE).setPrice(PRICE_NORMAL).build();
		final TripRow tripFutureHigh = getGenericTripRowBuilder().setStartDate(FUTURE).setPrice(PRICE_HIGH).build();
		final TripRow tripFutureLow = getGenericTripRowBuilder().setStartDate(FUTURE).setPrice(PRICE_LOW).build();

		final TripStartsOnOrBeforeDayFilter dateFilter = new TripStartsOnOrBeforeDayFilter(PAST, Constants.START_TIMEZONE);
		final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter(Float.parseFloat(PRICE_LOW), CURRENCY);
		final TripAndFilter andFilter = new TripAndFilter();
		andFilter.and(dateFilter);
		andFilter.and(priceFilter);
		
		assertFalse(andFilter.accept(tripPastNormal));
		assertFalse(andFilter.accept(tripPastHigh));
		assertTrue(andFilter.accept(tripPastLow)); // accepted
		assertFalse(andFilter.accept(tripFutureNormal));
		assertFalse(andFilter.accept(tripFutureHigh));
		assertFalse(andFilter.accept(tripFutureLow));
		
		assertEquals(andFilter, FilterFactory.getTripFilter(andFilter.getJsonRepresentation()));
	}
	
	@Test
	public void tripNotFilterTest() throws JSONException {
		// in this test scenario, we will only accept tripHigh
		// accept rule: NOT (price <= normal) 
		// equivalent to: (price > normal)
		
		final TripRow tripNormal = getGenericTripRowBuilder().setPrice(PRICE_NORMAL).build();
		final TripRow tripHigh = getGenericTripRowBuilder().setPrice(PRICE_HIGH).build();
		final TripRow tripLow = getGenericTripRowBuilder().setPrice(PRICE_LOW).build();

		final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter(Float.parseFloat(PRICE_NORMAL), CURRENCY);
		final TripNotFilter notFilter = new TripNotFilter(priceFilter);
		
		assertFalse(notFilter.accept(tripNormal));
		assertTrue(notFilter.accept(tripHigh)); // accepted
		assertFalse(notFilter.accept(tripLow));
		
		assertEquals(notFilter, FilterFactory.getTripFilter(notFilter.getJsonRepresentation()));
	}
	
	@Test
	public void tripAndFilterConstructorTest() throws JSONException {
		// in this test scenario, 
		// filters constructed with same data but different method should be equal

		final TripStartsOnOrBeforeDayFilter dateFilter = new TripStartsOnOrBeforeDayFilter(PAST, Constants.START_TIMEZONE);
		final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter(Float.parseFloat(PRICE_LOW), CURRENCY);
		
		// filter 1 -- composited filters added in object instantiation (i.e. constructor)
		final ArrayList<Filter<TripRow>> filters = new ArrayList<Filter<TripRow>>();
		filters.add(dateFilter);
		filters.add(priceFilter);
		final TripAndFilter filter1 = new TripAndFilter(filters);
		
		// filter 2 -- composited filters added after object instantiation
		final TripAndFilter filter2 = new TripAndFilter();
		filter2.and(dateFilter);
		filter2.and(priceFilter);
		
		assertEquals(filter1, filter2);
		assertEquals(filter1, FilterFactory.getTripFilter(filter1.getJsonRepresentation()));
		assertEquals(filter2, FilterFactory.getTripFilter(filter2.getJsonRepresentation()));
	}
	
	@Test
	public void tripOrFilterConstructorTest() throws JSONException {
		// in this test scenario, 
		// filters constructed with same data but different method should be equal

		final TripStartsOnOrBeforeDayFilter dateFilter = new TripStartsOnOrBeforeDayFilter(PAST, Constants.START_TIMEZONE);
		final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter(Float.parseFloat(PRICE_LOW), CURRENCY);
		
		// filter 1 -- composited filters added in object instantiation (i.e. constructor)
		final ArrayList<Filter<TripRow>> filters = new ArrayList<Filter<TripRow>>();
		filters.add(dateFilter);
		filters.add(priceFilter);
		final TripOrFilter filter1 = new TripOrFilter(filters);
		
		// filter 2 -- composited filters added after object instantiation
		final TripOrFilter filter2 = new TripOrFilter();
		filter2.or(dateFilter);
		filter2.or(priceFilter);
		
		assertEquals(filter1, filter2);
		assertEquals(filter1, FilterFactory.getTripFilter(filter1.getJsonRepresentation()));
		assertEquals(filter2, FilterFactory.getTripFilter(filter2.getJsonRepresentation()));
	}
}