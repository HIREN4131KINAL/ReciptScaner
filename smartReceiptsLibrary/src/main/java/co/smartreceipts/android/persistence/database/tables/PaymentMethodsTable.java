package co.smartreceipts.android.persistence.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.tables.columns.PDFTableColumns;
import co.smartreceipts.android.persistence.database.tables.columns.PaymentMethodsTableColumns;

public final class PaymentMethodsTable extends AbstractSqlTable<PaymentMethod> {

    private List<PaymentMethod> mPaymentMethods;

    public PaymentMethodsTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        super(sqLiteOpenHelper);
    }

    @Override
    public String getTableName() {
        return PaymentMethodsTableColumns.TABLE_NAME;
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        this.createPaymentMethodsTable(db, customizer);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);
        if (oldVersion <= 11) {
            this.createPaymentMethodsTable(db, customizer);
        }
    }

    private void createPaymentMethodsTable(final SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        final String sql = "CREATE TABLE " + getTableName() + " (" +
                           PDFTableColumns.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           PaymentMethodsTableColumns.COLUMN_METHOD + " TEXT" + ");";
        db.execSQL(sql);
        customizer.insertPaymentMethodDefaults(this);
    }

    /**
     * Fetches the list of all {@link PaymentMethod}. This is done on the calling thread.
     *
     * @return the {@link List} of {@link PaymentMethod} objects that we've saved
     */
    public synchronized List<PaymentMethod> get() {
        if (mPaymentMethods != null) {
            return mPaymentMethods;
        }
        mPaymentMethods = new ArrayList<>();

        Cursor c = null;
        try {
            c = getReadableDatabase().query(getTableName(), null, null, null, null, null, null);
            if (c != null && c.moveToFirst()) {
                final int idIndex = c.getColumnIndex(PaymentMethodsTableColumns.COLUMN_ID);
                final int methodIndex = c.getColumnIndex(PaymentMethodsTableColumns.COLUMN_METHOD);
                do {
                    final int id = c.getInt(idIndex);
                    final String method = c.getString(methodIndex);
                    final PaymentMethodBuilderFactory builder = new PaymentMethodBuilderFactory();
                    mPaymentMethods.add(builder.setId(id).setMethod(method).build());
                }
                while (c.moveToNext());
            }
            return new ArrayList<>(mPaymentMethods);
        } finally { // Close the cursor and db to avoid memory leaks
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Attempts to fetch a payment method for a given primary key id
     *
     * @param id - the id of the desired {@link PaymentMethod}
     * @return a {@link PaymentMethod} if the id matches or {@code null} if none is found
     */
    public synchronized PaymentMethod findPaymentMethodById(final int id) {
        final List<PaymentMethod> methodsSnapshot = new ArrayList<>(get());
        final int size = methodsSnapshot.size();
        for (int i = 0; i < size; i++) {
            final PaymentMethod method = methodsSnapshot.get(i);
            if (method.getId() == id) {
                return method;
            }
        }
        return null;
    }

    /**
     * Inserts a new {@link PaymentMethod} into our database. This method also automatically updates the underlying list
     * that is returned from {@link #get()}. This is done on the calling thread.
     *
     * @param method - a {@link String} representing the current method
     * @return a new {@link PaymentMethod} if it was successfully inserted, {@code null} if not
     */
    @Nullable
    public synchronized PaymentMethod insert(final String method) {
        ContentValues values = new ContentValues(1);
        values.put(PaymentMethodsTableColumns.COLUMN_METHOD, method);

        Cursor c = null;
        try {
            final SQLiteDatabase db = this.getWritableDatabase();
            if (db.insertOrThrow(getTableName(), null, values) == -1) {
                return null;
            } else {
                final PaymentMethodBuilderFactory builder = new PaymentMethodBuilderFactory();
                final PaymentMethod paymentMethod;
                c = db.rawQuery("SELECT last_insert_rowid()", null);
                if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
                    final int id = c.getInt(0);
                    paymentMethod = builder.setId(id).setMethod(method).build();
                } else {
                    paymentMethod = builder.setId(-1).setMethod(method).build();
                }
                if (mPaymentMethods != null) {
                    mPaymentMethods.add(paymentMethod);
                }
                return paymentMethod;
            }
        } finally { // Close the cursor and db to avoid memory leaks
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Updates a Payment method with a new method type. This method also automatically updates the underlying list that
     * is returned from {@link #get()}. This is done on the calling thread.
     *
     * @param oldPaymentMethod - the old method to update
     * @param newMethod        - the new string to use as the method
     * @return the new {@link PaymentMethod}
     */
    @Nullable
    public synchronized PaymentMethod update(final PaymentMethod oldPaymentMethod, final String newMethod) {
        if (oldPaymentMethod == null) {
            Log.e("TAG", "The oldPaymentMethod is null. No update can be performed");
            return null;
        }
        if (oldPaymentMethod.getMethod() == null && newMethod == null) {
            return oldPaymentMethod;
        } else if (newMethod != null && newMethod.equals(oldPaymentMethod.getMethod())) {
            return oldPaymentMethod;
        }

        ContentValues values = new ContentValues(1);
        values.put(PaymentMethodsTableColumns.COLUMN_METHOD, newMethod);
        
        try {
            if (getWritableDatabase().update(getTableName(), values, PaymentMethodsTableColumns.COLUMN_ID + " = ?", new String[]{Integer.toString(oldPaymentMethod.getId())}) > 0) {
                final PaymentMethodBuilderFactory builder = new PaymentMethodBuilderFactory();
                final PaymentMethod upddatePaymentMethod = builder.setId(oldPaymentMethod.getId()).setMethod(newMethod).build();
                if (mPaymentMethods != null) {
                    final int oldListIndex = mPaymentMethods.indexOf(oldPaymentMethod);
                    if (oldListIndex >= 0) {
                        mPaymentMethods.remove(oldPaymentMethod);
                        mPaymentMethods.add(oldListIndex, upddatePaymentMethod);
                    } else {
                        mPaymentMethods.add(upddatePaymentMethod);
                    }
                }
                return upddatePaymentMethod;
            } else {
                return null;
            }
        } catch (SQLException e) {
            return null;
        }
        
    }

    /**
     * Deletes a {@link PaymentMethod} from our database. This method also automatically updates the underlying list
     * that is returned from {@link #get()}. This is done on the calling thread.
     *
     * @param paymentMethod - the {@link PaymentMethod} to delete
     * @return {@code true} if is was successfully remove. {@code false} otherwise
     */
    public synchronized boolean delete(@NonNull PaymentMethod paymentMethod) {
        if (getWritableDatabase().delete(getTableName(), PaymentMethodsTableColumns.COLUMN_ID + " = ?", new String[]{Integer.toString(paymentMethod.getId())}) > 0) {
            if (mPaymentMethods != null) {
                mPaymentMethods.remove(paymentMethod);
            }
            return true;
        } else {
            return false;
        }
    }
}
