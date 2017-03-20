package co.smartreceipts.android.purchases.model;

import android.support.annotation.NonNull;

public class Subscription extends AbstractManagedProduct {

    public Subscription(@NonNull InAppPurchase inAppPurchase, @NonNull String purchaseToken,
                        @NonNull String inAppDataSignature) {
        super(inAppPurchase, purchaseToken, inAppDataSignature);
    }
}
