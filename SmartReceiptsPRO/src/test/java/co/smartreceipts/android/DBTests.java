package co.smartreceipts.android;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.persistence.DatabaseHelper;

@Config(emulateSdk = 18) 
@RunWith(RobolectricTestRunner.class)
public class DBTests {

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
	public void onCreate() {
		// assertTrue(false);
		// Define me
	}
	
	@Test
	public void onUpgrade() {
		// assertTrue(false);
		// Define me
	}
	
	@Test
	public void merge() {
		// assertTrue(false);
		// Define me
	}
	
}
