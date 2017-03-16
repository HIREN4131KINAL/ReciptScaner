package co.smartreceipts.android.purchases;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.android.vending.billing.IInAppBillingService;
import com.tom_roush.pdfbox.pdmodel.graphics.predictor.Sub;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.SameThreadExecutorService;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PurchaseManagerTest {

    // Service response codes
    private static final int RESULT_OK = 0;
    private static final int RESULT_USER_CANCELED = 1;
    private static final int RESULT_BILLING_UNAVAILABLE = 3;
    private static final int RESULT_ITEM_UNAVAILABLE = 4;
    private static final int RESULT_DEVELOPER_ERROR = 5;
    private static final int RESULT_ERROR = 6;
    private static final int RESULT_ITEM_ALREADY_OWNED = 7;
    private static final int RESULT_ITEM_NOT_OWNED = 8;

    // Purchase state codes
    private static final int PURCHASE_STATE_PURCHASED = 0;
    private static final int PURCHASE_STATE_CANCELLED = 1;
    private static final int PURCHASE_STATE_REFUNDED = 2;

    // Class under test
    PurchaseManager purchaseManager;

    ShadowApplication shadowApplication = Shadows.shadowOf(RuntimeEnvironment.application);
    
    String packageName = RuntimeEnvironment.application.getPackageName();

    @Mock
    PurchaseWallet purchaseWallet;

    @Mock
    Analytics analytics;

    @Mock
    IBinder binder;

    @Mock
    IInAppBillingService inAppBillingService;

    @Mock
    SubscriptionEventsListener listener1, listener2, listener3;

    @Captor
    ArgumentCaptor<Bundle> bundleCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(binder.queryLocalInterface("com.android.vending.billing.IInAppBillingService")).thenReturn(inAppBillingService);
        shadowApplication.setComponentNameAndServiceForBindService(new ComponentName("com.android.vending.billing", "InAppBillingService"), binder);

        purchaseManager = new PurchaseManager(RuntimeEnvironment.application, purchaseWallet, analytics, new SameThreadExecutorService());
        purchaseManager.addEventListener(listener1);
        purchaseManager.addEventListener(listener2);
        purchaseManager.addEventListener(listener3);
        purchaseManager.removeEventListener(listener3);
    }

    @Test
    public void onCreate() {
        purchaseManager.onCreate();
        verifyInAppBillingServiceConnected();
    }

    @Test
    public void onDestroy() {
        purchaseManager.onCreate();
        verifyInAppBillingServiceConnected();

        purchaseManager.onDestroy();
        verifyInAppBillingServiceDisonnected();
    }

    @Test
    public void getPurchaseWallet() {
        assertEquals(purchaseWallet, purchaseManager.getPurchaseWallet());
    }

    @Test
    public void queryPurchasesThrowsException() throws Exception {
        // Connect
        purchaseManager.onCreate();
        verifyInAppBillingServiceConnected();

        // Configure
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenThrow(new RemoteException());

        // Test
        purchaseManager.querySubscriptions();
        verify(listener1).onSubscriptionsUnavailable();
        verify(listener2).onSubscriptionsUnavailable();
        verifyZeroInteractions(listener3);
        verify(purchaseWallet, never()).updateSubscriptionsInWallet(anyCollectionOf(Subscription.class));
        verify(purchaseWallet, never()).addSubscriptionToWallet(any(Subscription.class));
    }
    
    @Test
    public void queryPurchasesResponseError() throws Exception {
        // Connect
        purchaseManager.onCreate();
        verifyInAppBillingServiceConnected();

        // Configure
        final Bundle getPurchasesResponse = new Bundle();
        getPurchasesResponse.putInt("RESPONSE_CODE", RESULT_ERROR);
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getPurchasesResponse);

        // Test
        purchaseManager.querySubscriptions();
        verify(listener1).onSubscriptionsUnavailable();
        verify(listener2).onSubscriptionsUnavailable();
        verifyZeroInteractions(listener3);
        verify(purchaseWallet, never()).updateSubscriptionsInWallet(anyCollectionOf(Subscription.class));
        verify(purchaseWallet, never()).addSubscriptionToWallet(any(Subscription.class));
    }

    @Test
    public void queryPurchasesButAvailableSkusResponseError() throws Exception {
        // Connect
        purchaseManager.onCreate();
        verifyInAppBillingServiceConnected();

        // Configure
        final Bundle getPurchasesResponse = new Bundle();
        getPurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getPurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<String>());
        getPurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<String>());
        getPurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<String>());
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getPurchasesResponse);

        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_ERROR);
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("subs"), bundleCaptor.capture())).thenReturn(getSkuDetailsResponse);

        // Test
        purchaseManager.querySubscriptions();
        verify(listener1).onSubscriptionsUnavailable();
        verify(listener2).onSubscriptionsUnavailable();
        verifyZeroInteractions(listener3);

        // TODO: Fix me - Don't trigger if either errors
        verify(purchaseWallet).updateSubscriptionsInWallet(Collections.<Subscription>emptyList());
        verify(purchaseWallet, never()).addSubscriptionToWallet(any(Subscription.class));
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), Subscription.getSkus());
    }

    @Test
    public void queryPurchasesWithNoneOwned() throws Exception {
        // Connect
        purchaseManager.onCreate();
        verifyInAppBillingServiceConnected();

        // Configure
        final Bundle getPurchasesResponse = new Bundle();
        getPurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getPurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<String>());
        getPurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<String>());
        getPurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<String>());
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getPurchasesResponse);

        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSkuDetailsResponse.putStringArrayList("DETAILS_LIST", new ArrayList<>(Collections.singletonList(getSkuDetails(Subscription.SmartReceiptsPlus))));
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("subs"), bundleCaptor.capture())).thenReturn(getSkuDetailsResponse);

        // Test
        purchaseManager.querySubscriptions();
        verify(listener1).onSubscriptionsAvailable(any(PurchaseableSubscriptions.class), eq(purchaseWallet));
        verify(listener2).onSubscriptionsAvailable(any(PurchaseableSubscriptions.class), eq(purchaseWallet));
        verifyZeroInteractions(listener3);
        verify(purchaseWallet).updateSubscriptionsInWallet(Collections.<Subscription>emptyList());
        verify(purchaseWallet, never()).addSubscriptionToWallet(any(Subscription.class));
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), Subscription.getSkus());
    }

    @Test
    public void queryPurchasesWithSomeOwned() throws Exception {
        // Connect
        purchaseManager.onCreate();
        verifyInAppBillingServiceConnected();

        // Configure
        final Bundle getPurchasesResponse = new Bundle();
        getPurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getPurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseItem(Subscription.SmartReceiptsPlus))));
        getPurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseData(Subscription.SmartReceiptsPlus, PURCHASE_STATE_PURCHASED))));
        getPurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>(Collections.singletonList(getInAppDataSignature())));
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getPurchasesResponse);

        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSkuDetailsResponse.putStringArrayList("DETAILS_LIST", new ArrayList<String>());
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("subs"), bundleCaptor.capture())).thenReturn(getSkuDetailsResponse);

        // Test
        purchaseManager.querySubscriptions();
        verify(listener1).onSubscriptionsAvailable(any(PurchaseableSubscriptions.class), eq(purchaseWallet));
        verify(listener2).onSubscriptionsAvailable(any(PurchaseableSubscriptions.class), eq(purchaseWallet));
        verifyZeroInteractions(listener3);
        verify(purchaseWallet).updateSubscriptionsInWallet(Collections.singletonList(Subscription.SmartReceiptsPlus));
        verify(purchaseWallet, never()).addSubscriptionToWallet(any(Subscription.class));
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), Subscription.getSkus());
    }

    @Test
    public void queryPurchasesWithSomeOwnedOnLateConnection() throws Exception {
        // Configure
        final Bundle getPurchasesResponse = new Bundle();
        getPurchasesResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getPurchasesResponse.putStringArrayList("INAPP_PURCHASE_ITEM_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseItem(Subscription.SmartReceiptsPlus))));
        getPurchasesResponse.putStringArrayList("INAPP_PURCHASE_DATA_LIST", new ArrayList<>(Collections.singletonList(getInAppPurchaseData(Subscription.SmartReceiptsPlus, PURCHASE_STATE_PURCHASED))));
        getPurchasesResponse.putStringArrayList("INAPP_DATA_SIGNATURE_LIST", new ArrayList<>(Collections.singletonList(getInAppDataSignature())));
        when(inAppBillingService.getPurchases(3, packageName, "subs", null)).thenReturn(getPurchasesResponse);

        final Bundle getSkuDetailsResponse = new Bundle();
        getSkuDetailsResponse.putInt("RESPONSE_CODE", RESULT_OK);
        getSkuDetailsResponse.putStringArrayList("DETAILS_LIST", new ArrayList<String>());
        when(inAppBillingService.getSkuDetails(eq(3), eq(packageName), eq("subs"), bundleCaptor.capture())).thenReturn(getSkuDetailsResponse);

        // Test Late Connect
        purchaseManager.querySubscriptions();
        purchaseManager.onCreate();
        verifyInAppBillingServiceConnected();

        // Verify
        verify(listener1).onSubscriptionsAvailable(any(PurchaseableSubscriptions.class), eq(purchaseWallet));
        verify(listener2).onSubscriptionsAvailable(any(PurchaseableSubscriptions.class), eq(purchaseWallet));
        verifyZeroInteractions(listener3);
        verify(purchaseWallet).updateSubscriptionsInWallet(Collections.singletonList(Subscription.SmartReceiptsPlus));
        verify(purchaseWallet, never()).addSubscriptionToWallet(any(Subscription.class));
        assertEquals(bundleCaptor.getValue().getStringArrayList("ITEM_ID_LIST"), Subscription.getSkus());
    }

    private void verifyInAppBillingServiceConnected() {
        final Intent intent = shadowApplication.getNextStartedService();
        assertNotNull(intent);
        assertEquals(intent.getAction(), "com.android.vending.billing.InAppBillingService.BIND");
        assertFalse(shadowApplication.getBoundServiceConnections().isEmpty());
    }

    private void verifyInAppBillingServiceDisonnected() {
        assertTrue(shadowApplication.getBoundServiceConnections().isEmpty());
        assertFalse(shadowApplication.getUnboundServiceConnections().isEmpty());
    }

    @NonNull
    private String getInAppPurchaseItem(@NonNull Subscription subscription) throws Exception {
        return subscription.getSku();
    }

    @NonNull
    private String getInAppPurchaseData(@NonNull Subscription subscription, int purchaseState) throws Exception {
        // https://developer.android.com/google/play/billing/billing_reference.html
        final JSONObject json = new JSONObject();
        json.put("autoRenewing", true);
        json.put("orderId", "orderId");
        json.put("packageName", packageName);
        json.put("productId", subscription.getSku());
        json.put("purchaseTime", 1234567890123L);
        json.put("purchaseState", purchaseState);
        json.put("developerPayload", "1234567890");
        json.put("purchaseToken", "012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689");
        return json.toString();
    }

    @NonNull
    private String getInAppDataSignature() throws Exception {
        return "012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689==";
    }

    @NonNull
    private String getSkuDetails(@NonNull Subscription subscription) throws Exception {
        // https://developer.android.com/google/play/billing/billing_reference.html
        final JSONObject json = new JSONObject();
        json.put("productId", subscription.getSku());
        json.put("type", "subs"); //TODO
        json.put("price", "$4.99");
        json.put("price_amount_micros", 4990000);
        json.put("price_currency_code", "USD");
        json.put("title", "title");
        json.put("description", "description");
        return json.toString();
    }
}