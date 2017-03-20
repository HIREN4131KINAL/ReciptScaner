package co.smartreceipts.android.purchases.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.ArrayList;

public enum InAppPurchase {

    SmartReceiptsPlus(new Subscription("pro_sku_3")),
    OcrScans50(new ConsumablePurchase("TODO_OCR_TODO", 50));

    private final ManagedProduct managedProduct;

    InAppPurchase(@NonNull ManagedProduct managedProduct) {
        this.managedProduct = Preconditions.checkNotNull(managedProduct);
    }

    @NonNull
    public String getSku() {
        return managedProduct.getSku();
    }

    @NonNull
    public ManagedProduct getManagedProduct() {
        return managedProduct;
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
    public static ArrayList<String> getConsumablePurchaseSkus() {
        final ArrayList<String> skus = new ArrayList<>(values().length);
        for (final InAppPurchase inAppPurchase : values()) {
            if (ConsumablePurchase.class.equals(inAppPurchase.managedProduct.getClass())) {
                skus.add(inAppPurchase.getSku());
            }
        }
        return skus;
    }

    @NonNull
    public static ArrayList<String> getSubscriptionSkus() {
        final ArrayList<String> skus = new ArrayList<>(values().length);
        for (final InAppPurchase inAppPurchase : values()) {
            if (Subscription.class.equals(inAppPurchase.managedProduct.getClass())) {
                skus.add(inAppPurchase.getSku());

            }
        }
        return skus;
    }
}