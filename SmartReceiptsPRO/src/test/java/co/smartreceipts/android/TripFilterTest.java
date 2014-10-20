package co.smartreceipts.android;

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
import co.smartreceipts.android.filters.FilterType;
import co.smartreceipts.android.filters.TripAndFilter;
import co.smartreceipts.android.filters.TripEndsOnOrAfterDayFilter;
import co.smartreceipts.android.filters.TripEndsOnOrBeforeDayFilter;
import co.smartreceipts.android.filters.TripMaximumPriceFilter;
import co.smartreceipts.android.filters.TripMinimumPriceFilter;
import co.smartreceipts.android.filters.TripNotFilter;
import co.smartreceipts.android.filters.TripOrFilter;
import co.smartreceipts.android.filters.TripStartsOnOrAfterDayFilter;
import co.smartreceipts.android.filters.TripStartsOnOrBeforeDayFilter;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.tests.utils.TripUtils.Constants;

@Config(emulateSdk = 18, manifest = "../SmartReceiptsPRO/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class TripFilterTest {

	// Test constants for Price checking
	private static final String CURRENCY = Constants.CURRENCY_CODE;
	private static final double PRICE_NORMAL = 100.00d;
	private static final double PRICE_HIGH = 150.00d;
	private static final double PRICE_LOW = 50.00d;

	// Test constants for Date checking
	private static final TimeZone TZ = TimeZone.getDefault();
	private static final long MILLIS = new java.util.Date().getTime();
	private static final Date NOW = new Date(MILLIS);
	private static final Date FUTURE = new Date(MILLIS + 1000);
	private static final Date PAST = new Date(MILLIS - 1000);

	private Trip.Builder getGenericTripRowBuilder() {
		Trip.Builder builderA = new Trip.Builder()
				.setComment(Constants.COMMENT)
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
		final Trip tripNormal = getGenericTripRowBuilder().build();
		final Trip tripHigh = getGenericTripRowBuilder().build();
		final Trip tripLow = getGenericTripRowBuilder().build();
		tripNormal.setPrice(PRICE_NORMAL);
		tripHigh.setPrice(PRICE_HIGH);
		tripLow.setPrice(PRICE_LOW);

		final TripMinimumPriceFilter filter = new TripMinimumPriceFilter((float) PRICE_NORMAL, Constants.CURRENCY_CODE);

		assertTrue(filter.accept(tripNormal));
		assertTrue(filter.accept(tripHigh));
		assertFalse(filter.accept(tripLow));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_trip_min_price);
		assertEquals(filter.getType(), FilterType.Float);
	}

	@Test
	public void tripMaximumPriceFilterTest() throws JSONException {
		final Trip tripNormal = getGenericTripRowBuilder().build();
		final Trip tripHigh = getGenericTripRowBuilder().build();
		final Trip tripLow = getGenericTripRowBuilder().build();
		tripNormal.setPrice(PRICE_NORMAL);
		tripHigh.setPrice(PRICE_HIGH);
		tripLow.setPrice(PRICE_LOW);

		final TripMaximumPriceFilter filter = new TripMaximumPriceFilter((float) PRICE_NORMAL, Constants.CURRENCY_CODE);

		assertTrue(filter.accept(tripNormal));
		assertFalse(filter.accept(tripHigh));
		assertTrue(filter.accept(tripLow));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_trip_max_price);
		assertEquals(filter.getType(), FilterType.Float);
	}

	@Test
	public void tripStartsOnOrAfterDayFilterTest() throws JSONException {
		final Trip tripNow = getGenericTripRowBuilder().setStartDate(NOW).build();
		final Trip tripFuture = getGenericTripRowBuilder().setStartDate(FUTURE).build();
		final Trip tripPast = getGenericTripRowBuilder().setStartDate(PAST).build();
		final TripStartsOnOrAfterDayFilter filter = new TripStartsOnOrAfterDayFilter(NOW, TZ);

		assertTrue(filter.accept(tripNow));
		assertTrue(filter.accept(tripFuture));
		assertFalse(filter.accept(tripPast));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_trip_starts_on_or_after);
		assertEquals(filter.getType(), FilterType.Date);
	}

	@Test
	public void tripStartsOnOrBeforeDayFilterTest() throws JSONException {
		final Trip tripNow = getGenericTripRowBuilder().setStartDate(NOW).build();
		final Trip tripFuture = getGenericTripRowBuilder().setStartDate(FUTURE).build();
		final Trip tripPast = getGenericTripRowBuilder().setStartDate(PAST).build();
		final TripStartsOnOrBeforeDayFilter filter = new TripStartsOnOrBeforeDayFilter(NOW, TZ);

		assertTrue(filter.accept(tripNow));
		assertFalse(filter.accept(tripFuture));
		assertTrue(filter.accept(tripPast));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_trip_starts_on_or_before);
		assertEquals(filter.getType(), FilterType.Date);
	}

	@Test
	public void tripEndsOnOrAfterDayFilterTest() throws JSONException {
		final Trip tripNow = getGenericTripRowBuilder().setEndDate(NOW).build();
		final Trip tripFuture = getGenericTripRowBuilder().setEndDate(FUTURE).build();
		final Trip tripPast = getGenericTripRowBuilder().setEndDate(PAST).build();
		final TripEndsOnOrAfterDayFilter filter = new TripEndsOnOrAfterDayFilter(NOW, TZ);

		assertTrue(filter.accept(tripNow));
		assertTrue(filter.accept(tripFuture));
		assertFalse(filter.accept(tripPast));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_trip_ends_on_or_after);
		assertEquals(filter.getType(), FilterType.Date);
	}

	@Test
	public void tripEndsOnOrBeforeDayFilterTest() throws JSONException {
		final Trip tripNow = getGenericTripRowBuilder().setEndDate(NOW).build();
		final Trip tripFuture = getGenericTripRowBuilder().setEndDate(FUTURE).build();
		final Trip tripPast = getGenericTripRowBuilder().setEndDate(PAST).build();
		final TripEndsOnOrBeforeDayFilter filter = new TripEndsOnOrBeforeDayFilter(NOW, TZ);

		assertTrue(filter.accept(tripNow));
		assertFalse(filter.accept(tripFuture));
		assertTrue(filter.accept(tripPast));
		assertEquals(filter, FilterFactory.getTripFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_trip_ends_on_or_before);
		assertEquals(filter.getType(), FilterType.Date);
	}

	@Test
	public void tripOrFilterTest() throws JSONException {
		// in this test scenario, trip with normal price will NOT be accepted
		// accept rule: price >= high OR price <= LOW

		final Trip tripNormal = getGenericTripRowBuilder().build();
		final Trip tripHigh = getGenericTripRowBuilder().build();
		final Trip tripLow = getGenericTripRowBuilder().build();
		tripNormal.setPrice(PRICE_NORMAL);
		tripHigh.setPrice(PRICE_HIGH);
		tripLow.setPrice(PRICE_LOW);

		final TripMinimumPriceFilter minPrice = new TripMinimumPriceFilter((float) PRICE_HIGH, CURRENCY);
		final TripMaximumPriceFilter maxPrice = new TripMaximumPriceFilter((float) PRICE_LOW, CURRENCY);
		final TripOrFilter orFilter = new TripOrFilter();
		orFilter.or(minPrice);
		orFilter.or(maxPrice);

		assertTrue(orFilter.accept(tripHigh));
		assertTrue(orFilter.accept(tripLow));
		assertFalse(orFilter.accept(tripNormal)); // rejected
		
		assertEquals(orFilter, FilterFactory.getTripFilter(orFilter.getJsonRepresentation()));
		assertEquals(orFilter.getNameResource(), R.string.filter_name_or);
		assertEquals(orFilter.getType(), FilterType.Composite);
	}

	@Test
	public void tripAndFilterTest() throws JSONException {
		// in this test scenario, we will only accept cheap trip started in the past (i.e. tripPastLow)
		// accept rule: starts on or before past AND price is low

		final Trip tripPastNormal = getGenericTripRowBuilder().setStartDate(PAST).build();
		final Trip tripPastHigh = getGenericTripRowBuilder().setStartDate(PAST).build();
		final Trip tripPastLow = getGenericTripRowBuilder().setStartDate(PAST).build();
		final Trip tripFutureNormal = getGenericTripRowBuilder().setStartDate(FUTURE).build();
		final Trip tripFutureHigh = getGenericTripRowBuilder().setStartDate(FUTURE).build();
		final Trip tripFutureLow = getGenericTripRowBuilder().setStartDate(FUTURE).build();
		tripPastHigh.setPrice(PRICE_HIGH);
		tripPastNormal.setPrice(PRICE_NORMAL);
		tripPastLow.setPrice(PRICE_LOW);
		tripFutureHigh.setPrice(PRICE_HIGH);
		tripFutureNormal.setPrice(PRICE_NORMAL);
		tripFutureLow.setPrice(PRICE_LOW);

		final TripStartsOnOrBeforeDayFilter dateFilter = new TripStartsOnOrBeforeDayFilter(PAST, Constants.START_TIMEZONE);
		final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter((float) PRICE_LOW, CURRENCY);
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
		assertEquals(andFilter.getNameResource(), R.string.filter_name_and);
		assertEquals(andFilter.getType(), FilterType.Composite);
	}
	
	@Test
	public void tripNotFilterTest() throws JSONException {
		// in this test scenario, we will only accept tripHigh
		// accept rule: NOT (price <= normal) 
		// equivalent to: (price > normal)
		
		final Trip tripNormal = getGenericTripRowBuilder().build();
		final Trip tripHigh = getGenericTripRowBuilder().build();
		final Trip tripLow = getGenericTripRowBuilder().build();
		tripNormal.setPrice(PRICE_NORMAL);
		tripHigh.setPrice(PRICE_HIGH);
		tripLow.setPrice(PRICE_LOW);

		final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter((float) PRICE_NORMAL, CURRENCY);
		final TripNotFilter notFilter = new TripNotFilter(priceFilter);
		
		assertFalse(notFilter.accept(tripNormal));
		assertTrue(notFilter.accept(tripHigh)); // accepted
		assertFalse(notFilter.accept(tripLow));
		
		assertEquals(notFilter, FilterFactory.getTripFilter(notFilter.getJsonRepresentation()));
		assertEquals(notFilter.getNameResource(), R.string.filter_name_not);
		assertEquals(notFilter.getType(), FilterType.Composite);
	}
	
	@Test
	public void tripAndFilterConstructorTest() throws JSONException {
		// in this test scenario, 
		// filters constructed with same data but different method should be equal

		final TripStartsOnOrBeforeDayFilter dateFilter = new TripStartsOnOrBeforeDayFilter(PAST, Constants.START_TIMEZONE);
		final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter((float) PRICE_LOW, CURRENCY);
		
		// filter 1 -- composited filters added in object instantiation (i.e. constructor)
		final ArrayList<Filter<Trip>> filters = new ArrayList<Filter<Trip>>();
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
		final TripMaximumPriceFilter priceFilter = new TripMaximumPriceFilter((float) PRICE_LOW, CURRENCY);
		
		// filter 1 -- composited filters added in object instantiation (i.e. constructor)
		final ArrayList<Filter<Trip>> filters = new ArrayList<Filter<Trip>>();
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