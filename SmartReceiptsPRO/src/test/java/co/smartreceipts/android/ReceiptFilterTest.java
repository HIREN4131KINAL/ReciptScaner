package co.smartreceipts.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.TimeZone;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.filters.FilterFactory;
import co.smartreceipts.android.filters.FilterType;
import co.smartreceipts.android.filters.ReceiptAndFilter;
import co.smartreceipts.android.filters.ReceiptCategoryFilter;
import co.smartreceipts.android.filters.ReceiptIsExpensableFilter;
import co.smartreceipts.android.filters.ReceiptMaximumPriceFilter;
import co.smartreceipts.android.filters.ReceiptMinimumPriceFilter;
import co.smartreceipts.android.filters.ReceiptNotFilter;
import co.smartreceipts.android.filters.ReceiptOnOrAfterDayFilter;
import co.smartreceipts.android.filters.ReceiptOnOrBeforeDayFilter;
import co.smartreceipts.android.filters.ReceiptOrFilter;
import co.smartreceipts.android.filters.ReceiptSelectedFilter;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.tests.utils.ReceiptUtils.Constants;

@Config(emulateSdk = 18, manifest = "../SmartReceiptsPRO/AndroidManifest.xml") 
@RunWith(RobolectricTestRunner.class)
public class ReceiptFilterTest {
	
	// Test constants for Price checking
	private static final String CURRENCY = Constants.CURRENCY_CODE;
	private static final String PRICE_NORMAL = "100.00";
	private static final String PRICE_HIGH = "150.00";
	private static final String PRICE_LOW = "50.00";

	// Test constants for Date checking
	private static final TimeZone TZ = Constants.TIMEZONE;
	private static final long MILLIS = new java.util.Date().getTime();
	private static final Date NOW = new Date(MILLIS);
	private static final Date FUTURE = new Date(MILLIS + 1000);
	private static final Date PAST = new Date(MILLIS - 1000);
	
	private SmartReceiptsApplication mApp;

	/**
	 * Generates a builder for mReceiptRowA. This builder user primitives/Strings
	 * whenever possible as opposed to higher level objects
	 * @return
	 */
	private ReceiptRow.Builder getGenericReceiptRowBuilder() {
		ReceiptRow.Builder builderA = new ReceiptRow.Builder(Constants.ID);
		builderA.setCategory(Constants.CATEGORY)
				.setComment(Constants.COMMENT)
				.setCurrency(Constants.CURRENCY_CODE)
				.setDate(Constants.DATE_MILLIS)
				.setExtraEditText1(Constants.EXTRA1)
				.setExtraEditText2(Constants.EXTRA2)
				.setExtraEditText3(Constants.EXTRA3)
				.setFile(getFile(Constants.IMAGE_FILE_NAME))
				.setIsExpenseable(Constants.IS_EXPENSABLE)
				.setIsFullPage(Constants.IS_FULLPAGE)
				.setName(Constants.NAME)
				.setPrice(Constants.PRICE)
				.setTax(Constants.TAX)
				.setTimeZone(Constants.TIMEZONE_CODE);
		return builderA;
	}
	
	private File getFile(String name) {
		File tripDir = mApp.getPersistenceManager().getStorageManager().mkdir(co.smartreceipts.tests.utils.TripUtils.Constants.DIRECTORY_NAME);
		File file = new File(tripDir, name);
		mApp.getPersistenceManager().getStorageManager().createFile(file);
		return file;
	}
	
	@Before
	public void setUp() throws Exception {
		mApp = (SmartReceiptsApplication) Robolectric.application;
	}

	@After
	public void tearDown() throws Exception {
		mApp = null;
	}
	
