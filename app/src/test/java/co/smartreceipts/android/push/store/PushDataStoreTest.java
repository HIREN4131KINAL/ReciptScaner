package co.smartreceipts.android.push.store;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import rx.Observable;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class PushDataStoreTest {

    // Class under test
    PushDataStore pushDataStore;

    SharedPreferences sharedPreferences;

    @Before
    public void setUp() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        pushDataStore = new PushDataStore(sharedPreferences);
    }

    @After
    public void tearDown() {
        sharedPreferences.edit().clear().apply();
    }

    @Test
    public void isRemoteRefreshRequiredDefaultsToTrue() throws Exception {
        assertTrue(pushDataStore.isRemoteRefreshRequired());
        final TestSubscriber<Boolean> testSubscriber1 = new TestSubscriber<>();
        pushDataStore.isRemoteRefreshRequiredObservable().subscribe(testSubscriber1);
        testSubscriber1.onNext(true);
        testSubscriber1.assertCompleted();
        testSubscriber1.assertNoErrors();
    }

    @Test
    public void setRemoteRefreshRequired() throws Exception {
        pushDataStore.setRemoteRefreshRequired(false);
        assertFalse(pushDataStore.isRemoteRefreshRequired());
        final TestSubscriber<Boolean> testSubscriber1 = new TestSubscriber<>();
        pushDataStore.isRemoteRefreshRequiredObservable().subscribe(testSubscriber1);
        testSubscriber1.onNext(false);
        testSubscriber1.assertCompleted();
        testSubscriber1.assertNoErrors();
    }

}