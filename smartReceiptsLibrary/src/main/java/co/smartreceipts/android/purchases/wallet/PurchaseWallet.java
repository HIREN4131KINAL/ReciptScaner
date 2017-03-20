package co.smartreceipts.android.purchases.wallet;

import android.support.annotation.NonNull;

import java.util.Collection;

import co.smartreceipts.android.purchases.model.InAppPurchase;

public interface PurchaseWallet {

    /**
     * Checks if this user owns a particular subscription for this application
     *
     * @param inAppPurchase the subscription to check for
     * @return {@code true} if it's owned. {@code false} otherwise
     */
    boolean hasSubscription(@NonNull InAppPurchase inAppPurchase);

    /**
     * Adds a subscriptions to the existing wallet
     *
     * @param inAppPurchase the subscription to add
     */
    void addSubscriptionToWallet(@NonNull InAppPurchase inAppPurchase);

    /**
     * Updates the list of subscriptions in the existing wallet
     *
     * @param inAppPurchases the subscriptions to add
     */
    void updateSubscriptionsInWallet(@NonNull Collection<InAppPurchase> inAppPurchases);

}
