package co.smartreceipts.android.purchases;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class ProSubscriptionCache implements SubscriptionCache {

    private final SubscriptionWallet mSubscriptionWallet;

    public ProSubscriptionCache() {
        mSubscriptionWallet = new ProSubscriptionWallet();
    }


    @NonNull
    @Override
    public SubscriptionWallet getSubscriptionWallet() {
        return mSubscriptionWallet;
    }

    @Override
    public void updateSubscriptionsInWallet(@NonNull Collection<Subscription> subscriptions) {
        // Intentional No-op
    }

}
