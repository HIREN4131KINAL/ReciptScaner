package co.smartreceipts.android.purchases.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import org.json.JSONException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.model.ManagedProductFactory;
import co.smartreceipts.android.utils.log.Logger;

public class DefaultPurchaseWallet implements PurchaseWallet {

    private static final String KEY_SKU_SET = "key_sku_set";
    private static final String FORMAT_KEY_PURCHASE_DATA = "%s_purchaseData";
    private static final String FORMAT_KEY_IN_APP_DATA_SIGNATURE = "%s_inAppDataSignature";

    private final SharedPreferences sharedPreferences;
    private final Map<InAppPurchase, ManagedProduct> ownedInAppPurchasesMap;

    @Inject
    public DefaultPurchaseWallet(Context context) {
        this(PreferenceManager.getDefaultSharedPreferences(context));
    }

    @VisibleForTesting
    protected DefaultPurchaseWallet(@NonNull SharedPreferences preferences) {
        this.sharedPreferences = Preconditions.checkNotNull(preferences);
        ownedInAppPurchasesMap = restoreWallet();
    }

    @NonNull
    @Override
    public Set<ManagedProduct> getActivePurchases() {
        return new HashSet<>(ownedInAppPurchasesMap.values());
    }

    @Override
    public synchronized boolean hasActivePurchase(@NonNull InAppPurchase inAppPurchase) {
        return ownedInAppPurchasesMap.containsKey(inAppPurchase);
    }

    @Nullable
    @Override
    public synchronized ManagedProduct getManagedProduct(@NonNull InAppPurchase inAppPurchase) {
        return ownedInAppPurchasesMap.get(inAppPurchase);
    }

    @Override
    public synchronized void updatePurchasesInWallet(@NonNull Set<ManagedProduct> managedProducts) {
        final Map<InAppPurchase, ManagedProduct> actualInAppPurchasesMap = new HashMap<>();
        for (final ManagedProduct managedProduct : managedProducts) {
            actualInAppPurchasesMap.put(managedProduct.getInAppPurchase(), managedProduct);
        }
        if (!actualInAppPurchasesMap.equals(ownedInAppPurchasesMap)) {
            // Only update if we actually added something to the underlying set
            ownedInAppPurchasesMap.clear();
            ownedInAppPurchasesMap.putAll(actualInAppPurchasesMap);
            persistWallet();
        }
    }

    @Override
    public synchronized void removePurchaseFromWallet(@NonNull InAppPurchase inAppPurchase) {
        final ManagedProduct managedProduct = ownedInAppPurchasesMap.remove(inAppPurchase);
        if (managedProduct != null) {
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(getKeyForPurchaseData(inAppPurchase));
            editor.remove(getKeyForInAppDataSignature(inAppPurchase));
            editor.apply();
            persistWallet(); // And persist our sku set
        }
    }

    @Override
    public synchronized void addPurchaseToWallet(@NonNull ManagedProduct managedProduct) {
        if (!ownedInAppPurchasesMap.containsKey(managedProduct.getInAppPurchase())) {
            ownedInAppPurchasesMap.put(managedProduct.getInAppPurchase(), managedProduct);
            persistWallet();
        }
    }

    @NonNull
    private Map<InAppPurchase, ManagedProduct> restoreWallet() {
        final Set<String> skusSet = sharedPreferences.getStringSet(KEY_SKU_SET, Collections.<String>emptySet());
        final Map<InAppPurchase, ManagedProduct> inAppPurchasesMap = new HashMap<>();
        for (final String sku : skusSet) {
            final InAppPurchase inAppPurchase = InAppPurchase.from(sku);
            if (inAppPurchase != null) {
                final String purchaseData = sharedPreferences.getString(getKeyForPurchaseData(inAppPurchase), "");
                final String inAppDataSignature = sharedPreferences.getString(getKeyForInAppDataSignature(inAppPurchase), "");
                try {
                    final ManagedProduct managedProduct = new ManagedProductFactory(inAppPurchase, purchaseData, inAppDataSignature).get();
                    inAppPurchasesMap.put(inAppPurchase, managedProduct);
                } catch (JSONException e) {
                    Logger.error(this, "Failed to parse the purchase data for " + inAppPurchase, e);
                }
            }
        }
        return inAppPurchasesMap;
    }

    private void persistWallet() {
        final Set<InAppPurchase> ownedInAppPurchases = new HashSet<>(ownedInAppPurchasesMap.keySet());
        final Set<String> skusSet = new HashSet<>();
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // Clear our our existing set before calling the new ones

        for (final InAppPurchase inAppPurchase : ownedInAppPurchases) {
            final ManagedProduct managedProduct = ownedInAppPurchasesMap.get(inAppPurchase);
            skusSet.add(inAppPurchase.getSku());
            editor.putString(getKeyForPurchaseData(inAppPurchase), managedProduct.getPurchaseData());
            editor.putString(getKeyForInAppDataSignature(inAppPurchase), managedProduct.getInAppDataSignature());
        }
        editor.putStringSet(KEY_SKU_SET, skusSet);
        editor.apply();
    }

    @NonNull
    private String getKeyForPurchaseData(@NonNull InAppPurchase inAppPurchase) {
        return String.format(Locale.US, FORMAT_KEY_PURCHASE_DATA, inAppPurchase.getSku());
    }

    @NonNull
    private String getKeyForInAppDataSignature(@NonNull InAppPurchase inAppPurchase) {
        return String.format(Locale.US, FORMAT_KEY_IN_APP_DATA_SIGNATURE, inAppPurchase.getSku());
    }

}
