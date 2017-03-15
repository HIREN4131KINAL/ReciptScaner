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

import co.smartreceipts.android.purchases.Subscription;

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
        assertFalse(defaultPurchaseWallet.hasSubscription(Subscription.SmartReceiptsPlus));
    }

    @Test
    public void singlePurchase() {
        defaultPurchaseWallet.addSubscriptionToWallet(Subscription.SmartReceiptsPlus);

        assertTrue(defaultPurchaseWallet.hasSubscription(Subscription.SmartReceiptsPlus));
    }

    @Test
    public void updatePurchases() {
        defaultPurchaseWallet.updateSubscriptionsInWallet(Collections.singletonList(Subscription.SmartReceiptsPlus));

        assertTrue(defaultPurchaseWallet.hasSubscription(Subscription.SmartReceiptsPlus));
    }

    @Test
    public void ensureAddedPurchaseIsPersisted() {
        defaultPurchaseWallet.addSubscriptionToWallet(Subscription.SmartReceiptsPlus);
        final PurchaseWallet newWallet = new DefaultPurchaseWallet(preferences);

        assertTrue(newWallet.hasSubscription(Subscription.SmartReceiptsPlus));
        assertTrue(defaultPurchaseWallet.hasSubscription(Subscription.SmartReceiptsPlus));
    }

    @Test
    public void ensureUpdatedPurchaseListIsPersisted() {
        // First add it
        defaultPurchaseWallet.addSubscriptionToWallet(Subscription.SmartReceiptsPlus);

        // Then revoke it
        defaultPurchaseWallet.updateSubscriptionsInWallet(Collections.<Subscription>emptySet());
        final PurchaseWallet newWallet = new DefaultPurchaseWallet(preferences);

        assertFalse(newWallet.hasSubscription(Subscription.SmartReceiptsPlus));
        assertFalse(defaultPurchaseWallet.hasSubscription(Subscription.SmartReceiptsPlus));
    }

}