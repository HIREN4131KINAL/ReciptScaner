package co.smartreceipts.android.persistence.database.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.adapters.ReceiptDatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.ReceiptPrimaryKey;

/**
 * Stores all database operations related to the {@link Receipt} model objects
 */
public class ReceiptsTable extends TripForeignKeyAbstractSqlTable<Receipt, Integer> {

    // SQL Definitions:
    public static final String TABLE_NAME = "receipts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PATH = "path";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PARENT = "parent";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_TAX = "tax";
    public static final String COLUMN_EXCHANGE_RATE = "exchange_rate";
    public static final String COLUMN_DATE = "rcpt_date";
    public static final String COLUMN_TIMEZONE = "timezone";
    public static final String COLUMN_COMMENT = "comment";
    public static final String COLUMN_EXPENSEABLE = "expenseable";
    public static final String COLUMN_ISO4217 = "isocode";
    public static final String COLUMN_PAYMENT_METHOD_ID = "paymentMethodKey";
    public static final String COLUMN_NOTFULLPAGEIMAGE = "fullpageimage";
    public static final String COLUMN_PROCESSING_STATUS = "receipt_processing_status";
    public static final String COLUMN_EXTRA_EDITTEXT_1 = "extra_edittext_1";
    public static final String COLUMN_EXTRA_EDITTEXT_2 = "extra_edittext_2";
    public static final String COLUMN_EXTRA_EDITTEXT_3 = "extra_edittext_3";

    private static final String TAG = ReceiptsTable.class.getSimpleName();

    private final String mDefaultCurrencyCode;

