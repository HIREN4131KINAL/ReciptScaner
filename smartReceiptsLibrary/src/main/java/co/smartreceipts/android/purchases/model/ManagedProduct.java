package co.smartreceipts.android.purchases.model;

import android.support.annotation.NonNull;

public interface ManagedProduct {

    /**
     * @return the unique {@link String} identifier (ie stock keeping unit) for this product
     */
    @NonNull
    String getSku();
}
