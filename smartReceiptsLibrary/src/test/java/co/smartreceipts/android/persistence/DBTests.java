package co.smartreceipts.android.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import co.smartreceipts.android.SmartReceiptsApplication;

@RunWith(RobolectricGradleTestRunner.class)
public class DBTests {

    private SmartReceiptsApplication mApp;
    private DatabaseHelper mDB;

    @Before
    public void setup() {
        mApp = (SmartReceiptsApplication) RuntimeEnvironment.application;
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
