package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.adapters.PaymentMethodDatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.PaymentMethodPrimaryKey;

/**
 * Stores all database operations related to the {@link PaymentMethod} model object
 */
public final class PaymentMethodsTable extends AbstractSqlTable<PaymentMethod, Integer> {

    // SQL Definitions:
    public static final String TABLE_NAME = "paymentmethods";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_METHOD = "method";


    private static final String TAG = CategoriesTable.class.getSimpleName();

    public PaymentMethodsTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        super(sqLiteOpenHelper, TABLE_NAME, new PaymentMethodDatabaseAdapter(), new PaymentMethodPrimaryKey());
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        final String sql = "CREATE TABLE " + getTableName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_METHOD + " TEXT"
                + AbstractSqlTable.COLUMN_SYNC_ID + " TEXT, "
                + AbstractSqlTable.COLUMN_MARKED_FOR_DELETION + " TEXT, "
                + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE"
                + ");";

        Log.d(TAG, sql);
        db.execSQL(sql);
        customizer.insertPaymentMethodDefaults(this);
    }

    @Override
    public synchronized void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);
        if (oldVersion <= 11) {
            final String sql = "CREATE TABLE " + getTableName() + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_METHOD + " TEXT"
                    + AbstractSqlTable.COLUMN_SYNC_ID + " TEXT, "
                    + AbstractSqlTable.COLUMN_MARKED_FOR_DELETION + " TEXT, "
                    + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE"
                    + ");";

            Log.d(TAG, sql);
            db.execSQL(sql);
            customizer.insertPaymentMethodDefaults(this);
        }

        if (oldVersion <= 14) {
            onUpgradeToAddSyncInformation(db, oldVersion, newVersion);
        }
    }

}
