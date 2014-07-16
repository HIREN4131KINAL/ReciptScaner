package co.smartreceipts.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Date;
import java.util.List;

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
	public void insertAndGetAll() {
		File img = new File(mTripRow.getDirectory(), Constants.IMAGE_FILE_NAME);
		ReceiptRow insertReceipt = insertDefaultReceipt();
		List<ReceiptRow> receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(insertReceipt);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 1);
		assertEquals(insertReceipt, receipts.get(0));
		assertNotEquals(img, insertReceipt.getFile());
		assertEquals(insertReceipt.getFileName(), insertReceipt.getIndex() + "_" + insertReceipt.getName() + ".jpg");
		ReceiptUtils.assertFieldEqualityPlusIdAndIndex(insertReceipt, receipts.get(0));
	}
	
	@Test
	public void insertMultipleAndGetAll() {
		ReceiptRow insertReceipt1 = insertDefaultReceipt();
		ReceiptRow insertReceipt2 = mDB.insertReceiptSerial(mTripRow, insertReceipt1);
		List<ReceiptRow> receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(insertReceipt1);
		assertNotNull(insertReceipt2);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 2);
		assertEquals(insertReceipt1, receipts.get(1));
		assertEquals(insertReceipt2, receipts.get(0));
		ReceiptUtils.assertFieldEquality(insertReceipt1, receipts.get(1));
		ReceiptUtils.assertFieldEquality(insertReceipt2, receipts.get(0));
	}
	
	@Test
	public void insertAndGetById() {
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
		List<ReceiptRow> receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(updateReceipt);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 1);
		assertEquals(updateReceipt, insertReceipt);
		assertEquals(updateReceipt, receipts.get(0));
		ReceiptUtils.assertFieldEqualityPlusIdAndIndex(updateReceipt, receipts.get(0));
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
		List<ReceiptRow> receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(updateReceipt);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 1);
		assertEquals(updateReceipt, insertReceipt);
		assertEquals(updateReceipt, receipts.get(0));
		assertTrue(updateReceipt.hasPDF());
		ReceiptUtils.assertFieldEqualityPlusIdAndIndex(updateReceipt, receipts.get(0));
		
		// Image Update test
		updateReceipt = mDB.updateReceiptFile(insertReceipt, img);
		receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(updateReceipt);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 1);
		assertEquals(updateReceipt, insertReceipt);
		assertEquals(updateReceipt, receipts.get(0));
		assertTrue(updateReceipt.hasImage());
		ReceiptUtils.assertFieldEqualityPlusIdAndIndex(updateReceipt, receipts.get(0));
		
		// Bad File Update test
		updateReceipt = mDB.updateReceiptFile(insertReceipt, bad);
		receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(updateReceipt);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 1);
		assertEquals(updateReceipt, insertReceipt);
		assertEquals(updateReceipt, receipts.get(0));
		assertFalse(updateReceipt.hasFile());
	}
	
	@Test
	public void copyImg() {
		ReceiptRow imgReceipt = insertDefaultReceipt();
		assertTrue(mDB.copyReceiptSerial(imgReceipt, mTripRow2));
		List<ReceiptRow> receipts1 = mDB.getReceiptsSerial(mTripRow);
		List<ReceiptRow> receipts2 = mDB.getReceiptsSerial(mTripRow2);
		assertNotNull(imgReceipt);
		assertNotNull(receipts1);
		assertNotNull(receipts2);
		assertEquals(receipts1.size(), 1);
		assertEquals(receipts2.size(), 1);
		assertTrue(imgReceipt.hasImage());
		assertTrue(receipts1.get(0).hasImage());
		assertTrue(receipts2.get(0).hasImage());
		assertNotEquals(imgReceipt.getTrip(), receipts2.get(0).getTrip());
		assertEquals(imgReceipt, receipts1.get(0)); // receipts2.get(0) will have a different id
		ReceiptUtils.assertFieldEqualityPlusIdAndIndex(imgReceipt, receipts1.get(0));
		assertNotEquals(imgReceipt.getTrip(), receipts2.get(0).getTrip());
		assertEquals(receipts2.get(0).getTrip(), mTripRow2);
		// Since we've verified the trip/image stuff, change them so we can use field equals
		receipts2.get(0).setFile(imgReceipt.getFile());
		receipts2.get(0).setTrip(imgReceipt.getTrip());
		ReceiptUtils.assertFieldEquality(imgReceipt, receipts2.get(0));
	}
	
	@Test
	public void moveImg() {
		ReceiptRow imgReceipt = insertDefaultReceipt();
		assertTrue(mDB.moveReceiptSerial(imgReceipt, mTripRow, mTripRow2));
		List<ReceiptRow> receipts1 = mDB.getReceiptsSerial(mTripRow);
		List<ReceiptRow> receipts2 = mDB.getReceiptsSerial(mTripRow2);
		assertNotNull(imgReceipt);
		assertNotNull(receipts1);
		assertNotNull(receipts2);
		assertEquals(receipts1.size(), 0);
		assertEquals(receipts2.size(), 1);
		assertFalse((new File(mTripRow.getDirectory(), Constants.IMAGE_FILE_NAME).exists()));
		assertFalse(imgReceipt.hasImage());
		assertTrue(receipts2.get(0).hasImage());
		assertNotEquals(imgReceipt.getTrip(), receipts2.get(0).getTrip());
		assertEquals(receipts2.get(0).getTrip(), mTripRow2);
		// Since we've verified the trip/image stuff, change them so we can use field equals
		receipts2.get(0).setFile(imgReceipt.getFile());
		receipts2.get(0).setTrip(imgReceipt.getTrip());
		ReceiptUtils.assertFieldEquality(imgReceipt, receipts2.get(0));	
	}
	
	@Test
	public void delete() {
		ReceiptRow imgReceipt = insertDefaultReceipt();
		assertTrue(mDB.deleteReceiptSerial(imgReceipt, mTripRow));
		assertFalse(mDB.deleteReceiptSerial(imgReceipt, mTripRow)); //Double delete should return false
		List<ReceiptRow> receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 0);
	}
	
	@Test
	public void parentMove() {
		insertDefaultReceipt();
		TripRow newTrip = mDB.updateTripSerial(mTripRow, 
				mApp.getPersistenceManager().getStorageManager().mkdir(co.smartreceipts.tests.utils.TripUtils.Constants.DIRECTORY_NAME + "_new"),
				mTripRow.getStartDate(), 
				mTripRow.getEndDate(), 
				mTripRow.getComment(), 
				mTripRow.getCurrencyCode());
		List<ReceiptRow> receipts = mDB.getReceiptsSerial(newTrip);
		assertNotNull(newTrip);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 1);
		assertEquals(receipts.get(0).getTrip(), newTrip);
		// TODO: Create a coupler class so single actions for db update plus storage update
	}
	
	@Test
	public void moveUp() {
		ReceiptRow receipt1 = insertDefaultReceipt();
		ReceiptRow receipt2 = insertDefaultReceipt();
		List<ReceiptRow> receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 2);
		ReceiptUtils.assertFieldEquality(receipt1, receipts.get(1));
		ReceiptUtils.assertFieldEquality(receipt2, receipts.get(0));
		assertTrue(mDB.moveReceiptUp(mTripRow, receipt1));
		receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 2);
		ReceiptUtils.assertFieldEquality(receipt2, receipts.get(1));
		ReceiptUtils.assertFieldEquality(receipt1, receipts.get(0));
		assertFalse(mDB.moveReceiptUp(mTripRow, receipt1));
		receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 2);
		ReceiptUtils.assertFieldEquality(receipt2, receipts.get(1));
		ReceiptUtils.assertFieldEquality(receipt1, receipts.get(0));
	}
	
	@Test
	public void moveDown() {
		ReceiptRow receipt1 = insertDefaultReceipt();
		ReceiptRow receipt2 = insertDefaultReceipt();
		List<ReceiptRow> receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 2);
		ReceiptUtils.assertFieldEquality(receipt1, receipts.get(1));
		ReceiptUtils.assertFieldEquality(receipt2, receipts.get(0));
		assertTrue(mDB.moveReceiptUp(mTripRow, receipt1));
		receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 2);
		ReceiptUtils.assertFieldEquality(receipt2, receipts.get(1));
		ReceiptUtils.assertFieldEquality(receipt1, receipts.get(0));
		assertFalse(mDB.moveReceiptUp(mTripRow, receipt1));
		receipts = mDB.getReceiptsSerial(mTripRow);
		assertNotNull(receipts);
		assertEquals(receipts.size(), 2);
		ReceiptUtils.assertFieldEquality(receipt2, receipts.get(1));
		ReceiptUtils.assertFieldEquality(receipt1, receipts.get(0));
	}
	
}
