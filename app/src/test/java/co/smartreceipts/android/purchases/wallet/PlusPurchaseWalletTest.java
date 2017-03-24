package co.smartreceipts.android.purchases.wallet;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    // Class under test
    PlusPurchaseWallet plusPurchaseWallet;

    SharedPreferences preferences;

    @Mock
    ManagedProduct managedProduct;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(managedProduct.getInAppPurchase()).thenReturn(InAppPurchase.OcrScans50);
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
    }

    @Test
    public void updatePurchasesKeepsPlus() {
        plusPurchaseWallet.updatePurchasesInWallet(Collections.singletonList(managedProduct));

        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
    }

    @Test
    public void ensureAddedPurchaseIsPersistedAndPlusRemains() {
        plusPurchaseWallet.addPurchaseToWallet(managedProduct);
        final PurchaseWallet newWallet = new PlusPurchaseWallet(preferences);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(newWallet.hasActivePurchase(InAppPurchase.OcrScans50));
        assertTrue(plusPurchaseWallet.hasActivePurchase(InAppPurchase.OcrScans50));
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
    }

}