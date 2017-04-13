package co.smartreceipts.android.ocr.purchases;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import rx.observers.TestSubscriber;

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
        final TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        localOcrScansTracker.getRemainingScansStream().subscribe(subscriber);
        assertEquals(0, localOcrScansTracker.getRemainingScans());

        subscriber.assertValues(0);
        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
    }

    @Test
    public void setRemainingScans() {
        final TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        localOcrScansTracker.getRemainingScansStream().subscribe(subscriber);

        localOcrScansTracker.setRemainingScans(50);
        assertEquals(50, localOcrScansTracker.getRemainingScans());

        subscriber.assertValues(0, 50);
        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
    }

    @Test
    public void decrementRemainingScans() {
        final TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        localOcrScansTracker.getRemainingScansStream().subscribe(subscriber);

        localOcrScansTracker.setRemainingScans(50);
        localOcrScansTracker.decrementRemainingScans();
        assertEquals(49, localOcrScansTracker.getRemainingScans());

        subscriber.assertValues(0, 50, 49);
        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
    }

    @Test
    public void decrementRemainingDoesntGoNegative() {
        final TestSubscriber<Integer> subscriber = new TestSubscriber<>();
        localOcrScansTracker.getRemainingScansStream().subscribe(subscriber);

        localOcrScansTracker.decrementRemainingScans();
        assertEquals(0, localOcrScansTracker.getRemainingScans());

        subscriber.assertValues(0);
        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();
    }

}