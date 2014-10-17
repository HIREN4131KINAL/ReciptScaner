package co.smartreceipts.android;

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
import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.tests.utils.TestUtils;
import co.smartreceipts.tests.utils.TripUtils.Constants;

@Config(emulateSdk = 18) 
@RunWith(RobolectricTestRunner.class)
public class TripsDBTest {

	private SmartReceiptsApplication mApp;
	private DatabaseHelper mDB;

	@Before
	public void setup() {
		mApp = (SmartReceiptsApplication) Robolectric.application;
		mDB = mApp.getPersistenceManager().getDatabase();
	}
	
	@After
	public void tearDown() {
		mDB.close();
		mDB = null;
		mApp = null;
	}
	
	@Test
	public void insert() {
		TripRow insertTrip = insertDefaultTrip();
		TripRow[] trips = mDB.getTripsSerial();
		assertNotNull(insertTrip);
		assertNotNull(trips);
		assertEquals(trips.length, 1);
		assertTrue(getDefaultDirectory().exists());
		assertEquals(insertTrip, trips[0]);
	}
	
	private File getDefaultDirectory() {
		return mApp.getPersistenceManager().getStorageManager().mkdir(Constants.DIRECTORY_NAME);
	}
	
	private TripRow insertDefaultTrip() {
		return mDB.insertTripSerial(getDefaultDirectory(), 
								    Constants.START_DATE, 
								    Constants.END_DATE, 
								    Constants.COMMENT,
								    Constants.CURRENCY_CODE);
	}
	
	@Test
	public void update() {
		File newDir = mApp.getPersistenceManager().getStorageManager().mkdir("newDir");
		Date newStartDate = new Date(Constants.START_DATE_MILLIS + 2000);
		Date newEndDate = new Date(Constants.START_DATE_MILLIS + 2000);
		String newComment = Constants.COMMENT + "_new";
		String newCurrency = "EUR";
		TripRow oldTrip = insertDefaultTrip();
		TripRow newTrip = mDB.updateTripSerial(oldTrip, newDir, newStartDate, newEndDate, newComment, newCurrency);
		TripRow[] trips = mDB.getTripsSerial();
		assertNotNull(oldTrip);
		assertNotNull(newTrip);
		assertNotNull(trips);
		assertEquals(trips.length, 1);
		assertTrue(newDir.exists());
		assertFalse(newTrip.equals(oldTrip));
		assertEquals(newTrip, trips[0]);
	}
	
	@Test
	public void delete() {
		TripRow insertTrip = insertDefaultTrip();
		assertTrue(mDB.deleteTripSerial(insertTrip));
		TripRow[] trips = mDB.getTripsSerial();
		assertNotNull(trips);
		assertEquals(trips.length, 0);
	}
	
	@Test
	public void addMiles() {
		TripRow insertTrip = insertDefaultTrip();
		mDB.addMiles(insertTrip, Float.toString(Constants.MILEAGE));
		mDB.addMiles(insertTrip, Float.toString(Constants.MILEAGE));
		mDB.addMiles(insertTrip, Float.toString(-Constants.MILEAGE));
		TripRow[] trips = mDB.getTripsSerial();
		assertNotNull(insertTrip);
		assertNotNull(trips);
		assertEquals(trips.length, 1);
		assertEquals(insertTrip.getMileage(), Constants.MILEAGE, TestUtils.EPSILON);
		assertEquals(trips[0].getMileage(), Constants.MILEAGE, TestUtils.EPSILON);
	}
	
}
