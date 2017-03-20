package co.smartreceipts.android.purchases.model;

import android.support.annotation.NonNull;

public class ConsumablePurchase extends AbstractManagedProduct {

    private int remainingCount;

    public ConsumablePurchase(@NonNull InAppPurchase inAppPurchase, @NonNull String purchaseToken,
                              @NonNull String inAppDataSignature, int remainingCount) {
        super(inAppPurchase, purchaseToken, inAppDataSignature);
        this.remainingCount = remainingCount;
    }

    public int getRemainingCount() {
        return remainingCount;
    }

    public void decrementRemainingCount() {
        this.remainingCount--;
    }
}
