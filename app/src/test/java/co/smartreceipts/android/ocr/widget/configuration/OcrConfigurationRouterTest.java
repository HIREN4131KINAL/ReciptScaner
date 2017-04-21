package co.smartreceipts.android.ocr.widget.configuration;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.activities.DaggerFragmentNavigationHandler;
import co.smartreceipts.android.identity.IdentityManager;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OcrConfigurationRouterTest {

    @InjectMocks
    OcrConfigurationRouter router;

    @Mock
    DaggerFragmentNavigationHandler<OcrConfigurationFragment> navigationHandler;

    @Mock
    IdentityManager identityManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void navigateToProperLocationWhenNotLoggedInForNewSession() {
        when(identityManager.isLoggedIn()).thenReturn(false);
        router.navigateToProperLocation(null);
        verify(navigationHandler).navigateToLoginScreen();
    }

    @Test
    public void navigateToProperLocationWhenNotLoggedInForExistingSession() {
        when(identityManager.isLoggedIn()).thenReturn(false);
        router.navigateToProperLocation(new Bundle());
        verify(navigationHandler).navigateBack();
    }

    @Test
    public void navigateToProperLocationWhenLoggedInForNewSession() {
        when(identityManager.isLoggedIn()).thenReturn(true);
        router.navigateToProperLocation(null);
        verifyZeroInteractions(navigationHandler);
    }

    @Test
    public void navigateToProperLocationWhenLoggedInForExistingSession() {
        when(identityManager.isLoggedIn()).thenReturn(true);
        router.navigateToProperLocation(new Bundle());
        verifyZeroInteractions(navigationHandler);
    }

    @Test
    public void navigateBack() {
        router.navigateBack();
        verify(navigationHandler).navigateBack();
    }
    
}