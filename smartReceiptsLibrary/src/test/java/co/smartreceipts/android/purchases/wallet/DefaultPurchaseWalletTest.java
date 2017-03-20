package co.smartreceipts.android.purchases.wallet;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Collections;

import co.smartreceipts.android.purchases.model.InAppPurchase;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class DefaultPurchaseWalletTest {

    // Class under test
    DefaultPurchaseWallet defaultPurchaseWallet;

    SharedPreferences preferences;

    @Before
    public void setUp() {
        preferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        defaultPurchaseWallet = new DefaultPurchaseWallet(preferences);
    }

    @After
    public void tearDown() {
        preferences.edit().clear().apply();
    }

    @Test
    public void emptyPurchases() {
        assertFalse(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
    }

    @Test
    public void singlePurchase() {
        defaultPurchaseWallet.addPurchaseToWallet(InAppPurchase.SmartReceiptsPlus);

        assertTrue(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
    }

    @Test
    public void updatePurchases() {
        defaultPurchaseWallet.updatePurchasesInWallet(Collections.singletonList(InAppPurchase.SmartReceiptsPlus));

        assertTrue(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
    }

    @Test
    public void ensureAddedPurchaseIsPersisted() {
        defaultPurchaseWallet.addPurchaseToWallet(InAppPurchase.SmartReceiptsPlus);
        final PurchaseWallet newWallet = new DefaultPurchaseWallet(preferences);

        assertTrue(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertTrue(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
    }

    @Test
    public void ensureUpdatedPurchaseListIsPersisted() {
        // First add it
        defaultPurchaseWallet.addPurchaseToWallet(InAppPurchase.SmartReceiptsPlus);

        // Then revoke it
        defaultPurchaseWallet.updatePurchasesInWallet(Collections.<InAppPurchase>emptySet());
        final PurchaseWallet newWallet = new DefaultPurchaseWallet(preferences);

        assertFalse(newWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
        assertFalse(defaultPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
    }

}