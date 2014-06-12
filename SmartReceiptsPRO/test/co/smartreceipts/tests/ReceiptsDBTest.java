package co.smartreceipts.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.tests.utils.ReceiptUtils.Constants;
import co.smartreceipts.tests.utils.ReceiptUtils;

@Config(emulateSdk = 18) 
@RunWith(RobolectricTestRunner.class)
public class ReceiptsDBTest {
	
	private SmartReceiptsApplication mApp;
	private DatabaseHelper mDB;
	private TripRow mTripRow, mTripRow2;

	@Before
	public void setup() {
		mApp = (SmartReceiptsApplication) Robolectric.application;
		mDB = mApp.getPersistenceManager().getDatabase();
		mTripRow = mDB.insertTripSerial(mApp.getPersistenceManager().getStorageManager().mkdir(co.smartreceipts.tests.utils.TripUtils.Constants.DIRECTORY_NAME), 
										co.smartreceipts.tests.utils.TripUtils.Constants.START_DATE, 
										co.smartreceipts.tests.utils.TripUtils.Constants.END_DATE, 
										co.smartreceipts.tests.utils.TripUtils.Constants.COMMENT,
										co.smartreceipts.tests.utils.TripUtils.Constants.CURRENCY_CODE);
		mTripRow2 = mDB.insertTripSerial(mApp.getPersistenceManager().getStorageManager().mkdir(co.smartreceipts.tests.utils.TripUtils.Constants.DIRECTORY_NAME + "2"), 
										 new Date(co.smartreceipts.tests.utils.TripUtils.Constants.START_DATE_MILLIS + 2000),  
										 new Date(co.smartreceipts.tests.utils.TripUtils.Constants.END_DATE_MILLIS + 2000), 
										 co.smartreceipts.tests.utils.TripUtils.Constants.COMMENT + "2",
										 "EUR");
	}
	
	@After
	public void tearDown() {
		mDB.close();
		mDB = null;
		mApp = null;
	}
	
	private ReceiptRow insertDefaultReceipt() {
		File img = new File(mTripRow.getDirectory(), Constants.IMAGE_FILE_NAME);
		mApp.getPersistenceManager().getStorageManager().createFile(img);
		return insertDefaultReceipt(img);
	}
	
	private ReceiptRow insertDefaultReceipt(File file) {
		return mDB.insertReceiptSerial(mTripRow, 
									  file, 
									  Constants.NAME, 
									  Constants.CATEGORY, 
									  Constants.DATE, 
									  Constants.COMMENT, 
									  Constants.PRICE, 
									  Constants.TAX, 
									  Constants.IS_EXPENSABLE, 
									  Constants.CURRENCY_CODE, 
									  Constants.IS_FULLPAGE, 
									  Constants.EXTRA1, 
									  Constants.EXTRA2, 
									  Constants.EXTRA3);
	}
	
	@Test
	public void insert1() {
		File img = new File(mTripRow.getDirectory(), Constants.IMAGE_FILE_NAME);
		ReceiptRow insertReceipt = insertDefaultReceipt();
		ReceiptRow[] receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(insertReceipt);
		assertNotNull(receipts);
		assertEquals(receipts.length, 1);
		assertEquals(insertReceipt, receipts[0]);
		assertNotEquals(img, insertReceipt.getFile());
		assertEquals(insertReceipt.getFileName(), insertReceipt.getIndex() + "_" + insertReceipt.getName() + ".jpg");
		ReceiptUtils.assertFieldEqualityPlusIndex(insertReceipt, receipts[0]);
	}
	
	@Test
	public void insert2() {
		ReceiptRow insertReceipt1 = insertDefaultReceipt();
		ReceiptRow insertReceipt2 = mDB.insertReceiptSerial(mTripRow, insertReceipt1);
		ReceiptRow[] receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(insertReceipt1);
		assertNotNull(insertReceipt2);
		assertNotNull(receipts);
		assertEquals(receipts.length, 2);
		assertEquals(insertReceipt1, receipts[1]);
		assertEquals(insertReceipt2, receipts[0]);
		ReceiptUtils.assertFieldEquality(insertReceipt1, receipts[1]);
		ReceiptUtils.assertFieldEquality(insertReceipt2, receipts[0]);
	}
	
