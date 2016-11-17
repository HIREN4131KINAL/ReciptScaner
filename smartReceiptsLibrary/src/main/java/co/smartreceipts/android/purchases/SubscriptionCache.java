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
     * Adds a subscriptions to the existing wallet
     *
     * @param subscription the subscription to add
     */
    void addSubscriptionToWallet(@NonNull Subscription subscription);

    /**
     * Updates the list of subscriptions in the existing wallet
     *
     * @param subscriptions the subscriptions to add
     */
    void updateSubscriptionsInWallet(@NonNull Collection<Subscription> subscriptions);

}
