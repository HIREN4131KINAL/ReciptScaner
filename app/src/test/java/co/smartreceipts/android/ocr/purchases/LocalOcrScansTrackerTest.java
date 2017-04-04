package co.smartreceipts.android.ocr.purchases;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class LocalOcrScansTrackerTest {

    // Class under test
    LocalOcrScansTracker localOcrScansTracker;

    SharedPreferences preferences;

    @Before
    public void setUp() {
        preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        localOcrScansTracker = new LocalOcrScansTracker(preferences);
    }

    @After
    public void tearDown() {
        preferences.edit().clear().apply();
    }

    @Test
    public void getRemainingScans() {
        assertEquals(0, localOcrScansTracker.getRemainingScans());
    }

    @Test
    public void setRemainingScans() {
        localOcrScansTracker.setRemainingScans(50);
        assertEquals(50, localOcrScansTracker.getRemainingScans());
    }

    @Test
    public void decrementRemainingScans() {
        localOcrScansTracker.setRemainingScans(50);
        localOcrScansTracker.decrementRemainingScans();
        assertEquals(49, localOcrScansTracker.getRemainingScans());
    }

    @Test
    public void decrementRemainingDoesntGoNegative() {
        localOcrScansTracker.decrementRemainingScans();
        assertEquals(0, localOcrScansTracker.getRemainingScans());
    }

}