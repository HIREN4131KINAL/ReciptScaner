package co.smartreceipts.android.purchases;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DefaultSubscriptionCache implements SubscriptionCache {

    private static final String KEY_SKU_SET = "key_sku_set";

    private final SharedPreferences mPreferences;
    private SubscriptionWallet mSubscriptionWallet;

    public DefaultSubscriptionCache(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public DefaultSubscriptionCache(@NonNull SharedPreferences preferences) {
        mPreferences = preferences;
        mSubscriptionWallet = restoreWallet();
    }


    @Override
    public synchronized void updateSubscriptionsInWallet(@NonNull Collection<Subscription> subscriptions) {
        final Set<Subscription> actualSubscriptionSet = new HashSet<>(subscriptions);
        final Set<Subscription> knownSubscriptionSet = new HashSet<>(mSubscriptionWallet.getOwnedSubscriptions());
        if (!actualSubscriptionSet.equals(knownSubscriptionSet)) {
            // Only update if we actually added something to the underlying set
            mSubscriptionWallet = new DefaultSubscriptionWallet(actualSubscriptionSet);
            persistWallet(mSubscriptionWallet);
        }
    }

    @NonNull
    @Override
    public synchronized SubscriptionWallet getSubscriptionWallet() {
        return mSubscriptionWallet;
    }

    @NonNull
    private SubscriptionWallet restoreWallet() {
        final Set<String> skusSet = mPreferences.getStringSet(KEY_SKU_SET, Collections.<String>emptySet());
        final Set<Subscription> subscriptions = new HashSet<>(skusSet.size());
        for (final String sku : skusSet) {
            final Subscription subscription = Subscription.from(sku);
            if (subscription != null) {
                subscriptions.add(subscription);
            }
        }
        return new DefaultSubscriptionWallet(subscriptions);
    }

    private void persistWallet(@NonNull SubscriptionWallet subscriptionWallet) {
        final Collection<Subscription> subscriptions = subscriptionWallet.getOwnedSubscriptions();
        final Set<String> skusSet = new HashSet<>(subscriptions.size());
        for (final Subscription subscription : subscriptions) {
            skusSet.add(subscription.getSku());
        }
        mPreferences.edit().putStringSet(KEY_SKU_SET, skusSet).apply();
    }

}
