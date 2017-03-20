package co.smartreceipts.android.purchases.wallet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;

import co.smartreceipts.android.purchases.model.InAppPurchase;

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
        assertTrue(proPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
    }

    @Test
    public void updatePurchasesDoesNotImpactAnything() {
        proPurchaseWallet.updatePurchasesInWallet(Collections.<InAppPurchase>emptyList());
        assertTrue(proPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));

        proPurchaseWallet.updatePurchasesInWallet(Collections.singletonList(InAppPurchase.SmartReceiptsPlus));
        assertTrue(proPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
    }

    @Test
    public void addPurchaseDoesNotImpactAnything() {
        proPurchaseWallet.addPurchaseToWallet(InAppPurchase.SmartReceiptsPlus);
        assertTrue(proPurchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus));
    }

}