package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.persistence.database.tables.adapters.PaymentMethodDatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.columns.PDFTableColumns;
import co.smartreceipts.android.persistence.database.tables.columns.PaymentMethodsTableColumns;
import co.smartreceipts.android.persistence.database.tables.keys.PaymentMethodPrimaryKey;

/**
 * Stores all database operations related to the {@link PaymentMethod} model object
 */
public final class PaymentMethodsTable extends AbstractSqlTable<PaymentMethod, Integer> {

    private static final String TAG = CategoriesTable.class.getSimpleName();

    public PaymentMethodsTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        super(sqLiteOpenHelper, PaymentMethodsTableColumns.TABLE_NAME, new PaymentMethodDatabaseAdapter(), new PaymentMethodPrimaryKey());
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        this.createPaymentMethodsTable(db, customizer);
    }

    @Override
    public synchronized void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);
        if (oldVersion <= 11) {
            this.createPaymentMethodsTable(db, customizer);
        }
    }

    private void createPaymentMethodsTable(final SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        final String sql = "CREATE TABLE " + getTableName() + " (" +
                           PDFTableColumns.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           PaymentMethodsTableColumns.COLUMN_METHOD + " TEXT" + ");";

        Log.d(TAG, sql);
        db.execSQL(sql);
        customizer.insertPaymentMethodDefaults(this);
    }

    /**
     * Attempts to fetch a payment method for a given primary key id
     *
     * @param id - the id of the desired {@link PaymentMethod}
     * @return a {@link PaymentMethod} if the id matches or {@code null} if none is found
     */
    @Nullable
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

}
