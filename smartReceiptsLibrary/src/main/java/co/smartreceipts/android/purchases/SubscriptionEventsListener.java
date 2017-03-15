package co.smartreceipts.android.purchases;

import android.app.PendingIntent;
import android.support.annotation.NonNull;

import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;

public interface SubscriptionEventsListener {

    /**
     * Called as soon as we have successfully queried a list of available subscriptions
     *
     * @param purchaseableSubscriptions a list of subscriptions for purchase
     * @param purchaseWallet a wallet of owned subscriptions
     */
    void onSubscriptionsAvailable(@NonNull PurchaseableSubscriptions purchaseableSubscriptions, @NonNull PurchaseWallet purchaseWallet);

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
     * @param purchaseSource where the purchase flow was initiated
     * @param updatedPurchaseWallet the updated subscription wallet
     */
    void onPurchaseSuccess(@NonNull Subscription subscription, @NonNull PurchaseSource purchaseSource, @NonNull PurchaseWallet updatedPurchaseWallet);

    /**
     * Called if we failed to complete a purchase
     *
     * @param purchaseSource where the purchase flow was initiated
     */
    void onPurchaseFailed(@NonNull PurchaseSource purchaseSource);
}
