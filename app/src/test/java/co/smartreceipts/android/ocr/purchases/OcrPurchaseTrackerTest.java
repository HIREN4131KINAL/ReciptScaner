package co.smartreceipts.android.ocr.purchases;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;
import java.util.Set;

import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.apis.MobileAppPurchasesService;
import co.smartreceipts.android.purchases.apis.PurchaseRequest;
import co.smartreceipts.android.purchases.apis.PurchaseResponse;
import co.smartreceipts.android.purchases.model.ConsumablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import rx.Observable;
import rx.schedulers.Schedulers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OcrPurchaseTrackerTest {

    // Class under test
    OcrPurchaseTracker ocrPurchaseTracker;

    @Mock
    ServiceManager serviceManager;

    @Mock
    PurchaseManager purchaseManager;

    @Mock
    PurchaseWallet purchaseWallet;

    @Mock
    LocalOcrScansTracker localOcrScansTracker;

    @Mock
    ConsumablePurchase consumablePurchase;

    @Mock
    MobileAppPurchasesService mobileAppPurchasesService;

    @Mock
    PurchaseResponse purchaseResponse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(consumablePurchase.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans50);
        when(consumablePurchase.getInAppDataSignature()).thenReturn("");
        when(consumablePurchase.getPurchaseData()).thenReturn("");
        when(purchaseWallet.getManagedProduct(InAppPurchase.OcrScans50)).thenReturn(consumablePurchase);
        when(serviceManager.getService(MobileAppPurchasesService.class)).thenReturn(mobileAppPurchasesService);
        ocrPurchaseTracker = new OcrPurchaseTracker(serviceManager, purchaseManager, purchaseWallet, localOcrScansTracker, Schedulers.immediate());
    }

    @Test
    public void initializeThrowsException() {
        // Configure
        when(purchaseManager.getAllOwnedPurchases()).thenReturn(Observable.<Set<ManagedProduct>>error(new Exception("test")));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(purchaseManager, never()).consumePurchase(any(ConsumablePurchase.class));
        verifyZeroInteractions(serviceManager);
    }

    @Test
    public void initializeUploadFails() {
        // Configure
        when(purchaseManager.getAllOwnedPurchases()).thenReturn(Observable.just(Collections.singleton((ManagedProduct)consumablePurchase)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.<PurchaseResponse>error(new Exception("test")));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(purchaseManager, never()).consumePurchase(any(ConsumablePurchase.class));
    }

    @Test
    public void initializeSucceeds() {
        // Configure
        when(purchaseManager.getAllOwnedPurchases()).thenReturn(Observable.just(Collections.singleton((ManagedProduct)consumablePurchase)));
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.initialize();

        // Verify
        verify(purchaseManager).addEventListener(ocrPurchaseTracker);
        verify(purchaseManager).consumePurchase(consumablePurchase);
    }

    @Test
    public void onPurchaseSuccessForUnTrackedType() {
        // Configure


        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.SmartReceiptsPlus, PurchaseSource.Unknown);

        // Verify
        verify(purchaseManager, never()).consumePurchase(any(ConsumablePurchase.class));
        verifyZeroInteractions(serviceManager);
    }

    @Test
    public void onPurchaseSuccessUploadFails() {
        // Configure
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.<PurchaseResponse>error(new Exception("test")));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(purchaseManager, never()).consumePurchase(any(ConsumablePurchase.class));
    }

    @Test
    public void onPurchaseSuccessSucceeds() {
        // Configure
        when(mobileAppPurchasesService.addPurchase(any(PurchaseRequest.class))).thenReturn(Observable.just(purchaseResponse));

        // Test
        ocrPurchaseTracker.onPurchaseSuccess(InAppPurchase.OcrScans50, PurchaseSource.Unknown);

        // Verify
        verify(purchaseManager).consumePurchase(consumablePurchase);
    }

    @Test
    public void onPurchaseFailed() {
        ocrPurchaseTracker.onPurchaseFailed(PurchaseSource.Unknown);
        verifyZeroInteractions(serviceManager, purchaseManager, purchaseWallet, localOcrScansTracker);
    }

    @Test
    public void getRemainingScans() {
        when(localOcrScansTracker.getRemainingScans()).thenReturn(50);
        assertEquals(50, ocrPurchaseTracker.getRemainingScans());
    }

    @Test
    public void hasAvailableScans() {
        when(localOcrScansTracker.getRemainingScans()).thenReturn(50);
        assertTrue(ocrPurchaseTracker.hasAvailableScans());

        when(localOcrScansTracker.getRemainingScans()).thenReturn(0);
        assertFalse(ocrPurchaseTracker.hasAvailableScans());
    }

    @Test
    public void decrementRemainingScans() {
        ocrPurchaseTracker.decrementRemainingScans();
        verify(localOcrScansTracker).decrementRemainingScans();
    }
}
