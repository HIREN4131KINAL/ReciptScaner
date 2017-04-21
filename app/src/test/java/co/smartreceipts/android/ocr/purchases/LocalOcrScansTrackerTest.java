package co.smartreceipts.android.ocr.purchases;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import io.reactivex.observers.TestObserver;

import static org.junit.Assert.assertEquals;

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
        TestObserver<Integer> testObserver = localOcrScansTracker.getRemainingScansStream().test();
        assertEquals(0, localOcrScansTracker.getRemainingScans());

        testObserver.assertValues(0);
        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void setRemainingScans() {
        TestObserver<Integer> testObserver = localOcrScansTracker.getRemainingScansStream().test();

        localOcrScansTracker.setRemainingScans(50);
        assertEquals(50, localOcrScansTracker.getRemainingScans());

        testObserver.assertValues(0, 50);
        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void decrementRemainingScans() {
        TestObserver<Integer> testObserver = localOcrScansTracker.getRemainingScansStream().test();

        localOcrScansTracker.setRemainingScans(50);
        localOcrScansTracker.decrementRemainingScans();
        assertEquals(49, localOcrScansTracker.getRemainingScans());

        testObserver.assertValues(0, 50, 49);
        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void decrementRemainingDoesntGoNegative() {
        TestObserver<Integer> testObserver = localOcrScansTracker.getRemainingScansStream().test();

        localOcrScansTracker.decrementRemainingScans();
        assertEquals(0, localOcrScansTracker.getRemainingScans());

        testObserver.assertValues(0);
        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
    }

}