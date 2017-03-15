package co.smartreceipts.android.purchases.wallet;

import android.support.annotation.NonNull;

import java.util.Collection;

import co.smartreceipts.android.purchases.Subscription;

public interface PurchaseWallet {

    /**
     * Checks if this user owns a particular subscription for this application
     *
     * @param subscription the subscription to check for
     * @return {@code true} if it's owned. {@code false} otherwise
     */
    boolean hasSubscription(@NonNull Subscription subscription);

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
