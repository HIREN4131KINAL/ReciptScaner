package co.smartreceipts.android.identity.cognito;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import co.smartreceipts.android.identity.apis.me.Cognito;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class LocalCognitoTokenStoreTest {

    private static final String TOKEN = "token";
    private static final String IDENTITY_ID = "identityId";
    private static final long EXPIRES_AT = 5;

    // Class under test
    LocalCognitoTokenStore localCognitoTokenStore;

    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);

    @Before
    public void setUp() {
        localCognitoTokenStore = new LocalCognitoTokenStore(preferences);
    }

    @After
    public void tearDown() {
        preferences.edit().clear().apply();
    }

    @Test
    public void nothingPersisted() {
        assertEquals(new Cognito(null, null, -1), localCognitoTokenStore.getCognitoToken());
    }

    @Test
    public void persist() {
        final Cognito cognito = new Cognito(TOKEN, IDENTITY_ID, EXPIRES_AT);
        localCognitoTokenStore.persist(cognito);
        assertEquals(cognito, localCognitoTokenStore.getCognitoToken());

        localCognitoTokenStore.persist(null);
        assertEquals(new Cognito(null, null, -1), localCognitoTokenStore.getCognitoToken());
    }

}