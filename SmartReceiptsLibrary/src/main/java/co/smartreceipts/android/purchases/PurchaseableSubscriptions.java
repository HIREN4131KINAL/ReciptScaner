package co.smartreceipts.android.purchases;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates a list of {@link co.smartreceipts.android.purchases.PurchaseableSubscription} to help avoid
 * situations in which we duplicate logic around checking these
 */
public class PurchaseableSubscriptions {

    private final Map<Subscription, PurchaseableSubscription> mPurchaseableSubscriptionMap;
    private final List<PurchaseableSubscription> mPurchaseableSubscriptions;

    PurchaseableSubscriptions(@NonNull List<PurchaseableSubscription> purchaseableSubscriptions) {
        mPurchaseableSubscriptions = Collections.unmodifiableList(purchaseableSubscriptions);
        mPurchaseableSubscriptionMap = new HashMap<>(purchaseableSubscriptions.size());
        for (int i = 0; i < purchaseableSubscriptions.size(); i++) {
            final PurchaseableSubscription purchaseableSubscription = purchaseableSubscriptions.get(i);
            mPurchaseableSubscriptionMap.put(purchaseableSubscription.getSubscription(), purchaseableSubscription);
        }
    }

    /**
     * Checks if a particular subscription is available for purchase or not
     *
     * @param subscription the desired subscription to check
     * @return {@code true} if it is available, {@code false} otherwise
     */
    public boolean isSubscriptionAvailableForPurchase(@NonNull Subscription subscription) {
        return mPurchaseableSubscriptionMap.containsKey(subscription);
    }

    /**
     * @return a {@link java.util.List} of all available {@link co.smartreceipts.android.purchases.PurchaseableSubscription}
     */
    public List<PurchaseableSubscription> getPurchaseableSubscriptions() {
        return mPurchaseableSubscriptions;
    }

    @Override
    public String toString() {
        return "PurchaseableSubscriptions{" + mPurchaseableSubscriptions + '}';
    }
}
