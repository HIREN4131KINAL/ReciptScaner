package co.smartreceipts.android.purchases;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class DefaultPurchaseCache implements PurchaseCache {

    private static final String KEY_SKU_SET = "key_sku_set";

    private final SharedPreferences mPreferences;
    private PurchaseWallet mPurchaseWallet;

    public DefaultPurchaseCache(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public DefaultPurchaseCache(@NonNull SharedPreferences preferences) {
        mPreferences = preferences;
        mPurchaseWallet = restoreWallet();
    }


    @Override
    public synchronized void updateSubscriptionsInWallet(@NonNull Collection<Subscription> subscriptions) {
        final Set<Subscription> actualSubscriptionSet = new HashSet<>(subscriptions);
        final Set<Subscription> knownSubscriptionSet = new HashSet<>(mPurchaseWallet.getOwnedSubscriptions());
        if (!actualSubscriptionSet.equals(knownSubscriptionSet)) {
            // Only update if we actually added something to the underlying set
            mPurchaseWallet = new DefaultPurchaseWallet(actualSubscriptionSet);
            persistWallet(mPurchaseWallet);
        }
    }

    @NonNull
    @Override
    public synchronized PurchaseWallet getSubscriptionWallet() {
        return mPurchaseWallet;
    }

    @Override
    public synchronized void addSubscriptionToWallet(@NonNull Subscription subscription) {
        final Set<Subscription> subscriptions = new HashSet<>(mPurchaseWallet.getOwnedSubscriptions());
        if (!subscriptions.contains(subscription)) {
            subscriptions.add(subscription);
            mPurchaseWallet = new DefaultPurchaseWallet(subscriptions);
            persistWallet(mPurchaseWallet);
        }
    }

    @NonNull
    private PurchaseWallet restoreWallet() {
        final Set<String> skusSet = mPreferences.getStringSet(KEY_SKU_SET, Collections.<String>emptySet());
        final Set<Subscription> subscriptions = new HashSet<>(skusSet.size());
        for (final String sku : skusSet) {
            final Subscription subscription = Subscription.from(sku);
            if (subscription != null) {
                subscriptions.add(subscription);
            }
        }
        return new DefaultPurchaseWallet(subscriptions);
    }

    private void persistWallet(@NonNull PurchaseWallet purchaseWallet) {
        final Collection<Subscription> subscriptions = purchaseWallet.getOwnedSubscriptions();
        final Set<String> skusSet = new HashSet<>(subscriptions.size());
        for (final Subscription subscription : subscriptions) {
            skusSet.add(subscription.getSku());
        }
        mPreferences.edit().putStringSet(KEY_SKU_SET, skusSet).apply();
    }

}
