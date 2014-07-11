package co.smartreceipts.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.filters.FilterFactory;
import co.smartreceipts.android.filters.ReceiptCategoryFilter;
import co.smartreceipts.android.filters.ReceiptOrFilter;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.tests.utils.ReceiptUtils.Constants;

@Config(emulateSdk = 18) 
@RunWith(RobolectricTestRunner.class)
public class ReceiptFilterTest {
	
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
	}
	
	@Test
	public void receiptOrFilterConstructorTest() throws JSONException {
		final ReceiptCategoryFilter filter1 = new ReceiptCategoryFilter(Constants.CATEGORY);
		final ReceiptCategoryFilter filter2 = new ReceiptCategoryFilter("cat2");
		final List<Filter<ReceiptRow>> filters = new ArrayList<Filter<ReceiptRow>>(2);
		filters.add(filter1);
		filters.add(filter2);
		final ReceiptOrFilter orFilter1 = new ReceiptOrFilter(filters);
		final ReceiptOrFilter orFilter2 = new ReceiptOrFilter();
		orFilter2.or(filter1);
		orFilter2.or(filter2);
		assertEquals(orFilter1, orFilter2);
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
	}
}