	@Test
	public void insert3() {
		ReceiptRow insertReceipt = insertDefaultReceipt();
		ReceiptRow findReceipt = mDB.getReceiptByID(insertReceipt.getId());
		assertNotNull(insertReceipt);
		assertNotNull(findReceipt);
		assertEquals(insertReceipt, findReceipt);
		ReceiptUtils.assertFieldEquality(insertReceipt, findReceipt);
	}
	
	@Test
	public void update() {
		ReceiptRow insertReceipt = insertDefaultReceipt();
		ReceiptRow updateReceipt = mDB.updateReceiptSerial(insertReceipt, 
														   mTripRow, 
														   Constants.NAME + "x", 
														   Constants.CATEGORY + "x", 
														   new Date(Constants.DATE_MILLIS + 2000), 
														   Constants.COMMENT + "x", 
														   "10.0", 
														   "2.00", 
														   !Constants.IS_EXPENSABLE, 
														   "EUR", 
														   !Constants.IS_FULLPAGE, 
														   null, 
														   "2", 
														   null);
		ReceiptRow[] receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(updateReceipt);
		assertNotNull(receipts);
		assertEquals(receipts.length, 1);
		assertEquals(updateReceipt, insertReceipt);
		assertEquals(updateReceipt, receipts[0]);
		ReceiptUtils.assertFieldEqualityPlusIndex(updateReceipt, receipts[0]);
	}
	
	@Test 
	public void updateFile() {
		// Files
		File pdf = new File(mTripRow.getDirectory(), Constants.PDF_FILE_NAME);
		File img = new File(mTripRow.getDirectory(), "2_" + Constants.IMAGE_FILE_NAME);
		File bad = new File(mTripRow.getDirectory(), "bad.ext");
		mApp.getPersistenceManager().getStorageManager().createFile(pdf);
		mApp.getPersistenceManager().getStorageManager().createFile(img);
		assertTrue(pdf.exists());
		assertTrue(img.exists());
		assertFalse(bad.exists());
		
		// PDF Update test
		ReceiptRow insertReceipt = insertDefaultReceipt();
		ReceiptRow updateReceipt = mDB.updateReceiptFile(insertReceipt, pdf);
		ReceiptRow[] receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(updateReceipt);
		assertNotNull(receipts);
		assertEquals(receipts.length, 1);
		assertEquals(updateReceipt, insertReceipt);
		assertEquals(updateReceipt, receipts[0]);
		assertTrue(updateReceipt.hasPDF());
		ReceiptUtils.assertFieldEqualityPlusIndex(updateReceipt, receipts[0]);
		
		// Image Update test
		updateReceipt = mDB.updateReceiptFile(insertReceipt, img);
		receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(updateReceipt);
		assertNotNull(receipts);
		assertEquals(receipts.length, 1);
		assertEquals(updateReceipt, insertReceipt);
		assertEquals(updateReceipt, receipts[0]);
		assertTrue(updateReceipt.hasImage());
		ReceiptUtils.assertFieldEqualityPlusIndex(updateReceipt, receipts[0]);
		
		// Bad File Update test
		updateReceipt = mDB.updateReceiptFile(insertReceipt, bad);
		receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(updateReceipt);
		assertNotNull(receipts);
		assertEquals(receipts.length, 1);
		assertEquals(updateReceipt, insertReceipt);
		assertEquals(updateReceipt, receipts[0]);
		assertFalse(updateReceipt.hasFile());
	}
	
	@Test
	public void copyImg() {
		ReceiptRow imgReceipt = insertDefaultReceipt();
		assertTrue(mDB.copyReceiptSerial(imgReceipt, mTripRow2));
		ReceiptRow[] receipts1 = mDB.getReceiptsSerial(mTripRow);
		ReceiptRow[] receipts2 = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(imgReceipt);
		assertNotNull(receipts1);
		assertNotNull(receipts2);
		assertEquals(receipts1.length, 1);
		assertEquals(receipts2.length, 1);
		assertTrue(imgReceipt.hasImage());
		assertTrue(receipts1[0].hasImage());
		assertTrue(receipts2[0].hasImage());
		assertEquals(imgReceipt, receipts1[0]); // receipts2[0] will have a different id
		ReceiptUtils.assertFieldEqualityPlusIndex(imgReceipt, receipts1[0]);
	}

}
