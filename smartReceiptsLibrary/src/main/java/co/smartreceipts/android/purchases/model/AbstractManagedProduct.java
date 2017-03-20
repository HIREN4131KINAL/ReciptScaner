package co.smartreceipts.android.purchases.model;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

abstract class AbstractManagedProduct implements ManagedProduct {

    private final String sku;

    public AbstractManagedProduct(@NonNull String sku) {
        this.sku = Preconditions.checkNotNull(sku);
    }

    @NonNull
    @Override
    public String getSku() {
        return sku;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractManagedProduct)) return false;

        AbstractManagedProduct that = (AbstractManagedProduct) o;

        return sku.equals(that.sku);

    }

    @Override
    public int hashCode() {
        return sku.hashCode();
    }
}
