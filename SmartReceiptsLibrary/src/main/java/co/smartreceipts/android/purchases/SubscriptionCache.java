package co.smartreceipts.android.purchases;

import android.support.annotation.NonNull;

import java.util.Collection;

public interface SubscriptionCache {

    /**
     * Gets the subscription wallet that is stored in this cache
     *
     * @return the current subscription wallet
     */
    @NonNull
    SubscriptionWallet getSubscriptionWallet();

    /**
     * Adds a list of subscriptions to the existing wallet
     *
     * @param subscriptions the subscription to add
     */
    void addSubscriptionsToWallet(@NonNull Collection<Subscription> subscriptions);

    /**
     * Adds a subscription to the existing wallet
     *
     * @param subscription the subscription to add
     */
    void addSubscriptionToWallet(@NonNull Subscription subscription);

}
