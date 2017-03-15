package co.smartreceipts.android.purchases.wallet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;

import co.smartreceipts.android.purchases.Subscription;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ProPurchaseWalletTest {

    // Class under test
    ProPurchaseWallet proPurchaseWallet;

    @Before
    public void setUp() {
        proPurchaseWallet = new ProPurchaseWallet();
    }
    
    @Test
    public void hasPlusSubscription() {
        assertTrue(proPurchaseWallet.hasSubscription(Subscription.SmartReceiptsPlus));
    }

    @Test
    public void updatePurchasesDoesNotImpactAnything() {
        proPurchaseWallet.updateSubscriptionsInWallet(Collections.<Subscription>emptyList());
        assertTrue(proPurchaseWallet.hasSubscription(Subscription.SmartReceiptsPlus));

        proPurchaseWallet.updateSubscriptionsInWallet(Collections.singletonList(Subscription.SmartReceiptsPlus));
        assertTrue(proPurchaseWallet.hasSubscription(Subscription.SmartReceiptsPlus));
    }

    @Test
    public void addPurchaseDoesNotImpactAnything() {
        proPurchaseWallet.addSubscriptionToWallet(Subscription.SmartReceiptsPlus);
        assertTrue(proPurchaseWallet.hasSubscription(Subscription.SmartReceiptsPlus));
    }

}