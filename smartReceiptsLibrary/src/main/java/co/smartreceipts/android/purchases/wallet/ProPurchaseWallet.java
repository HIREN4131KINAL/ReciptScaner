package co.smartreceipts.android.purchases.wallet;

import android.support.annotation.NonNull;

import java.util.Collection;

import javax.inject.Inject;

import co.smartreceipts.android.purchases.Subscription;

public final class ProPurchaseWallet implements PurchaseWallet {

    @Inject
    public ProPurchaseWallet() {
    }

    @Override
    public boolean hasSubscription(@NonNull Subscription subscription) {
        return true;
    }

    @Override
    public void addSubscriptionToWallet(@NonNull Subscription subscription) {
        // No-op
    }

    @Override
    public void updateSubscriptionsInWallet(@NonNull Collection<Subscription> subscriptions) {
        // No-op
    }
}
