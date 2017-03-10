package co.smartreceipts.android.identity.store;

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
public class MutableIdentityStoreTest {

    // Class under test
    MutableIdentityStore mutableIdentityStore;

    SharedPreferences sharedPreferences;

    @Before
    public void setUp() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        mutableIdentityStore = new MutableIdentityStore(sharedPreferences);
    }

    @After
    public void tearDown() {
        sharedPreferences.edit().clear().apply();
    }

    @Test
    public void defaultValues() {
        assertEquals(false, mutableIdentityStore.isLoggedIn());
        assertNull(mutableIdentityStore.getEmail());
        assertNull(mutableIdentityStore.getToken());
    }

    @Test
    public void setEmailAddress() {
        final String email = "test@test.com";
        mutableIdentityStore.setEmailAddress(email);

        assertEquals(false, mutableIdentityStore.isLoggedIn());
        assertNull(mutableIdentityStore.getToken());
        assertNotNull(mutableIdentityStore.getEmail());
        assertEquals(email, mutableIdentityStore.getEmail().getId());
    }

    @Test
    public void setToken() {
        final String token = "token";
        mutableIdentityStore.setToken(token);

        assertEquals(false, mutableIdentityStore.isLoggedIn());
        assertNull(mutableIdentityStore.getEmail());
        assertNotNull(mutableIdentityStore.getToken());
        assertEquals(token, mutableIdentityStore.getToken().getId());
    }

    @Test
    public void setEmailAddressAndToken() {
        final String email = "test@test.com";
        final String token = "token";
        mutableIdentityStore.setEmailAddress(email);
        mutableIdentityStore.setToken(token);

        assertEquals(true, mutableIdentityStore.isLoggedIn());
        assertNotNull(mutableIdentityStore.getEmail());
        assertNotNull(mutableIdentityStore.getToken());
        assertEquals(email, mutableIdentityStore.getEmail().getId());
        assertEquals(token, mutableIdentityStore.getToken().getId());
    }

}