package co.smartreceipts.android.purchases.wallet;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Collections;

import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PlusPurchaseWalletTest {

    private static final String PURCHASE_TOKEN = "012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689";
    private static final String IN_APP_DATA_SIGNATURE = "012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689012345689ABCDEF012345689ABCDEF012345689ABCDEF012345689==";
    private String purchaseData;

    // Class under test
    PlusPurchaseWallet plusPurchaseWallet;

    SharedPreferences preferences;

    @Mock
    ManagedProduct managedProduct;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // https://developer.android.com/google/play/billing/billing_reference.html
        final JSONObject purchaseData = new JSONObject();
        purchaseData.put("autoRenewing", true);
        purchaseData.put("orderId", "orderId");
        purchaseData.put("packageName", "co.smartreceipts.android");
        purchaseData.put("productId", InAppPurchase.SmartReceiptsPlus.getSku());
        purchaseData.put("purchaseTime", 1234567890123L);
        purchaseData.put("purchaseState", 0);
        purchaseData.put("developerPayload", "1234567890");
        purchaseData.put("purchaseToken", PURCHASE_TOKEN);
        this.purchaseData = purchaseData.toString();

        when(managedProduct.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans50);
        when(managedProduct.getPurchaseData()).thenReturn(this.purchaseData);
        when(managedProduct.getPurchaseToken()).thenReturn(PURCHASE_TOKEN);
        when(managedProduct.getInAppDataSignature()).thenReturn(IN_APP_DATA_SIGNATURE);

        preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        plusPurchaseWallet = new PlusPurchaseWallet(preferences);
    }

    @After
    public void tearDown() {
        preferences.edit().clear().apply();
    }

    @Test
    public void emptyPurchases() {
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertFalse(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
    }

    @Test
    public void singlePurchase() {
        plusPurchaseWallet.addPurchaseToWallet(managedProduct);

        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.OcrScans50.getSku()));
        assertEquals(preferences.getString("TODO_OCR_TODO_purchaseData", null), this.purchaseData);
        assertEquals(preferences.getString("TODO_OCR_TODO_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void updatePurchases() {
        plusPurchaseWallet.updatePurchasesInWallet(Collections.singleton(managedProduct));

        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.OcrScans50.getSku()));
        assertEquals(preferences.getString("TODO_OCR_TODO_purchaseData", null), this.purchaseData);
        assertEquals(preferences.getString("TODO_OCR_TODO_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void removeMissingPurchase() {
        plusPurchaseWallet.removePurchaseFromWallet(InAppPurchase.OcrScans50);

        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.<String>emptySet());
        assertFalse(preferences.contains("TODO_OCR_TODO_purchaseData"));
        assertFalse(preferences.contains("TODO_OCR_TODO_inAppDataSignature"));
    }

    @Test
    public void ensureAddedPurchaseIsPersistedAndPlusRemains() {
        plusPurchaseWallet.addPurchaseToWallet(managedProduct);
        final PurchaseWallet newWallet = new PlusPurchaseWallet(preferences);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(newWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.singleton(InAppPurchase.OcrScans50.getSku()));
        assertEquals(preferences.getString("TODO_OCR_TODO_purchaseData", null), this.purchaseData);
        assertEquals(preferences.getString("TODO_OCR_TODO_inAppDataSignature", null), IN_APP_DATA_SIGNATURE);
    }

    @Test
    public void ensureUpdatedPurchaseListIsPersistedAndPlusRemains() {
        // First add it
        plusPurchaseWallet.addPurchaseToWallet(managedProduct);

        // Then revoke it
        plusPurchaseWallet.updatePurchasesInWallet(Collections.<ManagedProduct>emptySet());
        final PurchaseWallet newWallet = new PlusPurchaseWallet(preferences);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertFalse(newWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertFalse(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.emptySet());
        assertFalse(preferences.contains("TODO_OCR_TODO_purchaseData"));
        assertFalse(preferences.contains("TODO_OCR_TODO_inAppDataSignature"));
    }

    @Test
    public void ensureRemovedPurchaseIsPersisted() {
        plusPurchaseWallet.addPurchaseToWallet(managedProduct);
        plusPurchaseWallet.removePurchaseFromWallet(InAppPurchase.OcrScans50);

        final PurchaseWallet newWallet = new PlusPurchaseWallet(preferences);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertFalse(newWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertFalse(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertEquals(preferences.getStringSet("key_sku_set", Collections.<String>emptySet()), Collections.emptySet());
        assertFalse(preferences.contains("TODO_OCR_TODO_purchaseData"));
        assertFalse(preferences.contains("TODO_OCR_TODO_inAppDataSignature"));
    }

}