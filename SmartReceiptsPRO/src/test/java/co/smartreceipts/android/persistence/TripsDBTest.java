package co.smartreceipts.android.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.sql.Date;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.testutils.TestUtils;
import co.smartreceipts.android.testutils.TripUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        Trip insertTrip = insertDefaultTrip();
        Trip[] trips = mDB.getTripsSerial();
        assertNotNull(insertTrip);
        assertNotNull(trips);
        assertEquals(trips.length, 1);
        assertTrue(getDefaultDirectory().exists());
        assertEquals(insertTrip, trips[0]);
    }

    private File getDefaultDirectory() {
        return mApp.getPersistenceManager().getStorageManager().mkdir(TripUtils.Constants.DIRECTORY_NAME);
    }

    private Trip insertDefaultTrip() {
        return mDB.insertTripSerial(getDefaultDirectory(),
                TripUtils.Constants.START_DATE,
                TripUtils.Constants.END_DATE,
                TripUtils.Constants.COMMENT,
                TripUtils.Constants.COST_CENTER,
                TripUtils.Constants.CURRENCY_CODE);
    }

    @Test
    public void update() {
        File newDir = mApp.getPersistenceManager().getStorageManager().mkdir("newDir");
        Date newStartDate = new Date(TripUtils.Constants.START_DATE_MILLIS + 2000);
        Date newEndDate = new Date(TripUtils.Constants.START_DATE_MILLIS + 2000);
        String newComment = TripUtils.Constants.COMMENT + "_new";
        String newCostCenter = TripUtils.Constants.COST_CENTER + "_new";
        String newCurrency = "EUR";
        Trip oldTrip = insertDefaultTrip();
        Trip newTrip = mDB.updateTripSerial(oldTrip, newDir, newStartDate, newEndDate, newComment, newCostCenter, newCurrency);
        Trip[] trips = mDB.getTripsSerial();
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
        Trip insertTrip = insertDefaultTrip();
        assertTrue(mDB.deleteTripSerial(insertTrip));
        Trip[] trips = mDB.getTripsSerial();
        assertNotNull(trips);
        assertEquals(trips.length, 0);
    }

}
