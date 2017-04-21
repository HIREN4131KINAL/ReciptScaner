package co.smartreceipts.android.push.store;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import io.reactivex.observers.TestObserver;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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
        final TestObserver<Boolean> testObserver = pushDataStore.isRemoteRefreshRequiredSingle().test();
        testObserver.onNext(true);
        testObserver.assertComplete()
                .assertNoErrors();
    }

    @Test
    public void setRemoteRefreshRequired() throws Exception {
        pushDataStore.setRemoteRefreshRequired(false);
        assertFalse(pushDataStore.isRemoteRefreshRequired());
        final TestObserver<Boolean> testObserver = pushDataStore.isRemoteRefreshRequiredSingle().test();
        testObserver.onNext(false);
        testObserver.assertComplete()
                .assertNoErrors();
    }

}