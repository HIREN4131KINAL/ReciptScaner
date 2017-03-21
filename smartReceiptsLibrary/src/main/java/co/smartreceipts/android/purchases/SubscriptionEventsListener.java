package co.smartreceipts.android.purchases;

import android.app.PendingIntent;
import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;

public interface SubscriptionEventsListener {

    /**
     * Called as soon as we have successfully queried a list of available purchases
     *
     *  @param availablePurchases a list of available {@link InAppPurchase}s
     *
     */
    void onPurchasesAvailable(@NonNull List<InAppPurchase> availablePurchases);

    /**
     * Called if we failed to query any items for purchase
     */
    void onPurchasesUnavailable();

    /**
     * Called if we were able to successfully fetch the purchase intent
     *
     * @param inAppPurchase the subscription to purchase
     * @param pendingIntent the intent for the purchase
     * @param key the key required for the purchase
     */
    void onPurchaseIntentAvailable(@NonNull InAppPurchase inAppPurchase, @NonNull PendingIntent pendingIntent, @NonNull String key);

    /**
     * Called if we failed to fetch the purchase intent
     *
     * @param inAppPurchase the subscription we tried to purchase
     */
    void onPurchaseIntentUnavailable(@NonNull InAppPurchase inAppPurchase);

    /**
     * Called if we successfully completed a purchase
     *
     * @param inAppPurchase the new subscription that we purchased
     * @param purchaseSource where the purchase flow was initiated
     * @param updatedPurchaseWallet the updated subscription wallet
     */
    void onPurchaseSuccess(@NonNull InAppPurchase inAppPurchase, @NonNull PurchaseSource purchaseSource, @NonNull PurchaseWallet updatedPurchaseWallet);

    /**
     * Called if we failed to complete a purchase
     *
     * @param purchaseSource where the purchase flow was initiated
     */
    void onPurchaseFailed(@NonNull PurchaseSource purchaseSource);
}
