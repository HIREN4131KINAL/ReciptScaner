package co.smartreceipts.android.persistence.database.tables.keys;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.persistence.database.tables.columns.PaymentMethodsTableColumns;

/**
 * Defines the primary key for the {@link co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable}
 */
public final class PaymentMethodPrimaryKey implements PrimaryKey<PaymentMethod, Integer> {

    @Override
    @NonNull
    public String getPrimaryKeyColumn() {
        return PaymentMethodsTableColumns.COLUMN_ID;
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
