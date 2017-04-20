package co.smartreceipts.android.ocr.widget.configuration;

import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OcrConfigurationInteractorTest {

    OcrConfigurationInteractor interactor;

    @Mock
    NavigationHandler navigationHandler;

    @Mock
    IdentityManager identityManager;

    @Mock
    OcrPurchaseTracker ocrPurchaseTracker;

    @Mock
    PurchaseManager purchaseManager;

    @Mock
    UserPreferenceManager userPreferenceManager;

    @Mock
    AvailablePurchase availablePurchase, availablePurchase2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        interactor = new OcrConfigurationInteractor(navigationHandler, identityManager, ocrPurchaseTracker, purchaseManager, userPreferenceManager);
    }

    @Test
    public void getEmail() {
        final EmailAddress emailAddress = new EmailAddress("email");
        when(identityManager.getEmail()).thenReturn(emailAddress);
        assertEquals(emailAddress, interactor.getEmail());
    }

    @Test
    public void getRemainingScansStream() {
        final PublishSubject<Integer> scanSubject = PublishSubject.create();
        when(ocrPurchaseTracker.getRemainingScansStream()).thenReturn(scanSubject);

        TestObserver<Integer> testObserver = interactor.getRemainingScansStream().test();
        scanSubject.onNext(61);

        testObserver.assertValue(61);
        testObserver.assertNotComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void getAvailableOcrPurchasesOrdersByPrice() {
        when(availablePurchase.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans50);
        when(availablePurchase2.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans10);
        when(availablePurchase.getPriceAmountMicros()).thenReturn(500000L);
        when(availablePurchase2.getPriceAmountMicros()).thenReturn(100000L);

        final Set<AvailablePurchase> purchaseSet = new HashSet<>(Arrays.asList(availablePurchase, availablePurchase2));
        when(purchaseManager.getAllAvailablePurchases()).thenReturn(Observable.just(purchaseSet));

        TestObserver<List<AvailablePurchase>> testObserver = interactor.getAvailableOcrPurchases().test();

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(Arrays.asList(availablePurchase2, availablePurchase));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void startOcrPurchase() {
        when(availablePurchase.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans50);
        interactor.startOcrPurchase(availablePurchase);
        verify(purchaseManager).initiatePurchase(InAppPurchase.OcrScans50, PurchaseSource.Ocr);
    }

    @Test
    public void getAllowUsToSaveImagesRemotely() {
        when(userPreferenceManager.get(UserPreference.Misc.OcrIncognitoMode)).thenReturn(true);
        assertFalse(interactor.getAllowUsToSaveImagesRemotely());

        when(userPreferenceManager.get(UserPreference.Misc.OcrIncognitoMode)).thenReturn(false);
        assertTrue(interactor.getAllowUsToSaveImagesRemotely());
    }

    @Test
    public void setAllowUsToSaveImagesRemotely() {
        interactor.setAllowUsToSaveImagesRemotely(false);
        verify(userPreferenceManager).set(UserPreference.Misc.OcrIncognitoMode, true);

        interactor.setAllowUsToSaveImagesRemotely(true);
        verify(userPreferenceManager).set(UserPreference.Misc.OcrIncognitoMode, false);
    }

    @Test
    public void routeToProperLocationWhenNotLoggedInForNewSession() {
        when(identityManager.isLoggedIn()).thenReturn(false);
        interactor.routeToProperLocation(null);
        verify(navigationHandler).navigateToLoginScreen();
    }

    @Test
    public void routeToProperLocationWhenNotLoggedInForExistingSession() {
        when(identityManager.isLoggedIn()).thenReturn(false);
        interactor.routeToProperLocation(new Bundle());
        verify(navigationHandler).navigateBack();
    }

    @Test
    public void routeToProperLocationWhenLoggedInForNewSession() {
        when(identityManager.isLoggedIn()).thenReturn(true);
        interactor.routeToProperLocation(null);
        verifyZeroInteractions(navigationHandler);
    }

    @Test
    public void routeToProperLocationWhenLoggedInForExistingSession() {
        when(identityManager.isLoggedIn()).thenReturn(true);
        interactor.routeToProperLocation(new Bundle());
        verifyZeroInteractions(navigationHandler);
    }

    @Test
    public void navigateBack() {
        interactor.navigateBack();
        verify(navigationHandler).navigateBack();
    }


}