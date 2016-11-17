package co.smartreceipts.android.purchases;

import android.support.annotation.NonNull;

import java.util.Collection;

public interface SubscriptionWallet {

    /**
     * Gets a complete list of all owned subscriptions
     *
     * @return an immutable collection of owned subscriptions
     */
    @NonNull
    Collection<Subscription> getOwnedSubscriptions();

    /**
     * Checks if this user owns a particular subscription for this application
     *
     * @param subscription the subscription to check for
     * @return {@code true} if it's owned. {@code false} otherwise
     */
    boolean hasSubscription(@NonNull Subscription subscription);

}
