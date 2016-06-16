package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable}
 */
public final class PaymentMethodDatabaseAdapter implements DatabaseAdapter<PaymentMethod, PrimaryKey<PaymentMethod, Integer>> {

    @NonNull
    @Override
    public PaymentMethod read(@NonNull Cursor cursor) {
        final int idIndex = cursor.getColumnIndex(PaymentMethodsTable.COLUMN_ID);
        final int methodIndex = cursor.getColumnIndex(PaymentMethodsTable.COLUMN_METHOD);

        final int id = cursor.getInt(idIndex);
        final String method = cursor.getString(methodIndex);
        return new PaymentMethodBuilderFactory().setId(id).setMethod(method).build();
    }

    @NonNull
    @Override
    public ContentValues write(@NonNull PaymentMethod paymentMethod) {
        final ContentValues values = new ContentValues();
        values.put(PaymentMethodsTable.COLUMN_METHOD, paymentMethod.getMethod());
        return values;
    }

    @NonNull
    @Override
    public PaymentMethod build(@NonNull PaymentMethod paymentMethod, @NonNull PrimaryKey<PaymentMethod, Integer> primaryKey) {
        return new PaymentMethodBuilderFactory().setId(primaryKey.getPrimaryKeyValue(paymentMethod)).setMethod(paymentMethod.getMethod()).build();
    }
}
