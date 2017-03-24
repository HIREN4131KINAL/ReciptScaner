package co.smartreceipts.android.purchases.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.ArrayList;

public enum InAppPurchase {

    SmartReceiptsPlus(Subscription.class, "pro_sku_3"),
    OcrScans50(ConsumablePurchase.class, "TODO_OCR_TODO");

    private final Class<? extends ManagedProduct> type;
    private final String sku;

    InAppPurchase(@NonNull Class<? extends ManagedProduct> type, @NonNull String sku) {
        this.type = Preconditions.checkNotNull(type);
        this.sku = Preconditions.checkNotNull(sku);
    }

    /**
     * @return the unique {@link String} identifier (ie stock keeping unit) for this product
     */
    @NonNull
    public String getSku() {
        return sku;
    }

    /**
     * @return the type of {@link ManagedProduct} that this is
     */
    @NonNull
    public Class<? extends ManagedProduct> getType() {
        return type;
    }

    /**
     * @return the {@link String} of the Google product type (ie "inapp" or "subs")
     */
    @NonNull
    public String getProductType() {
        if (ConsumablePurchase.class.equals(type)) {
            return ConsumablePurchase.GOOGLE_PRODUCT_TYPE;
        } else {
            return Subscription.GOOGLE_PRODUCT_TYPE;
        }
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
            if (ConsumablePurchase.class.equals(inAppPurchase.getType())) {
                skus.add(inAppPurchase.getSku());
            }
        }
        return skus;
    }

    @NonNull
    public static ArrayList<String> getSubscriptionSkus() {
        final ArrayList<String> skus = new ArrayList<>(values().length);
        for (final InAppPurchase inAppPurchase : values()) {
            if (Subscription.class.equals(inAppPurchase.getType())) {
                skus.add(inAppPurchase.getSku());

            }
        }
        return skus;
    }
}