package co.smartreceipts.android.persistence.database.tables.keys;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;

/**
 * Defines the primary key for the {@link co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable}
 */
public final class PaymentMethodPrimaryKey implements PrimaryKey<PaymentMethod, Integer> {

    @Override
    @NonNull
    public String getPrimaryKeyColumn() {
        return PaymentMethodsTable.COLUMN_ID;
    }

    @Override
    @NonNull
    public Class<Integer> getPrimaryKeyClass() {
        return Integer.class;
    }

    @Override
    @NonNull
    public Integer getPrimaryKeyValue(@NonNull PaymentMethod paymentMethod) {
        return paymentMethod.getId();
    }
}
