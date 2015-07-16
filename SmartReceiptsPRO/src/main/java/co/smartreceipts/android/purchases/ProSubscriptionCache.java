package co.smartreceipts.android.purchases;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ProSubscriptionCache implements SubscriptionCache {

    private final SubscriptionWallet mSubscriptionWallet;

    public ProSubscriptionCache() {
        // For pro users, just add all subscriptions
        final List<Subscription> subscriptions = Arrays.asList(Subscription.values());
        mSubscriptionWallet = new DefaultSubscriptionWallet(subscriptions);
    }


    @NonNull
    @Override
    public SubscriptionWallet getSubscriptionWallet() {
        return mSubscriptionWallet;
    }

    @Override
    public void addSubscriptionsToWallet(@NonNull Collection<Subscription> subscriptions) {
        // Intentional No-op
    }

    @Override
    public void addSubscriptionToWallet(@NonNull Subscription subscription) {
        // Intentional No-op
    }
}