    public ReceiptsTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull Table<Trip, String> tripsTable, @NonNull Table<PaymentMethod, Integer> paymentMethodTable, @NonNull Table<Category, String> categoryTable, @NonNull PersistenceManager persistenceManager) {
        super(sqLiteOpenHelper, TABLE_NAME, new ReceiptDatabaseAdapter(tripsTable, paymentMethodTable, categoryTable, persistenceManager), new ReceiptPrimaryKey(), COLUMN_PARENT, COLUMN_DATE);
        mDefaultCurrencyCode = persistenceManager.getPreferences().getDefaultCurreny();
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        final String receipts = "CREATE TABLE " + ReceiptsTable.TABLE_NAME + " ("
                + ReceiptsTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ReceiptsTable.COLUMN_PATH + " TEXT, "
                + ReceiptsTable.COLUMN_PARENT + " TEXT REFERENCES " + TripsTable.TABLE_NAME + " ON DELETE CASCADE, "
                + ReceiptsTable.COLUMN_NAME + " TEXT DEFAULT \"New Receipt\", "
                + ReceiptsTable.COLUMN_CATEGORY + " TEXT, "
                + ReceiptsTable.COLUMN_DATE + " DATE DEFAULT (DATE('now', 'localtime')), "
                + ReceiptsTable.COLUMN_TIMEZONE + " TEXT, "
                + ReceiptsTable.COLUMN_COMMENT + " TEXT, "
                + ReceiptsTable.COLUMN_ISO4217 + " TEXT NOT NULL, "
                + ReceiptsTable.COLUMN_PRICE + " DECIMAL(10, 2) DEFAULT 0.00, "
                + ReceiptsTable.COLUMN_TAX + " DECIMAL(10, 2) DEFAULT 0.00, "
                + ReceiptsTable.COLUMN_EXCHANGE_RATE + " DECIMAL(10, 10) DEFAULT -1.00, "
                + ReceiptsTable.COLUMN_PAYMENT_METHOD_ID + " INTEGER REFERENCES " + PaymentMethodsTable.TABLE_NAME + " ON DELETE NO ACTION, "
                + ReceiptsTable.COLUMN_EXPENSEABLE + " BOOLEAN DEFAULT 1, "
                + ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE + " BOOLEAN DEFAULT 1, "
                + ReceiptsTable.COLUMN_PROCESSING_STATUS + " TEXT, "
                + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1 + " TEXT, "
                + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2 + " TEXT, "
                + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3 + " TEXT"
                + ");";
        Log.d(TAG, receipts);
        db.execSQL(receipts);
    }


    @Override
    public synchronized void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);

        if (oldVersion <= 1) { // Add mCurrency column to receipts table
            final String alterReceipts = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_ISO4217 + " TEXT NOT NULL " + "DEFAULT " + mDefaultCurrencyCode;

            Log.d(TAG, alterReceipts);

            db.execSQL(alterReceipts);
        }

        if (oldVersion <= 3) { // Add extra_edittext columns
            final String alterReceipts1 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1 + " TEXT";
            final String alterReceipts2 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2 + " TEXT";
            final String alterReceipts3 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3 + " TEXT";

            Log.d(TAG, alterReceipts1);
            Log.d(TAG, alterReceipts2);
            Log.d(TAG, alterReceipts3);

            db.execSQL(alterReceipts1);
            db.execSQL(alterReceipts2);
            db.execSQL(alterReceipts3);
        }

        if (oldVersion <= 4) { // Change Mileage to Decimal instead of Integer
            final String alterReceipts = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_TAX + " DECIMAL(10, 2) DEFAULT 0.00";

            Log.d(TAG, alterReceipts);

            db.execSQL(alterReceipts);
        }


        if (oldVersion <= 6) { // Fix the database to replace absolute paths with relative ones

            Cursor receiptsCursor = null;
            try {
                receiptsCursor = db.query(ReceiptsTable.TABLE_NAME, new String[]{ReceiptsTable.COLUMN_ID, ReceiptsTable.COLUMN_PARENT, ReceiptsTable.COLUMN_PATH}, null, null, null, null, null);
                if (receiptsCursor != null && receiptsCursor.moveToFirst()) {
                    final int idIdx = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_ID);
                    final int parentIdx = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
                    final int imgIdx = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PATH);
                    do {
                        final int id = receiptsCursor.getInt(idIdx);
                        String absParentPath = receiptsCursor.getString(parentIdx);
                        if (absParentPath.endsWith(File.separator)) {
                            absParentPath = absParentPath.substring(0, absParentPath.length() - 1);
                        }
                        final String absImgPath = receiptsCursor.getString(imgIdx);
                        final ContentValues receiptValues = new ContentValues(2);
                        final String relParentPath = absParentPath.substring(absParentPath.lastIndexOf(File.separatorChar) + 1, absParentPath.length());
                        receiptValues.put(ReceiptsTable.COLUMN_PARENT, relParentPath);
                        Log.d(TAG, "Updating Abs. Parent Path for Receipt" + id + ": " + absParentPath + " => " + relParentPath);

                        if (!absImgPath.equalsIgnoreCase(DatabaseHelper.NO_DATA)) { // This can be either a path or NO_DATA
                            final String relImgPath = absImgPath.substring(absImgPath.lastIndexOf(File.separatorChar) + 1, absImgPath.length());
                            receiptValues.put(ReceiptsTable.COLUMN_PATH, relImgPath);
                            Log.d(TAG, "Updating Abs. Img Path for Receipt" + id + ": " + absImgPath + " => " + relImgPath);
                        }
                        if (db.update(ReceiptsTable.TABLE_NAME, receiptValues, ReceiptsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(id)}) == 0) {
                            Log.e(TAG, "Receipt Update Error Occured");
                        }
                    }
                    while (receiptsCursor.moveToNext());
                }
            } finally {
                if (receiptsCursor != null) {
                    receiptsCursor.close();
                }
            }
        }
        if (oldVersion <= 7) { // Added a timezone column to the receipts table
            final String alterReceipts = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_TIMEZONE + " TEXT";

            Log.d(TAG, alterReceipts);

            db.execSQL(alterReceipts);
        }

        if (oldVersion <= 11) { // Added trips filters, payment methods, and mileage table
            final String alterReceipts = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_PAYMENT_METHOD_ID + " INTEGER REFERENCES " + PaymentMethodsTable.TABLE_NAME + " ON DELETE NO ACTION";

            Log.d(TAG, alterReceipts);

            db.execSQL(alterReceipts);
        }
        if (oldVersion <= 12) { //Added better distance tracking, cost center to the trips, and status to trips/receipts

            final String alterReceiptsWithProcessingStatus = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_PROCESSING_STATUS + " TEXT";

            Log.d(TAG, alterReceiptsWithProcessingStatus);

            db.execSQL(alterReceiptsWithProcessingStatus);
        }
        if (oldVersion <= 13) {
            final String alterReceipts = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_EXCHANGE_RATE + " DECIMAL(10, 10) DEFAULT -1.00";
            Log.d(TAG, alterReceipts);
            db.execSQL(alterReceipts);
        }
    }

    @NonNull
    @Override
    protected Trip getTripFor(@NonNull Receipt receipt) {
        return receipt.getTrip();
    }
}
