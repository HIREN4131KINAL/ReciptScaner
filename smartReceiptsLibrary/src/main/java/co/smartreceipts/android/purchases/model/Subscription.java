package co.smartreceipts.android.purchases.model;

import android.support.annotation.NonNull;

public class Subscription extends AbstractManagedProduct {

    public Subscription(@NonNull String sku) {
        super(sku);
    }
}
