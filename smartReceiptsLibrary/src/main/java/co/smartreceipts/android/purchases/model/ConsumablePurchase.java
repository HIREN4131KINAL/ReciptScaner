package co.smartreceipts.android.purchases.model;

import android.support.annotation.NonNull;

public class ConsumablePurchase extends AbstractManagedProduct {

    private final int purchaseQuantity;
    private int remainingCount;

    public ConsumablePurchase(@NonNull String sku, int purchaseQuantity) {
        super(sku);
        this.purchaseQuantity = purchaseQuantity;
    }

    public int getPurchaseQuantity() {
        return purchaseQuantity;
    }

    public int getRemainingCount() {
        return remainingCount;
    }

    public void setRemainingCount(int remainingCount) {
        this.remainingCount = remainingCount;
    }
}
