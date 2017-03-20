package co.smartreceipts.android.purchases.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public enum InAppPurchase {

    SmartReceiptsPlus("pro_sku_3");

    private final String sku;

    InAppPurchase(@NonNull String sku) {
        this.sku = sku;
    }

    @NonNull
    public String getSku() {
        return sku;
    }

    @Nullable
    public static InAppPurchase from(@NonNull String sku) {
        for (final InAppPurchase inAppPurchase : values()) {
            if (inAppPurchase.getSku().equals(sku)) {
                return inAppPurchase;
            }
        }
        return null;
    }

    @NonNull
    public static ArrayList<String> getSkus() {
        final ArrayList<String> skus = new ArrayList<>(values().length);
        for (final InAppPurchase inAppPurchase : values()) {
            skus.add(inAppPurchase.getSku());
        }
        return skus;
    }
}