package co.smartreceipts.android.purchases;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum Subscription {

    SmartReceiptsPro("pro_sku_3");

    private final String mSku;

    Subscription(@NonNull String sku) {
        mSku = sku;
    }

    @NonNull
    public String getSku() {
        return mSku;
    }

    @Nullable
    public static Subscription from(@NonNull String sku) {
        for (final Subscription subscription : values()) {
            if (subscription.getSku().equals(sku)) {
                return subscription;
            }
        }
        return null;
    }

    @NonNull
    public static ArrayList<String> getSkus() {
        final ArrayList<String> skus = new ArrayList<>(values().length);
        for (final Subscription subscription : values()) {
            skus.add(subscription.getSku());
        }
        return skus;
    }
}