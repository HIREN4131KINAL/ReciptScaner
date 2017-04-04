package co.smartreceipts.android.aws.cognito;

import com.amazonaws.regions.Regions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.identity.apis.me.Cognito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SmartReceiptsAuthenticationProviderTest {

    private static final String TOKEN = "token";
    private static final String IDENTITY_ID = "identityId";

    // Class under testing
    SmartReceiptsAuthenticationProvider authenticationProvider;

    @Mock
    CognitoIdentityProvider cognitoIdentityProvider;

    @Mock
    Cognito cognitoToken;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(cognitoIdentityProvider.getCachedCognitoToken()).thenReturn(cognitoToken);
        when(cognitoIdentityProvider.synchronouslyRefreshCognitoToken()).thenReturn(cognitoToken);
        when(cognitoToken.getCognitoToken()).thenReturn(TOKEN);
        when(cognitoToken.getIdentityId()).thenReturn(IDENTITY_ID);
        authenticationProvider = new SmartReceiptsAuthenticationProvider(cognitoIdentityProvider, Regions.US_EAST_1);

        assertTrue(authenticationProvider.isAuthenticated());
    }

    @Test
    public void getProviderName() {
        assertEquals("login.smartreceipts.co", authenticationProvider.getProviderName());
    }

    @Test
    public void getIdentityId() {
        assertEquals(IDENTITY_ID, authenticationProvider.getIdentityId());
    }

    @Test
    public void getNullIdentityId() {
        when(cognitoIdentityProvider.getCachedCognitoToken()).thenReturn(null);
        assertEquals(null, authenticationProvider.getIdentityId());
    }

    @Test
    public void refresh() {
        assertEquals(TOKEN, authenticationProvider.refresh());
    }

    @Test
    public void refreshReturnsNullToken() {
        when(cognitoIdentityProvider.synchronouslyRefreshCognitoToken()).thenReturn(null);
        assertEquals(null, authenticationProvider.refresh());
    }
}