	@Test
	public void receiptCategoryFilterTest() throws JSONException {
		final ReceiptRow receipt1 = getGenericReceiptRowBuilder().build();
		final ReceiptRow receipt2 = getGenericReceiptRowBuilder().setCategory("BAD Category").build();
		final ReceiptCategoryFilter filter = new ReceiptCategoryFilter(Constants.CATEGORY);
		assertTrue(filter.accept(receipt1));
		assertFalse(filter.accept(receipt2));
		assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation())); // Confirm we can properly recreate
		assertEquals(filter.getNameResource(), R.string.filter_name_receipt_category);
		assertEquals(filter.getType(), FilterType.String);
	}
	
	@Test
	public void receiptIsExpensableFilterTest() throws JSONException {
		final ReceiptRow receipt1 = getGenericReceiptRowBuilder().setIsExpenseable(true).build();
		final ReceiptRow receipt2 = getGenericReceiptRowBuilder().setIsExpenseable(false).build();
		final ReceiptIsExpensableFilter filter = new ReceiptIsExpensableFilter();
		
		assertTrue(filter.accept(receipt1));
		assertFalse(filter.accept(receipt2));
		assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_receipt_expensable);
		assertEquals(filter.getType(), FilterType.Boolean);
	}
	
	@Test
	public void receiptIsSelectedFilterTest() throws JSONException {
		final ReceiptRow receipt1 = getGenericReceiptRowBuilder().setIsSelected(true).build();
		final ReceiptRow receipt2 = getGenericReceiptRowBuilder().setIsSelected(false).build();
		final ReceiptSelectedFilter filter = new ReceiptSelectedFilter();
		
		assertTrue(filter.accept(receipt1));
		assertFalse(filter.accept(receipt2));
		assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_receipt_selected);
		assertEquals(filter.getType(), FilterType.Boolean);
	}
	
	@Test
	public void receiptMinimumPriceFilterTest() throws JSONException {
		final ReceiptRow receiptNormal = getGenericReceiptRowBuilder().setPrice(PRICE_NORMAL).build();
		final ReceiptRow receiptHigh = getGenericReceiptRowBuilder().setPrice(PRICE_HIGH).build();
		final ReceiptRow receiptLow = getGenericReceiptRowBuilder().setPrice(PRICE_LOW).build();
		
		final ReceiptMinimumPriceFilter filter = new ReceiptMinimumPriceFilter(
				Float.parseFloat(PRICE_NORMAL), CURRENCY);
		
		assertTrue(filter.accept(receiptNormal));
		assertTrue(filter.accept(receiptHigh));
		assertFalse(filter.accept(receiptLow));
		assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_receipt_min_price);
		assertEquals(filter.getType(), FilterType.Float);
	}
	
	@Test
	public void receiptMaximumPriceFilterTest() throws JSONException {
		final ReceiptRow receiptNormal = getGenericReceiptRowBuilder().setPrice(PRICE_NORMAL).build();
		final ReceiptRow receiptHigh = getGenericReceiptRowBuilder().setPrice(PRICE_HIGH).build();
		final ReceiptRow receiptLow = getGenericReceiptRowBuilder().setPrice(PRICE_LOW).build();
		
		final ReceiptMaximumPriceFilter filter = new ReceiptMaximumPriceFilter(
				Float.parseFloat(PRICE_NORMAL), CURRENCY);
		
		assertTrue(filter.accept(receiptNormal));
		assertFalse(filter.accept(receiptHigh));
		assertTrue(filter.accept(receiptLow));
		assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_receipt_max_price);
		assertEquals(filter.getType(), FilterType.Float);
	}
	
	@Test
	public void receiptOnOrAfterDayFilterTest() throws JSONException {
		final ReceiptRow receiptNow = getGenericReceiptRowBuilder().setDate(NOW).build();
		final ReceiptRow receiptFuture = getGenericReceiptRowBuilder().setDate(FUTURE).build();
		final ReceiptRow receiptPast = getGenericReceiptRowBuilder().setDate(PAST).build();
		final ReceiptOnOrAfterDayFilter filter = new ReceiptOnOrAfterDayFilter(NOW, TZ);
		
		assertTrue(filter.accept(receiptNow));
		assertTrue(filter.accept(receiptFuture));
		assertFalse(filter.accept(receiptPast));
		assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_receipt_on_or_after);
		assertEquals(filter.getType(), FilterType.Date);
	}
	
	@Test
	public void receiptOnOrBeforeDayFilterTest() throws JSONException {
		final ReceiptRow receiptNow = getGenericReceiptRowBuilder().setDate(NOW).build();
		final ReceiptRow receiptFuture = getGenericReceiptRowBuilder().setDate(FUTURE).build();
		final ReceiptRow receiptPast = getGenericReceiptRowBuilder().setDate(PAST).build();
		final ReceiptOnOrBeforeDayFilter filter = new ReceiptOnOrBeforeDayFilter(NOW, TZ);
		
		assertTrue(filter.accept(receiptNow));
		assertFalse(filter.accept(receiptFuture));
		assertTrue(filter.accept(receiptPast));
		assertEquals(filter, FilterFactory.getReceiptFilter(filter.getJsonRepresentation()));
		assertEquals(filter.getNameResource(), R.string.filter_name_receipt_on_or_before);
		assertEquals(filter.getType(), FilterType.Date);
	}
	
	@Test
	public void receiptOrFilterTest() throws JSONException {
		final String category2 = "cat2";
		final ReceiptRow receipt1 = getGenericReceiptRowBuilder().build();
		final ReceiptRow receipt2 = getGenericReceiptRowBuilder().setCategory(category2).build();
		final ReceiptRow receipt3 = getGenericReceiptRowBuilder().setCategory("BAD Category").build();
		final ReceiptCategoryFilter filter1 = new ReceiptCategoryFilter(Constants.CATEGORY);
		final ReceiptCategoryFilter filter2 = new ReceiptCategoryFilter(category2);
		final ReceiptOrFilter orFilter = new ReceiptOrFilter();
		orFilter.or(filter1);
		orFilter.or(filter2);
		assertTrue(orFilter.accept(receipt1));
		assertTrue(orFilter.accept(receipt2));
		assertFalse(orFilter.accept(receipt3));
		assertEquals(orFilter, FilterFactory.getReceiptFilter(orFilter.getJsonRepresentation())); // Confirm we can properly recreate
		assertEquals(orFilter.getNameResource(), R.string.filter_name_or);
		assertEquals(orFilter.getType(), FilterType.Composite);
	}
	
	@Test
	public void receiptAndFilterTest() throws JSONException {
		final ReceiptRow receipt = getGenericReceiptRowBuilder().build();
		
		final ReceiptIsExpensableFilter trueFilter1 = new ReceiptIsExpensableFilter();
		final ReceiptCategoryFilter trueFilter2 = new ReceiptCategoryFilter(Constants.CATEGORY);
		final ReceiptCategoryFilter falseFilter = new ReceiptCategoryFilter("BAD Category");
		
		final ReceiptAndFilter andFilterGood = new ReceiptAndFilter();
		final ReceiptAndFilter andFilterBad = new ReceiptAndFilter();
		andFilterGood.and(trueFilter1).and(trueFilter2);
		andFilterBad.and(trueFilter1).and(trueFilter2).and(falseFilter);
		
		assertTrue(andFilterGood.accept(receipt));
		assertFalse(andFilterBad.accept(receipt));
		assertEquals(andFilterGood, FilterFactory.getReceiptFilter(andFilterGood.getJsonRepresentation()));
		assertEquals(andFilterBad, FilterFactory.getReceiptFilter(andFilterBad.getJsonRepresentation()));
		assertEquals(andFilterGood.getNameResource(), R.string.filter_name_and);
		assertEquals(andFilterGood.getType(), FilterType.Composite);
	}
	
	@Test
	public void receiptNotFilterTest() throws JSONException {
		// in this test scenario, we will only accept receiptHigh
		// accept rule: NOT (price <= normal) 
		// equivalent to: (price > normal)
		
		final ReceiptRow receiptNormal = getGenericReceiptRowBuilder().setPrice(PRICE_NORMAL).build();
		final ReceiptRow receiptHigh = getGenericReceiptRowBuilder().setPrice(PRICE_HIGH).build();
		final ReceiptRow receiptLow = getGenericReceiptRowBuilder().setPrice(PRICE_LOW).build();

		final ReceiptMaximumPriceFilter priceFilter = new ReceiptMaximumPriceFilter(Float.parseFloat(PRICE_NORMAL), CURRENCY);
		final ReceiptNotFilter notFilter = new ReceiptNotFilter(priceFilter);
		
		assertFalse(notFilter.accept(receiptNormal));
		assertTrue(notFilter.accept(receiptHigh)); // accepted
		assertFalse(notFilter.accept(receiptLow));
		
		assertEquals(notFilter, FilterFactory.getReceiptFilter(notFilter.getJsonRepresentation()));
		assertEquals(notFilter.getNameResource(), R.string.filter_name_not);
		assertEquals(notFilter.getType(), FilterType.Composite);
	}

	@Test
	public void receiptOrFilterConstructorTest() throws JSONException {
		// in this test scenario, 
		// filters constructed with same data but different method should be equal

		final ReceiptCategoryFilter filter1 = new ReceiptCategoryFilter(Constants.CATEGORY);
		final ReceiptCategoryFilter filter2 = new ReceiptCategoryFilter("Just another category");
		
		// filter 1 -- composited filters added in object instantiation (i.e. constructor)
		final ArrayList<Filter<ReceiptRow>> filters = new ArrayList<Filter<ReceiptRow>>();
		filters.add(filter1);
		filters.add(filter2);
		final ReceiptOrFilter orFilter1 = new ReceiptOrFilter(filters);
		
		// filter 2 -- composited filters added after object instantiation
		final ReceiptOrFilter orFilter2 = new ReceiptOrFilter();
		orFilter2.or(filter1);
		orFilter2.or(filter2);
		
		assertEquals(orFilter1, orFilter2);
		assertEquals(orFilter1, FilterFactory.getReceiptFilter(orFilter1.getJsonRepresentation()));
		assertEquals(orFilter2, FilterFactory.getReceiptFilter(orFilter2.getJsonRepresentation()));
	}
	
	@Test
	public void receiptAndFilterConstructorTest() throws JSONException {
		// in this test scenario, 
		// filters constructed with same data but different method should be equal

		final ReceiptCategoryFilter filter1 = new ReceiptCategoryFilter(Constants.CATEGORY);
		final ReceiptCategoryFilter filter2 = new ReceiptCategoryFilter("Just another category");
		
		// filter 1 -- composited filters added in object instantiation (i.e. constructor)
		final ArrayList<Filter<ReceiptRow>> filters = new ArrayList<Filter<ReceiptRow>>();
		filters.add(filter1);
		filters.add(filter2);
		final ReceiptAndFilter andFilter1 = new ReceiptAndFilter(filters);
		
		// filter 2 -- composited filters added after object instantiation
		final ReceiptAndFilter andFilter2 = new ReceiptAndFilter();
		andFilter2.and(filter1);
		andFilter2.and(filter2);
		
		assertEquals(andFilter1, andFilter2);
		assertEquals(andFilter1, FilterFactory.getReceiptFilter(andFilter1.getJsonRepresentation()));
		assertEquals(andFilter2, FilterFactory.getReceiptFilter(andFilter2.getJsonRepresentation()));
	}
}
