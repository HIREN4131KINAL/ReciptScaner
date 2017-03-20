package co.smartreceipts.android.purchases.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import co.smartreceipts.android.purchases.model.InAppPurchase;

public class DefaultPurchaseWallet implements PurchaseWallet {

    private static final String KEY_SKU_SET = "key_sku_set";

    private final SharedPreferences sharedPreferences;
    private Set<InAppPurchase> ownedInAppPurchases;

    @Inject
    public DefaultPurchaseWallet(@NonNull Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    @VisibleForTesting
    public DefaultPurchaseWallet(@NonNull SharedPreferences preferences) {
        this.sharedPreferences = Preconditions.checkNotNull(preferences);
        ownedInAppPurchases = restoreWallet();
    }

    @Override
    public boolean hasActivePurchase(@NonNull InAppPurchase inAppPurchase) {
        return ownedInAppPurchases.contains(inAppPurchase);
    }

    @Override
    public synchronized void updatePurchasesInWallet(@NonNull Collection<InAppPurchase> inAppPurchases) {
        final Set<InAppPurchase> actualInAppPurchaseSet = new HashSet<>(inAppPurchases);
        if (!actualInAppPurchaseSet.equals(ownedInAppPurchases)) {
            // Only update if we actually added something to the underlying set
            ownedInAppPurchases = actualInAppPurchaseSet;
            persistWallet();
        }
    }

    @Override
    public synchronized void addPurchaseToWallet(@NonNull InAppPurchase inAppPurchase) {
        if (!ownedInAppPurchases.contains(inAppPurchase)) {
            ownedInAppPurchases.add(inAppPurchase);
            persistWallet();
        }
    }

    @NonNull
    private Set<InAppPurchase> restoreWallet() {
        final Set<String> skusSet = sharedPreferences.getStringSet(KEY_SKU_SET, Collections.<String>emptySet());
        final Set<InAppPurchase> inAppPurchases = new HashSet<>(skusSet.size());
        for (final String sku : skusSet) {
            final InAppPurchase inAppPurchase = InAppPurchase.from(sku);
            if (inAppPurchase != null) {
                inAppPurchases.add(inAppPurchase);
            }
        }
        return inAppPurchases;
    }

    private void persistWallet() {
        final Collection<InAppPurchase> inAppPurchases = ownedInAppPurchases;
        final Set<String> skusSet = new HashSet<>(inAppPurchases.size());
        for (final InAppPurchase inAppPurchase : inAppPurchases) {
            skusSet.add(inAppPurchase.getSku());
        }
        sharedPreferences.edit().putStringSet(KEY_SKU_SET, skusSet).apply();
    }
}
