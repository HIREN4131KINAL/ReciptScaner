package co.smartreceipts.android.purchases.wallet;

import android.support.annotation.NonNull;

import java.util.Collection;

import javax.inject.Inject;

import co.smartreceipts.android.purchases.model.InAppPurchase;

public final class ProPurchaseWallet implements PurchaseWallet {

    @Inject
    public ProPurchaseWallet() {
    }

    @Override
    public boolean hasSubscription(@NonNull InAppPurchase inAppPurchase) {
        return true;
    }

    @Override
    public void addSubscriptionToWallet(@NonNull InAppPurchase inAppPurchase) {
        // No-op
    }

    @Override
    public void updateSubscriptionsInWallet(@NonNull Collection<InAppPurchase> inAppPurchases) {
        // No-op
    }
}
