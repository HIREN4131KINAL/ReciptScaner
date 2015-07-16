package co.smartreceipts.android.purchases;

import android.app.PendingIntent;
import android.support.annotation.NonNull;

import java.util.List;

public interface SubscriptionEventsListener {

    /**
     * Called as soon as we have successfully queried a list of available subscriptions
     *
     * @param subscriptions a list of subscriptions for purchase
     * @param subscriptionWallet a wallet of owned subscriptions
     */
    void onSubscriptionsAvailable(@NonNull List<PurchaseableSubscription> subscriptions, @NonNull SubscriptionWallet subscriptionWallet);

    /**
     * Called if we failed to find to query subscriptions
     */
    void onSubscriptionsUnavailable();

    /**
     * Called if we were able to successfully fetch the purchase intent
     *
     * @param subscription the subscription to purchase
     * @param pendingIntent the intent for the purchase
     * @param key the key required for the purchase
     */
    void onPurchaseIntentAvailable(@NonNull Subscription subscription, @NonNull PendingIntent pendingIntent, @NonNull String key);

    /**
     * Called if we failed to fetch the purchase intent
     *
     * @param subscription the subscription we tried to purchase
     */
    void onPurchaseIntentUnavailable(@NonNull Subscription subscription);

    /**
     * Called if we successfully completed a purchase
     *
     * @param subscription the new subscription that we purchased
     * @param updateSubscriptionWallet the updated subscription wallet
     */
    void onPurchaseSuccess(@NonNull Subscription subscription, @NonNull SubscriptionWallet updateSubscriptionWallet);

    /**
     * Called if we failed to complete a purchase
     */
    void onPurchaseFailed();
}
