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
public class OcrScansTrackerTest {

    // Class under test
    OcrScansTracker ocrScansTracker;

    SharedPreferences preferences;

    @Before
    public void setUp() {
        preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        ocrScansTracker = new OcrScansTracker(preferences);
    }

    @After
    public void tearDown() {
        preferences.edit().clear().apply();
    }

    @Test
    public void getRemainingScans() {
        assertEquals(0, ocrScansTracker.getRemainingScans());
    }

    @Test
    public void setRemainingScans() {
        ocrScansTracker.setRemainingScans(50);
        assertEquals(50, ocrScansTracker.getRemainingScans());
    }

    @Test
    public void decrementRemainingScans() {
        ocrScansTracker.setRemainingScans(50);
        ocrScansTracker.decrementRemainingScans();
        assertEquals(49, ocrScansTracker.getRemainingScans());
    }

    @Test
    public void decrementRemainingDoesntGoNegative() {
        ocrScansTracker.decrementRemainingScans();
        assertEquals(0, ocrScansTracker.getRemainingScans());
    }

}