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
     * Updates the list of subscriptions in the existing wallet
     *
     * @param subscriptions the subscription to add
     */
    void updateSubscriptionsInWallet(@NonNull Collection<Subscription> subscriptions);

}
