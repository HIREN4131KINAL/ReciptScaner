package co.smartreceipts.android.purchases;

import android.support.annotation.NonNull;

import java.util.Collection;

public final class ProPurchaseCache implements PurchaseCache {

    private final PurchaseWallet mPurchaseWallet;

    public ProPurchaseCache() {
        mPurchaseWallet = new ProPurchaseWallet();
    }


    @NonNull
    @Override
    public PurchaseWallet getSubscriptionWallet() {
        return mPurchaseWallet;
    }

    @Override
    public void addSubscriptionToWallet(@NonNull Subscription subscription) {
        // Intentional No-op
    }

    @Override
    public void updateSubscriptionsInWallet(@NonNull Collection<Subscription> subscriptions) {
        // Intentional No-op
    }

}
