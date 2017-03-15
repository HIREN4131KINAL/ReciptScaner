package co.smartreceipts.android.purchases.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import co.smartreceipts.android.purchases.Subscription;

public class DefaultPurchaseWallet implements PurchaseWallet {

    private static final String KEY_SKU_SET = "key_sku_set";

    private final SharedPreferences sharedPreferences;
    private Set<Subscription> ownedSubscriptions;

    public DefaultPurchaseWallet(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public DefaultPurchaseWallet(@NonNull SharedPreferences preferences) {
        this.sharedPreferences = Preconditions.checkNotNull(preferences);
        ownedSubscriptions = restoreWallet();
    }

    @Override
    public boolean hasSubscription(@NonNull Subscription subscription) {
        return ownedSubscriptions.contains(subscription);
    }

    @Override
    public synchronized void updateSubscriptionsInWallet(@NonNull Collection<Subscription> subscriptions) {
        final Set<Subscription> actualSubscriptionSet = new HashSet<>(subscriptions);
        if (!actualSubscriptionSet.equals(ownedSubscriptions)) {
            // Only update if we actually added something to the underlying set
            ownedSubscriptions = actualSubscriptionSet;
            persistWallet();
        }
    }

    @Override
    public synchronized void addSubscriptionToWallet(@NonNull Subscription subscription) {
        if (!ownedSubscriptions.contains(subscription)) {
            ownedSubscriptions.add(subscription);
            persistWallet();
        }
    }

    @NonNull
    private Set<Subscription> restoreWallet() {
        final Set<String> skusSet = sharedPreferences.getStringSet(KEY_SKU_SET, Collections.<String>emptySet());
        final Set<Subscription> subscriptions = new HashSet<>(skusSet.size());
        for (final String sku : skusSet) {
            final Subscription subscription = Subscription.from(sku);
            if (subscription != null) {
                subscriptions.add(subscription);
            }
        }
        return subscriptions;
    }

    private void persistWallet() {
        final Collection<Subscription> subscriptions = ownedSubscriptions;
        final Set<String> skusSet = new HashSet<>(subscriptions.size());
        for (final Subscription subscription : subscriptions) {
            skusSet.add(subscription.getSku());
        }
        sharedPreferences.edit().putStringSet(KEY_SKU_SET, skusSet).apply();
    }
}
