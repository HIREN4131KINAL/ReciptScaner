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
        assertFalse(defaultPurchaseWallet.hasSubscription(InAppPurchase.SmartReceiptsPlus));
    }

    @Test
    public void singlePurchase() {
        defaultPurchaseWallet.addSubscriptionToWallet(InAppPurchase.SmartReceiptsPlus);

        assertTrue(defaultPurchaseWallet.hasSubscription(InAppPurchase.SmartReceiptsPlus));
    }

    @Test
    public void updatePurchases() {
        defaultPurchaseWallet.updateSubscriptionsInWallet(Collections.singletonList(InAppPurchase.SmartReceiptsPlus));

        assertTrue(defaultPurchaseWallet.hasSubscription(InAppPurchase.SmartReceiptsPlus));
    }

    @Test
    public void ensureAddedPurchaseIsPersisted() {
        defaultPurchaseWallet.addSubscriptionToWallet(InAppPurchase.SmartReceiptsPlus);
        final PurchaseWallet newWallet = new DefaultPurchaseWallet(preferences);

        assertTrue(newWallet.hasSubscription(InAppPurchase.SmartReceiptsPlus));
        assertTrue(defaultPurchaseWallet.hasSubscription(InAppPurchase.SmartReceiptsPlus));
    }

    @Test
    public void ensureUpdatedPurchaseListIsPersisted() {
        // First add it
        defaultPurchaseWallet.addSubscriptionToWallet(InAppPurchase.SmartReceiptsPlus);

        // Then revoke it
        defaultPurchaseWallet.updateSubscriptionsInWallet(Collections.<InAppPurchase>emptySet());
        final PurchaseWallet newWallet = new DefaultPurchaseWallet(preferences);

        assertFalse(newWallet.hasSubscription(InAppPurchase.SmartReceiptsPlus));
        assertFalse(defaultPurchaseWallet.hasSubscription(InAppPurchase.SmartReceiptsPlus));
    }

}