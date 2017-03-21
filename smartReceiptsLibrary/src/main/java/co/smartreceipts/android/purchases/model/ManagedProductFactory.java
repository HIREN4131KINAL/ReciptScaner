package co.smartreceipts.android.purchases.model;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

public class ManagedProductFactory {

    private final InAppPurchase inAppPurchase;
    private final String purchaseToken;
    private final String inAppDataSignature;

    public ManagedProductFactory(@NonNull InAppPurchase inAppPurchase, @NonNull String purchaseToken,
                                  @NonNull String inAppDataSignature) {
        this.inAppPurchase = Preconditions.checkNotNull(inAppPurchase);
        this.purchaseToken = Preconditions.checkNotNull(purchaseToken);
        this.inAppDataSignature = Preconditions.checkNotNull(inAppDataSignature);
    }

    @NonNull
    public ManagedProduct get() {
        if (Subscription.class.equals(inAppPurchase.getType())) {
            return new Subscription(inAppPurchase, purchaseToken, inAppDataSignature);
        } else if (ConsumablePurchase.class.equals(inAppPurchase.getType())) {
            return new ConsumablePurchase(inAppPurchase, purchaseToken, inAppDataSignature);
        } else {
            throw new IllegalArgumentException("Unsupported purchase type");
        }
    }
}
