package co.smartreceipts.tests;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.filters.ReceiptFilter;
import co.smartreceipts.tests.utils.ReceiptUtils.Constants;

@Config(emulateSdk = 18) 
@RunWith(RobolectricTestRunner.class)
public class FilterTest {
	
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
	public void sampleFilterTest() {
		// TODO: Please override me
		ReceiptFilter filter = new ReceiptFilter() {
			@Override
			public boolean accept(ReceiptRow receipt) {
				// TODO: Use the actual implementation instead
				return true;
			}
		};
		ReceiptRow receipt1 = getGenericReceiptRowBuilder().setCategory("XXXX").build();
		ReceiptRow receipt2 = getGenericReceiptRowBuilder().setDate(0).build();
		assertTrue(filter.accept(receipt1));
		assertTrue(filter.accept(receipt2));
	}
}
