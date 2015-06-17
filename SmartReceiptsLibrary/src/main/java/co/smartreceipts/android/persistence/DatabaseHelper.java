package co.smartreceipts.android.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.factory.ColumnBuilderFactory;
import co.smartreceipts.android.model.factory.DistanceBuilderFactory;
import co.smartreceipts.android.model.factory.ExchangeRateBuilderFactory;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.factory.TripBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.utils.ListUtils;
import co.smartreceipts.android.utils.Utils;
import co.smartreceipts.android.workers.ImportTask;
import wb.android.autocomplete.AutoCompleteAdapter;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

public final class DatabaseHelper extends SQLiteOpenHelper implements AutoCompleteAdapter.QueryListener, AutoCompleteAdapter.ItemSelectedListener {

    // Logging Vars
    private static final boolean D = true;
    private static final String TAG = "DatabaseHelper";

    // Database Info
    public static final String DATABASE_NAME = "receipts.db";
    private static final int DATABASE_VERSION = 14;
    public static final String NO_DATA = "null"; // TODO: Just set to null
    static final String MULTI_CURRENCY = "XXXXXX";

    // Tags
    public static final String TAG_TRIPS_NAME = "Trips";
    public static final String TAG_TRIPS_COST_CENTER = "Trips_CostCenter";
    public static final String TAG_RECEIPTS_NAME = "Receipts";
    public static final String TAG_RECEIPTS_COMMENT = "Receipts_Comment";
    public static final String TAG_DISTANCE_LOCATION = "Distance_Location";

    // InstanceVar
    private static DatabaseHelper INSTANCE = null;

    // Caching Vars
    private Trip[] mTripsCache;
    private boolean mAreTripsValid;
    private final HashMap<Trip, List<Receipt>> mReceiptCache;
    private int mNextReceiptAutoIncrementId = -1;
    private HashMap<String, String> mCategories;
    private ArrayList<CharSequence> mCategoryList, mCurrencyList;
    private final ColumnDefinitions<Receipt> mReceiptColumnDefinitions;
    private List<Column<Receipt>> mCSVColumns;
    private List<Column<Receipt>> mPDFColumns;
    private List<PaymentMethod> mPaymentMethods;
    private Time mNow;

    // Other vars
    private final Context mContext;
    private final Flex mFlex;
    private final PersistenceManager mPersistenceManager;
    private final TableDefaultsCustomizer mCustomizations;

    // Listeners
    private TripRowListener mTripRowListener;
    private ReceiptRowListener mReceiptRowListener;
    private DistanceRowListener mDistanceRowListener;
    private ReceiptRowGraphListener mReceiptRowGraphListener;

    // Locks
    private final Object mDatabaseLock = new Object();
    private final Object mReceiptCacheLock = new Object();
    private final Object mTripCacheLock = new Object();

    // Misc Vars
    private boolean mIsDBOpen = false;

    // Hack to prevent Recursive Database Calling
    private SQLiteDatabase _initDB; // This is only set while either onCreate or onUpdate is running. It is null all
    // other times

    public interface TripRowListener {
        public void onTripRowsQuerySuccess(Trip[] trips);

        public void onTripRowInsertSuccess(Trip trip);

        public void onTripRowInsertFailure(SQLException ex, File directory); // Directory here is out of mDate

        public void onTripRowUpdateSuccess(Trip trip);

        public void onTripRowUpdateFailure(Trip newTrip, Trip oldTrip, File directory); // For rollback info

        public void onTripDeleteSuccess(Trip oldTrip);

        public void onTripDeleteFailure();

        public void onSQLCorruptionException();
    }

    public interface ReceiptRowListener {
        public void onReceiptRowsQuerySuccess(List<Receipt> receipts);

        public void onReceiptRowInsertSuccess(Receipt receipt);

        public void onReceiptRowInsertFailure(SQLException ex); // Directory here is out of mDate

        public void onReceiptRowUpdateSuccess(Receipt receipt);

        public void onReceiptRowUpdateFailure(); // For rollback info

        public void onReceiptDeleteSuccess(Receipt receipt);

        public void onReceiptRowAutoCompleteQueryResult(String name, String price, String category); // Any of these can
        // be null!

        public void onReceiptCopySuccess(Trip trip);

        public void onReceiptCopyFailure();

        public void onReceiptMoveSuccess(Trip trip);

        public void onReceiptMoveFailure();

        public void onReceiptDeleteFailure();
    }

    public interface DistanceRowListener {
        public void onDistanceRowsQuerySuccess(List<Distance> distance);

        public void onDistanceRowInsertSuccess(Distance distance);

        public void onDistanceRowInsertFailure(SQLException error);

        public void onDistanceRowUpdateSuccess(Distance distance);

        public void onDistanceRowUpdateFailure(); //For rollback info

        public void onDistanceDeleteSuccess(Distance distance);

        public void onDistanceDeleteFailure();
    }

    public interface ReceiptRowGraphListener {
        public void onGraphQuerySuccess(List<Receipt> receipts);
    }

    public interface TableDefaultsCustomizer {
        public void onFirstRun();

        public void insertCategoryDefaults(DatabaseHelper db);

        public void insertCSVDefaults(DatabaseHelper db);

        public void insertPDFDefaults(DatabaseHelper db);

        public void insertPaymentMethodDefaults(DatabaseHelper db);
    }

    // Tables Declarations
    // Remember to update the merge() command below when adding columns
    private static final class TripsTable {
        private TripsTable() {
        }

        public static final String TABLE_NAME = "trips";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_FROM = "from_date";
        public static final String COLUMN_TO = "to_date";
        public static final String COLUMN_FROM_TIMEZONE = "from_timezone";
        public static final String COLUMN_TO_TIMEZONE = "to_timezone";
        @SuppressWarnings("unused")
        @Deprecated
        public static final String COLUMN_PRICE = "price"; // Deprecated, since this is receipt info
        public static final String COLUMN_MILEAGE = "miles_new";
        public static final String COLUMN_COMMENT = "trips_comment";
        public static final String COLUMN_COST_CENTER = "trips_cost_center";
        public static final String COLUMN_DEFAULT_CURRENCY = "trips_default_currency";
        public static final String COLUMN_FILTERS = "trips_filters";
        public static final String COLUMN_PROCESSING_STATUS = "trip_processing_status";
    }

    private static final class ReceiptsTable {

        private ReceiptsTable() {
        }

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
    }

    private static final class CategoriesTable {
        private CategoriesTable() {
        }

        public static final String TABLE_NAME = "categories";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_BREAKDOWN = "breakdown";
    }

    private static final class DistanceTable {
        private DistanceTable() {
        }

        public static final String TABLE_NAME = "distance";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_PARENT = "parent";
        public static final String COLUMN_DISTANCE = "distance";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TIMEZONE = "timezone";
        public static final String COLUMN_COMMENT = "comment";
        public static final String COLUMN_RATE = "rate";
        public static final String COLUMN_RATE_CURRENCY = "rate_currency";
    }

    public static final class CSVTable {
        private CSVTable() {
        }

        public static final String TABLE_NAME = "csvcolumns";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TYPE = "type";
    }

    public static final class PDFTable {
        private PDFTable() {
        }

        public static final String TABLE_NAME = "pdfcolumns";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TYPE = "type";
    }

    public static final class PaymentMethodsTable {
        private PaymentMethodsTable() {
        }

        public static final String TABLE_NAME = "paymentmethods";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_METHOD = "method";
    }

    private DatabaseHelper(SmartReceiptsApplication application, PersistenceManager persistenceManager, String databasePath) {
        super(application.getApplicationContext(), databasePath, null, DATABASE_VERSION); // Requests the default cursor
        // factory
        mAreTripsValid = false;
        mReceiptCache = new HashMap<Trip, List<Receipt>>();
        mContext = application.getApplicationContext();
        mFlex = application.getFlex();
        mPersistenceManager = persistenceManager;
        mCustomizations = application;
        mReceiptColumnDefinitions = new ReceiptColumnDefinitions(mContext, this, mPersistenceManager.getPreferences(), mFlex);
        this.getReadableDatabase(); // Called here, so onCreate gets called on the UI thread
    }

    public static final DatabaseHelper getInstance(SmartReceiptsApplication application, PersistenceManager persistenceManager) {
        if (INSTANCE == null || !INSTANCE.isOpen()) { // If we don't have an instance or it's closed
            String databasePath = StorageManager.GetRootPath();
            if (BuildConfig.DEBUG) {
                if (databasePath.equals("")) {
                    throw new RuntimeException("The SDCard must be created beforoe GetRootPath is called in DBHelper");
                }
            }
            if (!databasePath.endsWith(File.separator)) {
                databasePath = databasePath + File.separator;
            }
            databasePath = databasePath + DATABASE_NAME;
            INSTANCE = new DatabaseHelper(application, persistenceManager, databasePath);
        }
        return INSTANCE;
    }

    public static final DatabaseHelper getNewInstance(SmartReceiptsApplication application, PersistenceManager persistenceManager) {
        String databasePath = StorageManager.GetRootPath();
        if (BuildConfig.DEBUG) {
            if (databasePath.equals("")) {
                throw new RuntimeException("The SDCard must be created beforoe GetRootPath is called in DBHelper");
            }
        }
        if (!databasePath.endsWith(File.separator)) {
            databasePath = databasePath + File.separator;
        }
        databasePath = databasePath + DATABASE_NAME;
        return new DatabaseHelper(application, persistenceManager, databasePath);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // Begin Abstract Method Overrides
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(final SQLiteDatabase db) {
        synchronized (mDatabaseLock) {
            _initDB = db;
            //N.B. This only gets called if you actually request the database using the getDatabase method
            final String trips = "CREATE TABLE " + TripsTable.TABLE_NAME + " ("
                    + TripsTable.COLUMN_NAME + " TEXT PRIMARY KEY, "
                    + TripsTable.COLUMN_FROM + " DATE, "
                    + TripsTable.COLUMN_TO + " DATE, "
                    + TripsTable.COLUMN_FROM_TIMEZONE + " TEXT, "
                    + TripsTable.COLUMN_TO_TIMEZONE + " TEXT, "
                    /*+ TripsTable.COLUMN_PRICE + " DECIMAL(10, 2) DEFAULT 0.00, "*/
                    + TripsTable.COLUMN_MILEAGE + " DECIMAL(10, 2) DEFAULT 0.00, "
                    + TripsTable.COLUMN_COMMENT + " TEXT, "
                    + TripsTable.COLUMN_COST_CENTER + " TEXT, "
                    + TripsTable.COLUMN_DEFAULT_CURRENCY + " TEXT, "
                    + TripsTable.COLUMN_PROCESSING_STATUS + " TEXT, "
                    + TripsTable.COLUMN_FILTERS + " TEXT"
                    + ");";
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
            final String categories = "CREATE TABLE " + CategoriesTable.TABLE_NAME + " ("
                    + CategoriesTable.COLUMN_NAME + " TEXT PRIMARY KEY, "
                    + CategoriesTable.COLUMN_CODE + " TEXT, "
                    + CategoriesTable.COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1"
                    + ");";

            if (BuildConfig.DEBUG) {
                Log.d(TAG, trips);
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, receipts);
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, categories);
            }
            db.execSQL(trips);
            db.execSQL(receipts);
            db.execSQL(categories);
            this.createCSVTable(db);
            this.createPDFTable(db);
            this.createPaymentMethodsTable(db);
            this.createDistanceTable(db);
            mCustomizations.insertCategoryDefaults(this);
            mCustomizations.onFirstRun();
            _initDB = null;
        }
    }

    @Override
    public final void onUpgrade(final SQLiteDatabase db, int oldVersion, final int newVersion) {
        synchronized (mDatabaseLock) {

            if (D) {
                Log.d(TAG, "Upgrading the database from version " + oldVersion + " to " + newVersion);
            }

            // Try to backup the database to the SD Card for support reasons
            final StorageManager storageManager = mPersistenceManager.getStorageManager();
            File sdDB = storageManager.getFile(DATABASE_NAME + "." + oldVersion + ".bak");
            try {
                storageManager.copy(new File(db.getPath()), sdDB, true);
                if (D) {
                    Log.d(TAG, "Backed up database file to: " + sdDB.getName());
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to back up database: " + e.toString());
            }

            _initDB = db;
            if (oldVersion <= 1) { // Add mCurrency column to receipts table
                final String alterReceipts = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_ISO4217 + " TEXT NOT NULL " + "DEFAULT " + mPersistenceManager.getPreferences().getDefaultCurreny();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, alterReceipts);
                }
                db.execSQL(alterReceipts);
            }
            if (oldVersion <= 2) { // Add the mileage field to trips, add the breakdown boolean to categories, and
                // create the CSV table
                final String alterCategories = "ALTER TABLE " + CategoriesTable.TABLE_NAME + " ADD " + CategoriesTable.COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1";
                if (D) {
                    Log.d(TAG, alterCategories);
                }
                db.execSQL(alterCategories);
                this.createCSVTable(db);
            }
            if (oldVersion <= 3) { // Add extra_edittext columns
                final String alterReceipts1 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1 + " TEXT";
                final String alterReceipts2 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2 + " TEXT";
                final String alterReceipts3 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3 + " TEXT";
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, alterReceipts1);
                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, alterReceipts2);
                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, alterReceipts3);
                }
                db.execSQL(alterReceipts1);
                db.execSQL(alterReceipts2);
                db.execSQL(alterReceipts3);
            }
            if (oldVersion <= 4) { // Change Mileage to Decimal instead of Integer
                final String alterMiles = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_MILEAGE + " DECIMAL(10, 2) DEFAULT 0.00";
                final String alterReceipts1 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_TAX + " DECIMAL(10, 2) DEFAULT 0.00";
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, alterMiles);
                    Log.d(TAG, alterReceipts1);
                }
            }
            if (oldVersion <= 5) {
                // Skipped b/c I forgot to include the update stuff
            }
            if (oldVersion <= 6) { // Fix the database to replace absolute paths with relative ones
                final Cursor tripsCursor = db.query(TripsTable.TABLE_NAME, new String[]{TripsTable.COLUMN_NAME}, null, null, null, null, null);
                if (tripsCursor != null && tripsCursor.moveToFirst()) {
                    final int nameIndex = tripsCursor.getColumnIndex(TripsTable.COLUMN_NAME);
                    do {
                        String absPath = tripsCursor.getString(nameIndex);
                        if (absPath.endsWith(File.separator)) {
                            absPath = absPath.substring(0, absPath.length() - 1);
                        }
                        final String relPath = absPath.substring(absPath.lastIndexOf(File.separatorChar) + 1, absPath.length());
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Updating Abs. Trip Path: " + absPath + " => " + relPath);
                        }
                        final ContentValues tripValues = new ContentValues(1);
                        tripValues.put(TripsTable.COLUMN_NAME, relPath);
                        if (db.update(TripsTable.TABLE_NAME, tripValues, TripsTable.COLUMN_NAME + " = ?", new String[]{absPath}) == 0) {
                            if (BuildConfig.DEBUG) {
                                Log.e(TAG, "Trip Update Error Occured");
                            }
                        }
                    }
                    while (tripsCursor.moveToNext());
                }
                // TODO: Finally close here
                tripsCursor.close();

                final Cursor receiptsCursor = db.query(ReceiptsTable.TABLE_NAME, new String[]{ReceiptsTable.COLUMN_ID, ReceiptsTable.COLUMN_PARENT, ReceiptsTable.COLUMN_PATH}, null, null, null, null, null);
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
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Updating Abs. Parent Path for Receipt" + id + ": " + absParentPath + " => " + relParentPath);
                        }
                        ;
                        if (!absImgPath.equalsIgnoreCase(NO_DATA)) { // This can be either a path or NO_DATA
                            final String relImgPath = absImgPath.substring(absImgPath.lastIndexOf(File.separatorChar) + 1, absImgPath.length());
                            receiptValues.put(ReceiptsTable.COLUMN_PATH, relImgPath);
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "Updating Abs. Img Path for Receipt" + id + ": " + absImgPath + " => " + relImgPath);
                            }
                        }
                        if (db.update(ReceiptsTable.TABLE_NAME, receiptValues, ReceiptsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(id)}) == 0) {
                            if (BuildConfig.DEBUG) {
                                Log.e(TAG, "Receipt Update Error Occured");
                            }
                        }
                    }
                    while (receiptsCursor.moveToNext());
                }
                receiptsCursor.close();
            }
            if (oldVersion <= 7) { // Added a timezone column to the receipts table
                final String alterReceipts = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_TIMEZONE + " TEXT";
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, alterReceipts);
                }
                db.execSQL(alterReceipts);
            }
            if (oldVersion <= 8) { // Added a timezone column to the trips table
                final String alterTrips1 = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_FROM_TIMEZONE + " TEXT";
                final String alterTrips2 = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_TO_TIMEZONE + " TEXT";
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, alterTrips1);
                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, alterTrips2);
                }
                db.execSQL(alterTrips1);
                db.execSQL(alterTrips2);
            }
            if (oldVersion <= 9) { // Added a PDF table
                this.createPDFTable(db);
            }
            if (oldVersion <= 10) {
                final String alterTrips1 = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_COMMENT + " TEXT";
                final String alterTrips2 = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_DEFAULT_CURRENCY + " TEXT";
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, alterTrips1);
                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, alterTrips2);
                }
                db.execSQL(alterTrips1);
                db.execSQL(alterTrips2);
            }
            if (oldVersion <= 11) { // Added trips filters, payment methods, and mileage table
                this.createPaymentMethodsTable(db);
                final String alterTrips = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_FILTERS + " TEXT";
                final String alterReceipts = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_PAYMENT_METHOD_ID + " INTEGER REFERENCES " + PaymentMethodsTable.TABLE_NAME + " ON DELETE NO ACTION";
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, alterTrips);
                    Log.d(TAG, alterReceipts);
                }
                db.execSQL(alterTrips);
                db.execSQL(alterReceipts);
            }
            if (oldVersion <= 12) { //Added better distance tracking, cost center to the trips, and status to trips/receipts
                this.createDistanceTable(db);

                // Once we create the table, we need to move our "trips" mileage into a single item in the distance table
                final String distanceMigrateBase = "INSERT INTO " + DistanceTable.TABLE_NAME + "(" + DistanceTable.COLUMN_PARENT + ", " + DistanceTable.COLUMN_DISTANCE + ", " + DistanceTable.COLUMN_LOCATION + ", " + DistanceTable.COLUMN_DATE + ", " + DistanceTable.COLUMN_TIMEZONE + ", " + DistanceTable.COLUMN_COMMENT + ", " + DistanceTable.COLUMN_RATE_CURRENCY + ")"
                        + " SELECT " + TripsTable.COLUMN_NAME + ", " + TripsTable.COLUMN_MILEAGE + " , \"\" as " + DistanceTable.COLUMN_LOCATION + ", " + TripsTable.COLUMN_FROM + ", " + TripsTable.COLUMN_FROM_TIMEZONE + " , \"\" as " + DistanceTable.COLUMN_COMMENT + ", ";
                final String distanceMigrateNotNullCurrency = distanceMigrateBase + TripsTable.COLUMN_DEFAULT_CURRENCY + " FROM " + TripsTable.TABLE_NAME + " WHERE " + TripsTable.COLUMN_DEFAULT_CURRENCY + " IS NOT NULL AND " + TripsTable.COLUMN_MILEAGE + " > 0;";
                final String distanceMigrateNullCurrency = distanceMigrateBase + "\"" + mPersistenceManager.getPreferences().getDefaultCurreny() + "\" as " + DistanceTable.COLUMN_RATE_CURRENCY + " FROM " + TripsTable.TABLE_NAME + " WHERE " + TripsTable.COLUMN_DEFAULT_CURRENCY + " IS NULL AND " + TripsTable.COLUMN_MILEAGE + " > 0;";
                final String alterTripsWithCostCenter = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_COST_CENTER + " TEXT";
                final String alterTripsWithProcessingStatus = "ALTER TABLE " + TripsTable.TABLE_NAME + " ADD " + TripsTable.COLUMN_PROCESSING_STATUS + " TEXT";
                final String alterReceiptsWithProcessingStatus = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_PROCESSING_STATUS + " TEXT";
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, distanceMigrateNotNullCurrency);
                    Log.d(TAG, distanceMigrateNullCurrency);
                    Log.d(TAG, alterTripsWithCostCenter);
                    Log.d(TAG, alterTripsWithProcessingStatus);
                    Log.d(TAG, alterReceiptsWithProcessingStatus);
                }
                db.execSQL(distanceMigrateNotNullCurrency);
                db.execSQL(distanceMigrateNullCurrency);
                db.execSQL(alterTripsWithCostCenter);
                db.execSQL(alterTripsWithProcessingStatus);
                db.execSQL(alterReceiptsWithProcessingStatus);
            }
            if (oldVersion <= 13) {
                final String alterReceipts = "ALTER TABLE " + ReceiptsTable.TABLE_NAME + " ADD " + ReceiptsTable.COLUMN_EXCHANGE_RATE + " DECIMAL(10, 10) DEFAULT -1.00";
                Log.d(TAG, alterReceipts);
                db.execSQL(alterReceipts);
            }
            _initDB = null;
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        mIsDBOpen = true;
    }

    @Override
    public synchronized void close() {
        super.close();
        mIsDBOpen = false;
    }

    public boolean isOpen() {
        return mIsDBOpen;
    }

    public void onDestroy() {
        try {
            this.close();
        } catch (Exception e) {
            // This can be called from finalize, so operate cautiously
            Log.e(TAG, e.toString());
        }
    }

    @Override
    protected void finalize() throws Throwable {
        onDestroy(); // Close our resources if we still need
        super.finalize();
    }

	/*
	 * public final void testPrintDBValues() { final SQLiteDatabase db = this.getReadableDatabase(); final Cursor
	 * tripsCursor = db.query(TripsTable.TABLE_NAME, new String[] {TripsTable.COLUMN_NAME}, null, null, null, null,
	 * null); String data = ""; if (BuildConfig.DEBUG) Log.d(TAG,
	 * "=================== Printing Trips ==================="); if (BuildConfig.DEBUG) data +=
	 * "=================== Printing Trips ===================" + "\n"; if (tripsCursor != null &&
	 * tripsCursor.moveToFirst()) { final int nameIndex = tripsCursor.getColumnIndex(TripsTable.COLUMN_NAME); do { if
	 * (BuildConfig.DEBUG) Log.d(TAG, tripsCursor.getString(nameIndex)); if (BuildConfig.DEBUG) data += "\"" +
	 * tripsCursor.getString(nameIndex) + "\"";
	 * 
	 * } while (tripsCursor.moveToNext()); }
	 * 
	 * final Cursor receiptsCursor = db.query(ReceiptsTable.TABLE_NAME, new String[] {ReceiptsTable.COLUMN_ID,
	 * ReceiptsTable.COLUMN_PARENT, ReceiptsTable.COLUMN_PATH}, null, null, null, null, null); if (BuildConfig.DEBUG)
	 * Log.d(TAG, "=================== Printing Receipts ==================="); if (BuildConfig.DEBUG) data +=
	 * "=================== Printing Receipts ===================" + "\n"; if (receiptsCursor != null &&
	 * receiptsCursor.moveToFirst()) { final int idIdx = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_ID); final
	 * int parentIdx = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT); final int imgIdx =
	 * receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PATH); do { if (BuildConfig.DEBUG) Log.d(TAG, "(" +
	 * receiptsCursor.getInt(idIdx) + ", " + receiptsCursor.getString(parentIdx) + ", " +
	 * receiptsCursor.getString(imgIdx) + ")"); if (BuildConfig.DEBUG) data += "(" + receiptsCursor.getInt(idIdx) + ", "
	 * + receiptsCursor.getString(parentIdx) + ", " + receiptsCursor.getString(imgIdx) + ")" + "\n"; } while
	 * (receiptsCursor.moveToNext()); } mContext.getStorageManager().write("db.txt", data); }
	 */

    private final void createCSVTable(final SQLiteDatabase db) { // Called in onCreate and onUpgrade
        final String csv = "CREATE TABLE " + CSVTable.TABLE_NAME + " (" + CSVTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CSVTable.COLUMN_TYPE + " TEXT" + ");";
        if (BuildConfig.DEBUG) {
            Log.d(TAG, csv);
        }
        db.execSQL(csv);
        mCustomizations.insertCSVDefaults(this);
    }

    private final void createPDFTable(final SQLiteDatabase db) { // Called in onCreate and onUpgrade
        final String pdf = "CREATE TABLE " + PDFTable.TABLE_NAME + " (" + PDFTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PDFTable.COLUMN_TYPE + " TEXT" + ");";
        if (BuildConfig.DEBUG) {
            Log.d(TAG, pdf);
        }
        db.execSQL(pdf);
        mCustomizations.insertPDFDefaults(this);
    }

    private final void createDistanceTable(final SQLiteDatabase db) {
        final String distance = "CREATE TABLE " + DistanceTable.TABLE_NAME + " ("
                + DistanceTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DistanceTable.COLUMN_PARENT + " TEXT REFERENCES " + TripsTable.COLUMN_NAME + " ON DELETE CASCADE,"
                + DistanceTable.COLUMN_DISTANCE + " DECIMAL(10, 2) DEFAULT 0.00,"
                + DistanceTable.COLUMN_LOCATION + " TEXT,"
                + DistanceTable.COLUMN_DATE + " DATE,"
                + DistanceTable.COLUMN_TIMEZONE + " TEXT,"
                + DistanceTable.COLUMN_COMMENT + " TEXT,"
                + DistanceTable.COLUMN_RATE_CURRENCY + " TEXT NOT NULL, "
                + DistanceTable.COLUMN_RATE + " DECIMAL(10, 2) DEFAULT 0.00 );";

        if (BuildConfig.DEBUG) {
            Log.d(TAG, distance);
        }
        db.execSQL(distance);
    }

    private final void createPaymentMethodsTable(final SQLiteDatabase db) { // Called in onCreate and onUpgrade
        final String sql = "CREATE TABLE " + PaymentMethodsTable.TABLE_NAME + " (" + PDFTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PaymentMethodsTable.COLUMN_METHOD + " TEXT" + ");";
        if (BuildConfig.DEBUG) {
            Log.d(TAG, sql);
        }
        db.execSQL(sql);
        mCustomizations.insertPaymentMethodDefaults(this);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // Trip Methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public void registerTripRowListener(TripRowListener listener) {
        mTripRowListener = listener;
    }

    public void unregisterTripRowListener(TripRowListener listener) {
        mTripRowListener = null;
    }

    public Trip[] getTripsSerial() throws SQLiteDatabaseCorruptException {
        synchronized (mTripCacheLock) {
            if (mAreTripsValid) {
                return mTripsCache;
            }
        }
        Trip[] trips = getTripsHelper();
        synchronized (mTripCacheLock) {
            if (!mAreTripsValid) {
                mAreTripsValid = true;
                mTripsCache = trips;
            }
            return mTripsCache;
        }
    }

    public void getTripsParallel() {
        if (mTripRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No TripRowListener was registered.");
            }
        } else {
            synchronized (mTripCacheLock) {
                if (mAreTripsValid) {
                    mTripRowListener.onTripRowsQuerySuccess(mTripsCache);
                    return;
                }
            }
            (new GetTripsWorker()).execute();
        }
    }

    private static final String CURR_CNT_QUERY = "SELECT COUNT(*), " + ReceiptsTable.COLUMN_ISO4217 + " FROM (SELECT COUNT(*), " + ReceiptsTable.COLUMN_ISO4217 + " FROM " + ReceiptsTable.TABLE_NAME + " WHERE " + ReceiptsTable.COLUMN_PARENT + "=? GROUP BY " + ReceiptsTable.COLUMN_ISO4217 + ");";

    private Trip[] getTripsHelper() throws SQLiteDatabaseCorruptException {
        SQLiteDatabase db;
        Cursor c = null;
        synchronized (mDatabaseLock) {
            Trip[] trips;
            try {
                db = this.getReadableDatabase();
                c = db.query(TripsTable.TABLE_NAME, null, null, null, null, null, TripsTable.COLUMN_TO + " DESC");
                if (c != null && c.moveToFirst()) {
                    trips = new Trip[c.getCount()];
                    final int nameIndex = c.getColumnIndex(TripsTable.COLUMN_NAME);
                    final int fromIndex = c.getColumnIndex(TripsTable.COLUMN_FROM);
                    final int toIndex = c.getColumnIndex(TripsTable.COLUMN_TO);
                    final int fromTimeZoneIndex = c.getColumnIndex(TripsTable.COLUMN_FROM_TIMEZONE);
                    final int toTimeZoneIndex = c.getColumnIndex(TripsTable.COLUMN_TO_TIMEZONE);
                    final int milesIndex = c.getColumnIndex(TripsTable.COLUMN_MILEAGE);
                    final int commentIndex = c.getColumnIndex(TripsTable.COLUMN_COMMENT);
                    final int costCenterIndex = c.getColumnIndex(TripsTable.COLUMN_COST_CENTER);
                    final int defaultCurrencyIndex = c.getColumnIndex(TripsTable.COLUMN_DEFAULT_CURRENCY);
                    final int filterIndex = c.getColumnIndex(TripsTable.COLUMN_FILTERS);
                    do {
                        final String name = c.getString(nameIndex);
                        final long from = c.getLong(fromIndex);
                        final long to = c.getLong(toIndex);
                        final String fromTimeZone = c.getString(fromTimeZoneIndex);
                        final String toTimeZone = c.getString(toTimeZoneIndex);
                        final float miles = c.getFloat(milesIndex);
                        final String comment = c.getString(commentIndex);
                        final String costCenter = c.getString(costCenterIndex);
                        final String defaultCurrency = c.getString(defaultCurrencyIndex);
                        final String filterJson = c.getString(filterIndex);
                        final TripBuilderFactory builder = new TripBuilderFactory();
                        trips[c.getPosition()] = builder.setDirectory(mPersistenceManager.getStorageManager().getFile(name)).setStartDate(from).setEndDate(to).setStartTimeZone(fromTimeZone).setEndTimeZone(toTimeZone).setComment(comment).setCostCenter(costCenter).setFilter(filterJson).setDefaultCurrency(defaultCurrency, mPersistenceManager.getPreferences().getDefaultCurreny()).setSourceAsCache().build();
                        getTripPriceAndDailyPrice(trips[c.getPosition()]);
                    }
                    while (c.moveToNext());
                    return trips;
                } else {
                    trips = new Trip[0];
                }
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
            return trips;
        }
    }

    private class GetTripsWorker extends AsyncTask<Void, Void, Trip[]> {

        private boolean mIsDatabaseCorrupt = false;

        @Override
        protected Trip[] doInBackground(Void... params) {
            try {
                return getTripsHelper();
            } catch (SQLiteDatabaseCorruptException e) {
                mIsDatabaseCorrupt = true;
                return new Trip[0];
            }
        }

        @Override
        protected void onPostExecute(Trip[] result) {
            if (mIsDatabaseCorrupt) {
                if (mTripRowListener != null) {
                    mTripRowListener.onSQLCorruptionException();
                }
            } else {
                synchronized (mTripCacheLock) {
                    mAreTripsValid = true;
                    mTripsCache = result;
                }
                if (mTripRowListener != null) {
                    mTripRowListener.onTripRowsQuerySuccess(result);
                }
            }
        }

    }

    public List<CharSequence> getTripNames() {
        Trip[] trips = getTripsSerial();
        final ArrayList<CharSequence> tripNames = new ArrayList<CharSequence>(trips.length);
        for (int i = 0; i < trips.length; i++) {
            tripNames.add(trips[i].getName());
        }
        return tripNames;
    }

    public List<CharSequence> getTripNames(Trip tripToExclude) {
        Trip[] trips = getTripsSerial();
        final ArrayList<CharSequence> tripNames = new ArrayList<CharSequence>(trips.length - 1);
        for (int i = 0; i < trips.length; i++) {
            Trip trip = trips[i];
            if (!trip.equals(tripToExclude)) {
                tripNames.add(trip.getName());
            }
        }
        return tripNames;
    }

    public final Trip getTripByName(final String name) {
        if (name == null || name.length() == 0) {
            return null;
        }
        synchronized (mTripCacheLock) {
            if (mAreTripsValid) {
                for (int i = 0; i < mTripsCache.length; i++) {
                    if (mTripsCache[i].getName().equals(name)) {
                        return mTripsCache[i];
                    }
                }
            }
        }
        SQLiteDatabase db = null;
        Cursor c = null;
        synchronized (mDatabaseLock) {
            try {
                db = this.getReadableDatabase();
                c = db.query(TripsTable.TABLE_NAME, null, TripsTable.COLUMN_NAME + " = ?", new String[]{name}, null, null, null);
                if (c != null && c.moveToFirst()) {
                    final int fromIndex = c.getColumnIndex(TripsTable.COLUMN_FROM);
                    final int toIndex = c.getColumnIndex(TripsTable.COLUMN_TO);
                    final int fromTimeZoneIndex = c.getColumnIndex(TripsTable.COLUMN_FROM_TIMEZONE);
                    final int toTimeZoneIndex = c.getColumnIndex(TripsTable.COLUMN_TO_TIMEZONE);
                    // final int priceIndex = c.getColumnIndex(TripsTable.COLUMN_PRICE);
                    final int milesIndex = c.getColumnIndex(TripsTable.COLUMN_MILEAGE);
                    final int commentIndex = c.getColumnIndex(TripsTable.COLUMN_COMMENT);
                    final int costCenterIndex = c.getColumnIndex(TripsTable.COLUMN_COST_CENTER);
                    final int defaultCurrencyIndex = c.getColumnIndex(TripsTable.COLUMN_DEFAULT_CURRENCY);
                    final int filterIndex = c.getColumnIndex(TripsTable.COLUMN_FILTERS);
                    final long from = c.getLong(fromIndex);
                    final long to = c.getLong(toIndex);
                    final String fromTimeZone = c.getString(fromTimeZoneIndex);
                    final String toTimeZone = c.getString(toTimeZoneIndex);
                    final float miles = c.getFloat(milesIndex);
                    // final String price = c.getString(priceIndex);
                    final String comment = c.getString(commentIndex);
                    final String costCenter = c.getString(costCenterIndex);
                    final String defaultCurrency = c.getString(defaultCurrencyIndex);
                    final String filterJson = c.getString(filterIndex);
                    final TripBuilderFactory builder = new TripBuilderFactory();
                    final Trip trip = builder.setDirectory(mPersistenceManager.getStorageManager().getFile(name)).setStartDate(from).setEndDate(to).setStartTimeZone(fromTimeZone).setEndTimeZone(toTimeZone).setComment(comment).setCostCenter(costCenter).setFilter(filterJson).setDefaultCurrency(defaultCurrency, mPersistenceManager.getPreferences().getDefaultCurreny()).setSourceAsCache().build();
                    getTripPriceAndDailyPrice(trip);
                    return trip;
                } else {
                    return null;
                }
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    // Returns the trip on success. Null otherwise
    public final Trip insertTripSerial(File dir, Date from, Date to, String comment, String costCenter, String defaultCurrencyCode) throws SQLException {
        Trip trip = insertTripHelper(dir, from, to, comment, costCenter, defaultCurrencyCode);
        if (trip != null) {
            synchronized (mTripCacheLock) {
                mAreTripsValid = false;
            }
        }
        return trip;
    }

    public void insertTripParallel(File dir, Date from, Date to, String comment, String costCenter, String defaultCurrencyCode) {
        if (mTripRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No TripRowListener was registered.");
            }
        }
        (new InsertTripRowWorker(dir, from, to, comment, costCenter, defaultCurrencyCode)).execute();
    }

    private Trip insertTripHelper(File dir, Date from, Date to, String comment, String costCenter, String defaultCurrencyCode) throws SQLException {
        ContentValues values = new ContentValues(10);
        values.put(TripsTable.COLUMN_NAME, dir.getName());
        values.put(TripsTable.COLUMN_FROM, from.getTime());
        values.put(TripsTable.COLUMN_TO, to.getTime());
        values.put(TripsTable.COLUMN_FROM_TIMEZONE, TimeZone.getDefault().getID());
        values.put(TripsTable.COLUMN_TO_TIMEZONE, TimeZone.getDefault().getID());
        values.put(TripsTable.COLUMN_COMMENT, comment);
        values.put(TripsTable.COLUMN_COST_CENTER, costCenter);
        values.put(TripsTable.COLUMN_DEFAULT_CURRENCY, defaultCurrencyCode);
        Trip toReturn;
        synchronized (mDatabaseLock) {
            SQLiteDatabase db;
            db = this.getWritableDatabase();
            if (db.insertOrThrow(TripsTable.TABLE_NAME, null, values) == -1) {
                return null;
            } else {
                toReturn = (new TripBuilderFactory()).setDirectory(dir).setStartDate(from).setEndDate(to).setStartTimeZone(TimeZone.getDefault()).setEndTimeZone(TimeZone.getDefault()).setComment(comment).setCostCenter(costCenter).setDefaultCurrency(defaultCurrencyCode).setSourceAsCache().build();
            }
        }
        if (this.getReadableDatabase() != null) {
            String databasePath = this.getReadableDatabase().getPath();
            if (!TextUtils.isEmpty(databasePath)) {
                backUpDatabase(databasePath);
            }
        }
        return toReturn;
    }

    private class InsertTripRowWorker extends AsyncTask<Void, Void, Trip> {

        private final File mDir;
        private final Date mFrom, mTo;
        private final String mComment, mCostCenter, mDefaultCurrencyCode;
        private SQLException mException;

        public InsertTripRowWorker(final File dir, final Date from, final Date to, final String comment, final String costCenter, final String defaultCurrencyCode) {
            mDir = dir;
            mFrom = from;
            mTo = to;
            mComment = comment;
            mDefaultCurrencyCode = defaultCurrencyCode;
            mCostCenter = costCenter;
            mException = null;
        }

        @Override
        protected Trip doInBackground(Void... params) {
            try {
                return insertTripHelper(mDir, mFrom, mTo, mComment, mCostCenter, mDefaultCurrencyCode);
            } catch (SQLException ex) {
                mException = ex;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Trip result) {
            if (result != null) {
                synchronized (mTripCacheLock) {
                    mAreTripsValid = false;
                }
                if (mTripRowListener != null) {
                    mTripRowListener.onTripRowInsertSuccess(result);
                }
            } else {
                if (mTripRowListener != null) {
                    mTripRowListener.onTripRowInsertFailure(mException, mDir);
                }
            }
        }

    }

    public final Trip updateTripSerial(Trip oldTrip, File dir, Date from, Date to, String comment, String costCenter, String defaultCurrencyCode) {
        Trip trip = updateTripHelper(oldTrip, dir, from, to, comment, costCenter, defaultCurrencyCode);
        if (trip != null) {
            synchronized (mTripCacheLock) {
                mAreTripsValid = false;
            }
        }
        return trip;
    }

    public void updateTripParallel(Trip oldTrip, File dir, Date from, Date to, String comment, String costCenter, String defaultCurrencyCode) {
        if (mTripRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No TripRowListener was registered.");
            }
        }
        (new UpdateTripRowWorker(oldTrip, dir, from, to, comment, costCenter, defaultCurrencyCode)).execute();
    }

    private Trip updateTripHelper(Trip oldTrip, File dir, Date from, Date to, String comment, String costCenter, String defaultCurrencyCode) {
        ContentValues values = new ContentValues(10);
        values.put(TripsTable.COLUMN_NAME, dir.getName());
        values.put(TripsTable.COLUMN_FROM, from.getTime());
        values.put(TripsTable.COLUMN_TO, to.getTime());
        TimeZone startTimeZone = oldTrip.getStartTimeZone();
        TimeZone endTimeZone = oldTrip.getEndTimeZone();
        if (!from.equals(oldTrip.getStartDate())) { // Update time zone if date changed
            startTimeZone = TimeZone.getDefault();
            values.put(TripsTable.COLUMN_FROM_TIMEZONE, startTimeZone.getID());
        }
        if (!to.equals(oldTrip.getEndDate())) { // Update time zone if date changed
            endTimeZone = TimeZone.getDefault();
            values.put(TripsTable.COLUMN_TO_TIMEZONE, endTimeZone.getID());
        }
        values.put(TripsTable.COLUMN_COMMENT, comment);
        values.put(TripsTable.COLUMN_COST_CENTER, costCenter);
        values.put(TripsTable.COLUMN_DEFAULT_CURRENCY, defaultCurrencyCode);
        synchronized (mDatabaseLock) {
            SQLiteDatabase db;
            try {
                db = this.getWritableDatabase();
                if ((db.update(TripsTable.TABLE_NAME, values, TripsTable.COLUMN_NAME + " = ?", new String[]{oldTrip.getName()}) == 0)) {
                    return null;
                } else {
                    if (!oldTrip.getName().equalsIgnoreCase(dir.getName())) {
                        synchronized (mReceiptCacheLock) {
                            if (mReceiptCache.containsKey(oldTrip)) {
                                mReceiptCache.remove(oldTrip);
                            }
                        }
                        final String oldName = oldTrip.getName();
                        final String newName = dir.getName();
                        final ContentValues rcptVals = new ContentValues(1);
                        final ContentValues distVals = new ContentValues(1);
                        rcptVals.put(ReceiptsTable.COLUMN_PARENT, newName);
                        distVals.put(DistanceTable.COLUMN_PARENT, newName);
                        db.update(ReceiptsTable.TABLE_NAME, rcptVals, ReceiptsTable.COLUMN_PARENT + " = ?", new String[]{oldName});
                        db.update(DistanceTable.TABLE_NAME, distVals, DistanceTable.COLUMN_PARENT + " = ?", new String[]{oldName});
                        // TODO: Build in a rollback mechanism if the update fails
                    }
                    return (new TripBuilderFactory()).setDirectory(dir).setStartDate(from).setEndDate(to).setStartTimeZone(startTimeZone).setEndTimeZone(endTimeZone).setComment(comment).setCostCenter(costCenter).setDefaultCurrency(defaultCurrencyCode, mPersistenceManager.getPreferences().getDefaultCurreny()).setSourceAsCache().build();
                }
            } catch (SQLException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString());
                }
                return null;
            }
        }
    }

    private class UpdateTripRowWorker extends AsyncTask<Void, Void, Trip> {

        private final File mDir;
        private final Date mFrom, mTo;
        private final String mComment, mCostCenter, mDefaultCurrencyCode;
        private final Trip mOldTrip;

        public UpdateTripRowWorker(Trip oldTrip, File dir, Date from, Date to, String comment, String costCenter, String defaultCurrencyCode) {
            mOldTrip = oldTrip;
            mDir = dir;
            mFrom = from;
            mTo = to;
            mComment = comment;
            mCostCenter = costCenter;
            mDefaultCurrencyCode = defaultCurrencyCode;
        }

        @Override
        protected Trip doInBackground(Void... params) {
            return updateTripHelper(mOldTrip, mDir, mFrom, mTo, mComment, mCostCenter, mDefaultCurrencyCode);
        }

        @Override
        protected void onPostExecute(Trip result) {
            if (result != null) {
                synchronized (mTripCacheLock) {
                    mAreTripsValid = false;
                }
                if (mTripRowListener != null) {
                    mTripRowListener.onTripRowUpdateSuccess(result);
                }
            } else {
                if (mTripRowListener != null) {
                    mTripRowListener.onTripRowUpdateFailure(result, mOldTrip, mDir);
                }
            }
        }

    }

    public boolean deleteTripSerial(Trip trip) {
        if (mTripRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No TripRowListener was registered.");
            }
        }
        boolean success = deleteTripHelper(trip);
        if (success) {
            synchronized (mTripCacheLock) {
                mAreTripsValid = false;
            }
        }
        return success;
    }

    public void deleteTripParallel(Trip trip) {
        (new DeleteTripRowWorker()).execute(trip);
    }

    private boolean deleteTripHelper(Trip trip) {
        boolean success = false;
        SQLiteDatabase db = null;
        db = this.getWritableDatabase();
        // Delete all child receipts (technically ON DELETE CASCADE should handle this, but i'm not certain)
        synchronized (mDatabaseLock) {
            // TODO: Fix errors when the disk is not yet mounted
            success = (db.delete(ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_PARENT + " = ?", new String[]{trip.getName()}) >= 0);
            success &= (db.delete(DistanceTable.TABLE_NAME, DistanceTable.COLUMN_PARENT + " = ?", new String[]{trip.getName()}) >= 0);
        }
        if (success) {
            synchronized (mReceiptCacheLock) {
                mReceiptCache.remove(trip);
            }
        } else {
            return false;
        }
        synchronized (mDatabaseLock) {
            success = (db.delete(TripsTable.TABLE_NAME, TripsTable.COLUMN_NAME + " = ?", new String[]{trip.getName()}) > 0);
        }
        return success;
    }

    private class DeleteTripRowWorker extends AsyncTask<Trip, Void, Boolean> {

        private Trip mOldTrip;

        @Override
        protected Boolean doInBackground(Trip... params) {
            if (params == null || params.length == 0) {
                return false;
            }
            mOldTrip = params[0];
            return deleteTripHelper(mOldTrip);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                synchronized (mTripCacheLock) {
                    mAreTripsValid = false;
                }
            }
            if (mTripRowListener != null) {
                if (result) {
                    mTripRowListener.onTripDeleteSuccess(mOldTrip);
                } else {
                    mTripRowListener.onTripDeleteFailure();
                }
            }
        }

    }

    public final boolean addMiles(final Trip trip, final String delta) {
        try {
            final SQLiteDatabase db = this.getReadableDatabase();

            DecimalFormat format = new DecimalFormat();
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            format.setGroupingUsed(false);

            final float currentMiles = trip.getMileage();
            final float deltaMiles = format.parse(delta).floatValue();
            float total = currentMiles + deltaMiles;
            if (total < 0) {
                total = 0;
            }
            ContentValues values = new ContentValues(1);
            values.put(TripsTable.COLUMN_MILEAGE, total);
            trip.setMileage(total);
            return (db.update(TripsTable.TABLE_NAME, values, TripsTable.COLUMN_NAME + " = ?", new String[]{trip.getName()}) > 0);
        } catch (NumberFormatException e) {
            return false;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * This class is not synchronized! Sync outside of it
     *
     * @param trip
     * @return
     */
    private void getTripPriceAndDailyPrice(final Trip trip) {
        queryTripPrice(trip);
        queryTripDailyPrice(trip);
    }

    /**
     * Queries the trips price and updates this object. This class is not synchronized! Sync outside of it
     *
     * @param trip the trip, which will be updated
     */
    private void queryTripPrice(final Trip trip) {
        SQLiteDatabase db = null;
        Cursor receiptPriceCursor = null;
        Cursor distancePriceCursor = null;
        try {
            mAreTripsValid = false;
            db = this.getReadableDatabase();

            String receiptSelection = ReceiptsTable.COLUMN_PARENT + "= ?";
            if (mPersistenceManager.getPreferences().onlyIncludeExpensableReceiptsInReports()) {
                receiptSelection += " AND " + ReceiptsTable.COLUMN_EXPENSEABLE + " = 1";
            }

            // Get the Trip's total Price
            BigDecimal price = new BigDecimal(0);
            WBCurrency currency = trip.getDefaultCurrency();
            boolean firstPass = true;
            receiptPriceCursor = db.query(ReceiptsTable.TABLE_NAME, new String[]{ReceiptsTable.COLUMN_PRICE, ReceiptsTable.COLUMN_ISO4217}, receiptSelection, new String[]{trip.getName()}, null, null, null);
            if (receiptPriceCursor != null && receiptPriceCursor.moveToFirst() && receiptPriceCursor.getColumnCount() > 0) {
                do {
                    price = price.add(getDecimal(receiptPriceCursor, 0));
                    if (firstPass) {
                        currency = WBCurrency.getInstance(receiptPriceCursor.getString(1));
                        firstPass = false;
                    }
                    else {
                        if (currency != null && !currency.equals(WBCurrency.getInstance(receiptPriceCursor.getString(1)))) {
                            currency = null;
                        }
                    }
                }
                while (receiptPriceCursor.moveToNext());
            }

            if (mPersistenceManager.getPreferences().getShouldTheDistancePriceBeIncludedInReports()) {
                final String distanceSelection = DistanceTable.COLUMN_PARENT + " = ?";
                final String[] distanceColumns = new String[] {DistanceTable.COLUMN_DISTANCE + "*" + DistanceTable.COLUMN_RATE, DistanceTable.COLUMN_RATE_CURRENCY};
                distancePriceCursor = db.query(DistanceTable.TABLE_NAME, distanceColumns, distanceSelection, new String[] {trip.getName()}, null, null, null);
                if (distancePriceCursor != null && distancePriceCursor.moveToFirst() && distancePriceCursor.getColumnCount() > 0) {
                    do {
                        price = price.add(getDecimal(distancePriceCursor, 0));
                        if (firstPass) {
                            currency = WBCurrency.getInstance(distancePriceCursor.getString(1));
                            firstPass = false;
                        }
                        else {
                            if (currency != null && !currency.equals(WBCurrency.getInstance(distancePriceCursor.getString(1)))) {
                                currency = null;
                            }
                        }
                    }
                    while (distancePriceCursor.moveToNext());
                }
            }

            trip.setPrice(new PriceBuilderFactory().setPrice(price).setCurrency(currency).build());
        } finally {
            if (receiptPriceCursor != null) {
                receiptPriceCursor.close();
            }
            if (distancePriceCursor != null) {
                distancePriceCursor.close();
            }
        }
    }

    /**
     * Queries the trips daily total price and updates this object. This class is not synchronized! Sync outside of it
     *
     * @param trip the trip, which will be updated
     */
    private void queryTripDailyPrice(final Trip trip) {
        SQLiteDatabase db = null;
        Cursor receiptPriceCursor = null;
        Cursor distancePriceCursor = null;
        try {
            db = this.getReadableDatabase();

            // Build a calendar for the start of today
            final Time now = new Time();
            now.setToNow();
            final Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTimeInMillis(now.toMillis(false));
            startCalendar.setTimeZone(TimeZone.getDefault());
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MILLISECOND, 0);

            // Build a calendar for the end date
            final Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTimeInMillis(now.toMillis(false));
            endCalendar.setTimeZone(TimeZone.getDefault());
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
            endCalendar.set(Calendar.SECOND, 59);
            endCalendar.set(Calendar.MILLISECOND, 999);

            // Set the timers
            final long startTime = startCalendar.getTimeInMillis();
            final long endTime = endCalendar.getTimeInMillis();
            String selection = ReceiptsTable.COLUMN_PARENT + "= ? AND " + ReceiptsTable.COLUMN_DATE + " >= ? AND " + ReceiptsTable.COLUMN_DATE + " <= ?";
            if (mPersistenceManager.getPreferences().onlyIncludeExpensableReceiptsInReports()) {
                selection += " AND " + ReceiptsTable.COLUMN_EXPENSEABLE + " = 1";
            }

            BigDecimal subTotal = new BigDecimal(0);
            WBCurrency currency = trip.getDefaultCurrency();
            boolean firstPass = true;
            receiptPriceCursor = db.query(ReceiptsTable.TABLE_NAME, new String[]{ReceiptsTable.COLUMN_PRICE, ReceiptsTable.COLUMN_ISO4217}, selection, new String[]{trip.getName(), Long.toString(startTime), Long.toString(endTime)}, null, null, null);
            if (receiptPriceCursor != null && receiptPriceCursor.moveToFirst() && receiptPriceCursor.getColumnCount() > 0) {
                do {
                    subTotal = subTotal.add(getDecimal(receiptPriceCursor, 0));
                    if (firstPass) {
                        currency = WBCurrency.getInstance(receiptPriceCursor.getString(1));
                        firstPass = false;
                    }
                    else {
                        if (currency != null && !currency.equals(WBCurrency.getInstance(receiptPriceCursor.getString(1)))) {
                            currency = null;
                        }
                    }
                }
                while (receiptPriceCursor.moveToNext());
            }

            if (mPersistenceManager.getPreferences().getShouldTheDistancePriceBeIncludedInReports()) {
                final String distanceSelection = DistanceTable.COLUMN_PARENT + " = ? AND " + DistanceTable.COLUMN_DATE + " >= ? AND " + DistanceTable.COLUMN_DATE + " <= ?";
                final String[] distanceColumns = new String[] {DistanceTable.COLUMN_DISTANCE + "*" + DistanceTable.COLUMN_RATE, DistanceTable.COLUMN_RATE_CURRENCY};
                distancePriceCursor = db.query(DistanceTable.TABLE_NAME, distanceColumns, distanceSelection, new String[] {trip.getName()}, null, null, null);
                if (distancePriceCursor != null && distancePriceCursor.moveToFirst() && distancePriceCursor.getColumnCount() > 0) {
                    do {
                        subTotal = subTotal.add(getDecimal(distancePriceCursor, 0));
                        if (firstPass) {
                            currency = WBCurrency.getInstance(distancePriceCursor.getString(1));
                            firstPass = false;
                        }
                        else {
                            if (currency != null && !currency.equals(WBCurrency.getInstance(distancePriceCursor.getString(1)))) {
                                currency = null;
                            }
                        }
                    }
                    while (distancePriceCursor.moveToNext());
                }
            }

            trip.setDailySubTotal(new PriceBuilderFactory().setPrice(subTotal).setCurrency(currency).build());
        } finally { // Close the cursor to avoid memory leaks
            if (receiptPriceCursor != null) {
                receiptPriceCursor.close();
            }
            if (distancePriceCursor != null) {
                distancePriceCursor.close();
            }
        }
    }

    private void updateTripPrice(final Trip trip) {
        synchronized (mDatabaseLock) {
            mAreTripsValid = false;
            queryTripPrice(trip);
            queryTripDailyPrice(trip);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //	Distance Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void registerDistanceRowListener(DistanceRowListener listener) {
        mDistanceRowListener = listener;
    }

    public void unregisterDistanceRowListener() {
        mDistanceRowListener = null;
    }

    public List<Distance> getDistanceSerial(final Trip trip) {
        return this.getDistanceHelper(trip);
    }

    public void getDistanceParallel(final Trip trip) {
        if (mDistanceRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No DistanceRowListener was registered.");
            }
        }

        (new GetDistanceWorker(trip)).execute();
    }

    private List<Distance> getDistanceHelper(final Trip trip) {
        List<Distance> distances;
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = this.getReadableDatabase();
                c = db.query(DistanceTable.TABLE_NAME,
                        null,
                        DistanceTable.COLUMN_PARENT + "= ?",
                        new String[]{trip.getName()},
                        null,
                        null,
                        DistanceTable.COLUMN_DATE + " DESC");
                if (c != null && c.moveToFirst()) {
                    distances = new ArrayList<Distance>(c.getCount());
                    final int idIndex = c.getColumnIndex(DistanceTable.COLUMN_ID);
                    final int locationIndex = c.getColumnIndex(DistanceTable.COLUMN_LOCATION);
                    final int distanceIndex = c.getColumnIndex(DistanceTable.COLUMN_DISTANCE);
                    final int dateIndex = c.getColumnIndex(DistanceTable.COLUMN_DATE);
                    final int timezoneIndex = c.getColumnIndex(DistanceTable.COLUMN_TIMEZONE);
                    final int rateIndex = c.getColumnIndex(DistanceTable.COLUMN_RATE);
                    final int rateCurrencyIndex = c.getColumnIndex(DistanceTable.COLUMN_RATE_CURRENCY);
                    final int commentIndex = c.getColumnIndex(DistanceTable.COLUMN_COMMENT);
                    do {
                        final int id = c.getInt(idIndex);
                        final String location = c.getString(locationIndex);
                        final BigDecimal distance = BigDecimal.valueOf(c.getDouble(distanceIndex));
                        final long date = c.getLong(dateIndex);
                        final String timezone = c.getString(timezoneIndex);
                        final BigDecimal rate = BigDecimal.valueOf(c.getDouble(rateIndex));
                        final String rateCurrency = c.getString(rateCurrencyIndex);
                        final String comment = c.getString(commentIndex);

                        distances.add(new DistanceBuilderFactory(id)
                                .setTrip(trip)
                                .setLocation(location)
                                .setDistance(distance)
                                .setDate(date)
                                .setTimezone(timezone)
                                .setRate(rate)
                                .setCurrency(rateCurrency)
                                .setComment(comment)
                                .build());
                    }
                    while (c.moveToNext());
                } else {
                    distances = new ArrayList<Distance>();
                }
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }

        return distances;
    }

    private class GetDistanceWorker extends AsyncTask<Void, Void, List<Distance>> {

        private final Trip mTrip;

        public GetDistanceWorker(final Trip trip) {
            mTrip = trip;
        }

        @Override
        protected List<Distance> doInBackground(Void... params) {
            return getDistanceHelper(mTrip);
        }

        @Override
        protected void onPostExecute(List<Distance> result) {
            if (mDistanceRowListener != null) {
                mDistanceRowListener.onDistanceRowsQuerySuccess(result);
            }
        }
    }

    public Distance insertDistanceSerial(
            final Trip trip,
            final String location,
            final BigDecimal distance,
            final Date date,
            final BigDecimal rate,
            final String rateCurrency,
            final String comment) {

        return insertDistanceHelper(trip, location, distance, date, rate, rateCurrency, comment);
    }

    public void insertDistanceParallel(
            final Trip trip,
            final String location,
            final BigDecimal distance,
            final Date date,
            final BigDecimal rate,
            final String rateCurrency,
            final String comment) {

        if (mDistanceRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No DistanceRowListener was registered.");
            }
        }

        new InsertDistanceWorker(trip, location, distance, date, rate, rateCurrency, comment).execute();
    }

    private Distance insertDistanceHelper(
            final Trip trip,
            final String location,
            final BigDecimal distance,
            final Date date,
            final BigDecimal rate,
            final String rateCurrency,
            final String comment) {

        final int distanceCount = getDistanceSerial(trip).size() + 1;
        final TimeZone timeZone = TimeZone.getDefault();
        final ContentValues values = new ContentValues(8);
        values.put(DistanceTable.COLUMN_PARENT, trip.getName());
        values.put(DistanceTable.COLUMN_LOCATION, location.trim());
        values.put(DistanceTable.COLUMN_DISTANCE, distance.doubleValue());
        // Fudge the data millis by the distance count (for this trip) to help with uniqueness
        values.put(DistanceTable.COLUMN_DATE, date.getTime() + distanceCount);
        values.put(DistanceTable.COLUMN_TIMEZONE, timeZone.getID());
        values.put(DistanceTable.COLUMN_RATE, rate.doubleValue());
        values.put(DistanceTable.COLUMN_RATE_CURRENCY, rateCurrency);
        values.put(DistanceTable.COLUMN_COMMENT, comment.trim());

        Distance toReturn = null;
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = this.getWritableDatabase();
            if (db.insertOrThrow(DistanceTable.TABLE_NAME, null, values) == -1) {
                toReturn = null;
            } else {
                Cursor cur = null;
                try {
                    cur = db.rawQuery("SELECT last_insert_rowid()", null);
                    if (cur != null && cur.moveToFirst() && cur.getColumnCount() > 0) {
                        final int id = cur.getInt(0);
                        toReturn = new DistanceBuilderFactory(id)
                                .setTrip(trip)
                                .setLocation(location)
                                .setDistance(distance)
                                .setDate(date)
                                .setTimezone(timeZone)
                                .setRate(rate)
                                .setCurrency(rateCurrency)
                                .setComment(comment)
                                .build();
                        this.updateTripPrice(trip);
                    }
                } finally {
                    if (cur != null) {
                        cur.close();
                    }
                }
            }
        }

        return toReturn;
    }

    private class InsertDistanceWorker extends AsyncTask<Void, Void, Distance> {

        private final Trip mTrip;
        private final String mLocation;
        private final BigDecimal mDistance;
        private final Date mDate;
        private final BigDecimal mRate;
        private final String mRateCurrency;
        private final String mComment;

        private SQLException mException;

        public InsertDistanceWorker(
                final Trip trip,
                final String location,
                final BigDecimal distance,
                final Date date,
                final BigDecimal rate,
                final String rateCurrency,
                final String comment) {

            mTrip = trip;
            mLocation = location;
            mDistance = distance;
            mDate = date;
            mRate = rate;
            mRateCurrency = rateCurrency;
            mComment = comment;
        }

        @Override
        protected Distance doInBackground(Void... params) {
            try {
                return insertDistanceHelper(mTrip, mLocation, mDistance, mDate, mRate, mRateCurrency, mComment);
            } catch (SQLException exception) {
                mException = exception;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Distance result) {
            if (mDistanceRowListener == null)
                return;

            if (result == null || mException != null) { // implies exception
                mDistanceRowListener.onDistanceRowInsertFailure(mException);
            } else {
                mDistanceRowListener.onDistanceRowInsertSuccess(result);
            }
        }

    }

    public void updateDistanceSerial(
            final Distance oldDistance,
            final String location,
            final BigDecimal distance,
            final Date date,
            final BigDecimal rate,
            final String rateCurrency,
            final String comment) {

        updateDistanceHelper(oldDistance, location, distance, date, rate, rateCurrency, comment);
    }

    public void updateDistanceParallel(
            final Distance oldDistance,
            final String location,
            final BigDecimal distance,
            final Date date,
            final BigDecimal rate,
            final String rateCurrency,
            final String comment) {

        if (mDistanceRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No DistanceRowListener was registered.");
            }
        }

        new UpdateDistanceWorker(oldDistance, location, distance, date, rate, rateCurrency, comment).execute();
    }

    private Distance updateDistanceHelper(
            final Distance oldDistance,
            final String location,
            final BigDecimal distance,
            final Date date,
            final BigDecimal rate,
            final String rateCurrency,
            final String comment) {

        final TimeZone timeZone = TimeZone.getDefault();
        final ContentValues values = new ContentValues(7);
        values.put(DistanceTable.COLUMN_LOCATION, location.trim());
        values.put(DistanceTable.COLUMN_DISTANCE, distance.doubleValue());
        values.put(DistanceTable.COLUMN_DATE, date.getTime());
        values.put(DistanceTable.COLUMN_TIMEZONE, timeZone.getID());
        values.put(DistanceTable.COLUMN_RATE, rate.doubleValue());
        values.put(DistanceTable.COLUMN_RATE_CURRENCY, rateCurrency);
        values.put(DistanceTable.COLUMN_COMMENT, comment.trim());

        Distance toReturn = null;
        final int id = oldDistance.getId();
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = this.getWritableDatabase();
            if (db.update(DistanceTable.TABLE_NAME, values,
                    DistanceTable.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}) > 0) {

                toReturn = new DistanceBuilderFactory(id)
                        .setTrip(oldDistance.getTrip())
                        .setLocation(location)
                        .setDistance(distance)
                        .setDate(date)
                        .setTimezone(timeZone)
                        .setRate(rate)
                        .setCurrency(rateCurrency)
                        .setComment(comment)
                        .build();
                this.updateTripPrice(oldDistance.getTrip());

            } else {
                toReturn = null;
            }
        }

        return toReturn;
    }

    private class UpdateDistanceWorker extends AsyncTask<Void, Void, Distance> {

        private final Distance mOldDistance;
        private final String mLocation;
        private final BigDecimal mDistance;
        private final Date mDate;
        private final BigDecimal mRate;
        private final String mRateCurrency;
        private final String mComment;

        public UpdateDistanceWorker(
                final Distance oldDistance,
                final String location,
                final BigDecimal distance,
                final Date date,
                final BigDecimal rate,
                final String rateCurrency,
                final String comment) {


            mOldDistance = oldDistance;
            mLocation = location;
            mDistance = distance;
            mDate = date;
            mRate = rate;
            mRateCurrency = rateCurrency;
            mComment = comment;

        }

        @Override
        protected Distance doInBackground(Void... params) {
            return updateDistanceHelper(mOldDistance, mLocation, mDistance, mDate, mRate, mRateCurrency, mComment);
        }

        @Override
        protected void onPostExecute(Distance result) {
            if (mDistanceRowListener == null)
                return;

            if (result == null)
                mDistanceRowListener.onDistanceRowUpdateFailure();
            else
                mDistanceRowListener.onDistanceRowUpdateSuccess(result);
        }

    }

    public boolean deleteDistanceSerial(Distance distance) {
        return deleteDistanceHelper(distance);
    }

    public void deleteDistanceParallel(Distance distance) {
        if (mDistanceRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No DistanceRowListener was registered.");
            }
        }

        new DeleteDistanceWorker().execute(distance);
    }

    private boolean deleteDistanceHelper(Distance distance) {
        int result = -1;
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            db = this.getWritableDatabase();
            try {
                result = db.delete(DistanceTable.TABLE_NAME,
                        DistanceTable.COLUMN_ID + " = ?",
                        new String[]{String.valueOf(distance.getId())});
                this.updateTripPrice(distance.getTrip());
            } catch (Exception e) {
                return false;
            }
        }

        return result > 0;
    }

    private class DeleteDistanceWorker extends AsyncTask<Distance, Void, Boolean> {

        private Distance mDistance;

        @Override
        protected Boolean doInBackground(Distance... params) {
            if (params.length < 1)
                return false;

            mDistance = params[0];
            return deleteDistanceHelper(mDistance);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mDistanceRowListener == null)
                return;

            if (result)
                mDistanceRowListener.onDistanceDeleteSuccess(mDistance);
            else
                mDistanceRowListener.onDistanceDeleteFailure();
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //	ReceiptRow Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void registerReceiptRowListener(ReceiptRowListener listener) {
        mReceiptRowListener = listener;
    }

    public void unregisterReceiptRowListener() {
        mReceiptRowListener = null;
    }

    public List<Receipt> getReceiptsSerial(final Trip trip) {
        synchronized (mReceiptCacheLock) {
            if (mReceiptCache.containsKey(trip)) {
                return mReceiptCache.get(trip);
            }
        }
        return this.getReceiptsHelper(trip, true);
    }

    public List<Receipt> getReceiptsSerial(final Trip trip, final boolean desc) { // Only the email writer should
        return getReceiptsHelper(trip, desc);
    }

    public void getReceiptsParallel(final Trip trip) {
        if (mReceiptRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No ReceiptRowListener was registered.");
            }
        }
        synchronized (mReceiptCacheLock) {
            if (mReceiptCache.containsKey(trip)) { // only cache the default way (otherwise we get into issues with asc
                // v desc)
                if (mReceiptRowListener != null) {
                    mReceiptRowListener.onReceiptRowsQuerySuccess(mReceiptCache.get(trip));
                }
                return;
            }
        }
        (new GetReceiptsWorker()).execute(trip);
    }

    /**
     * Gets all receipts associated with a particular trip
     *
     * @param trip    - the trip
     * @param silence - silences the result (so no listeners will be alerted)
     */
    public void getReceiptsParallel(final Trip trip, boolean silence) {
        if (mReceiptRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No ReceiptRowListener was registered.");
            }
        }
        synchronized (mReceiptCacheLock) {
            if (mReceiptCache.containsKey(trip)) { // only cache the default way (otherwise we get into issues with asc
                // v desc)
                if (mReceiptRowListener != null) {
                    mReceiptRowListener.onReceiptRowsQuerySuccess(mReceiptCache.get(trip));
                }
                return;
            }
        }
        (new GetReceiptsWorker(true)).execute(trip);
    }

    private final List<Receipt> getReceiptsHelper(final Trip trip, final boolean desc) {
        List<Receipt> receipts;
        if (trip == null) {
            return new ArrayList<Receipt>();
        }
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = this.getReadableDatabase();
                c = db.query(ReceiptsTable.TABLE_NAME, null, ReceiptsTable.COLUMN_PARENT + "= ?", new String[]{trip.getName()}, null, null, ReceiptsTable.COLUMN_DATE + ((desc) ? " DESC" : " ASC"));
                if (c != null && c.moveToFirst()) {
                    receipts = new ArrayList<Receipt>(c.getCount());
                    final int idIndex = c.getColumnIndex(ReceiptsTable.COLUMN_ID);
                    final int pathIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PATH);
                    final int nameIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NAME);
                    final int categoryIndex = c.getColumnIndex(ReceiptsTable.COLUMN_CATEGORY);
                    final int priceIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PRICE);
                    final int taxIndex = c.getColumnIndex(ReceiptsTable.COLUMN_TAX);
                    final int exchangeRateIndex = c.getColumnIndex(ReceiptsTable.COLUMN_EXCHANGE_RATE);
                    final int dateIndex = c.getColumnIndex(ReceiptsTable.COLUMN_DATE);
                    final int timeZoneIndex = c.getColumnIndex(ReceiptsTable.COLUMN_TIMEZONE);
                    final int commentIndex = c.getColumnIndex(ReceiptsTable.COLUMN_COMMENT);
                    final int expenseableIndex = c.getColumnIndex(ReceiptsTable.COLUMN_EXPENSEABLE);
                    final int currencyIndex = c.getColumnIndex(ReceiptsTable.COLUMN_ISO4217);
                    final int fullpageIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE);
                    final int paymentMethodIdIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID);
                    final int extra_edittext_1_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1);
                    final int extra_edittext_2_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2);
                    final int extra_edittext_3_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3);
                    do {
                        final int id = c.getInt(idIndex);
                        final String path = c.getString(pathIndex);
                        final String name = c.getString(nameIndex);
                        final String category = c.getString(categoryIndex);
                        final double priceDouble = c.getDouble(priceIndex);
                        final double taxDouble = c.getDouble(taxIndex);
                        final double exchangeRateDouble = c.getDouble(exchangeRateIndex);
                        final String priceString = c.getString(priceIndex);
                        final String taxString = c.getString(taxIndex);
                        final String exchangeRateString = c.getString(exchangeRateIndex);
                        final long date = c.getLong(dateIndex);
                        final String timezone = (timeZoneIndex > 0) ? c.getString(timeZoneIndex) : null;
                        final String comment = c.getString(commentIndex);
                        final boolean expensable = c.getInt(expenseableIndex) > 0;
                        final String currency = c.getString(currencyIndex);
                        final boolean fullpage = !(c.getInt(fullpageIndex) > 0);
                        final int paymentMethodId = c.getInt(paymentMethodIdIndex); // Not using a join, since we need
                        final String extra_edittext_1 = c.getString(extra_edittext_1_Index);
                        final String extra_edittext_2 = c.getString(extra_edittext_2_Index);
                        final String extra_edittext_3 = c.getString(extra_edittext_3_Index);
                        File img = null;
                        if (!path.equalsIgnoreCase(DatabaseHelper.NO_DATA)) {
                            img = mPersistenceManager.getStorageManager().getFile(trip.getDirectory(), path);
                        }
                        final ReceiptBuilderFactory builder = new ReceiptBuilderFactory(id);
                        builder.setTrip(trip).setName(name).setCategory(category).setImage(img).setDate(date).setTimeZone(timezone).setComment(comment).setIsExpenseable(expensable).setCurrency(currency).setIsFullPage(fullpage).setIndex(c.getPosition() + 1).setPaymentMethod(findPaymentMethodById(paymentMethodId)).setExtraEditText1(extra_edittext_1).setExtraEditText2(extra_edittext_2).setExtraEditText3(extra_edittext_3);
                        /**
                         * Please note that a very frustrating bug exists here. Android cursors only return the first 6
                         * characters of a price string if that string contains a '.' character. It returns all of them
                         * if not. This means we'll break for prices over 5 digits unless we are using a comma separator, 
                         * which we'd do in the EU. Stupid check below to un-break this. Stupid Android.
                         *
                         * TODO: Longer term, everything should be saved with a decimal point
                         * https://code.google.com/p/android/issues/detail?id=22219
                         */
                        if (!TextUtils.isEmpty(priceString) && priceString.contains(",")) {
                            builder.setPrice(priceString);
                        } else {
                            builder.setPrice(priceDouble);
                        }
                        if (!TextUtils.isEmpty(taxString) && taxString.contains(",")) {
                            builder.setTax(taxString);
                        } else {
                            builder.setTax(taxDouble);
                        }
                        final ExchangeRateBuilderFactory exchangeRateBuilder = new ExchangeRateBuilderFactory().setBaseCurrency(trip.getDefaultCurrency());
                        if (!TextUtils.isEmpty(exchangeRateString) && exchangeRateString.contains(",")) {
                            exchangeRateBuilder.setRate(currency, exchangeRateString);
                        } else {
                            exchangeRateBuilder.setRate(currency, exchangeRateDouble);
                        }
                        builder.setExchangeRate(exchangeRateBuilder.build());
                        receipts.add(builder.build());
                    }
                    while (c.moveToNext());
                } else {
                    receipts = new ArrayList<Receipt>();
                }
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }
        synchronized (mReceiptCacheLock) {
            if (desc) {
                mReceiptCache.put(trip, receipts);
            }
        }
        return receipts;
    }

    private class GetReceiptsWorker extends AsyncTask<Trip, Void, List<Receipt>> {

        private final boolean mSilence;

        public GetReceiptsWorker() {
            mSilence = false;
        }

        public GetReceiptsWorker(boolean silence) {
            mSilence = silence;
        }

        @Override
        protected List<Receipt> doInBackground(Trip... params) {
            if (params == null || params.length == 0) {
                return new ArrayList<Receipt>();
            }
            Trip trip = params[0];
            return getReceiptsHelper(trip, true);
        }

        @Override
        protected void onPostExecute(List<Receipt> result) {
            if (mReceiptRowListener != null && !mSilence) {
                mReceiptRowListener.onReceiptRowsQuerySuccess(result);
            }
        }

    }

    public final Receipt getReceiptByID(final int id) {
        if (id <= 0) {
            return null;
        }
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = this.getReadableDatabase();
                c = db.query(ReceiptsTable.TABLE_NAME, null, ReceiptsTable.COLUMN_ID + "= ?", new String[]{Integer.toString(id)}, null, null, null);
                if (c != null && c.moveToFirst()) {
                    final int pathIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PATH);
                    final int parentIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
                    final int nameIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NAME);
                    final int categoryIndex = c.getColumnIndex(ReceiptsTable.COLUMN_CATEGORY);
                    final int priceIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PRICE);
                    final int taxIndex = c.getColumnIndex(ReceiptsTable.COLUMN_TAX);
                    final int exchangeRateIndex = c.getColumnIndex(ReceiptsTable.COLUMN_EXCHANGE_RATE);
                    final int dateIndex = c.getColumnIndex(ReceiptsTable.COLUMN_DATE);
                    final int timeZoneIndex = c.getColumnIndex(ReceiptsTable.COLUMN_TIMEZONE);
                    final int commentIndex = c.getColumnIndex(ReceiptsTable.COLUMN_COMMENT);
                    final int expenseableIndex = c.getColumnIndex(ReceiptsTable.COLUMN_EXPENSEABLE);
                    final int currencyIndex = c.getColumnIndex(ReceiptsTable.COLUMN_ISO4217);
                    final int fullpageIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE);
                    final int paymentMethodIdIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID);
                    final int extra_edittext_1_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1);
                    final int extra_edittext_2_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2);
                    final int extra_edittext_3_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3);
                    final String path = c.getString(pathIndex);
                    final String parent = c.getString(parentIndex);
                    final String name = c.getString(nameIndex);
                    final String category = c.getString(categoryIndex);
                    final double priceDouble = c.getDouble(priceIndex);
                    final double taxDouble = c.getDouble(taxIndex);
                    final double exchangeRateDouble = c.getDouble(exchangeRateIndex);
                    final String priceString = c.getString(priceIndex);
                    final String taxString = c.getString(taxIndex);
                    final String exchangeRateString = c.getString(exchangeRateIndex);
                    final long date = c.getLong(dateIndex);
                    final String timezone = c.getString(timeZoneIndex);
                    final String comment = c.getString(commentIndex);
                    final boolean expensable = c.getInt(expenseableIndex) > 0;
                    final String currency = c.getString(currencyIndex);
                    final boolean fullpage = !(c.getInt(fullpageIndex) > 0);
                    final int paymentMethodId = c.getInt(paymentMethodIdIndex); // Not using a join, since we need the
                    final String extra_edittext_1 = c.getString(extra_edittext_1_Index);
                    final String extra_edittext_2 = c.getString(extra_edittext_2_Index);
                    final String extra_edittext_3 = c.getString(extra_edittext_3_Index);
                    File img = null;
                    if (!path.equalsIgnoreCase(DatabaseHelper.NO_DATA)) {
                        final StorageManager storageManager = mPersistenceManager.getStorageManager();
                        img = storageManager.getFile(storageManager.getFile(parent), path);
                    }
                    final ReceiptBuilderFactory builder = new ReceiptBuilderFactory(id);
                    final Trip trip = getTripByName(parent);
                    builder.setTrip(trip).setName(name).setCategory(category).setImage(img).setDate(date).setTimeZone(timezone).setComment(comment).setIsExpenseable(expensable).setCurrency(currency).setIsFullPage(fullpage).setPaymentMethod(findPaymentMethodById(paymentMethodId)).setExtraEditText1(extra_edittext_1).setExtraEditText2(extra_edittext_2).setExtraEditText3(extra_edittext_3);
                    /**
                     * Please note that a very frustrating bug exists here. Android cursors only return the first 6
                     * characters of a price string if that string contains a '.' character. It returns all of them
                     * if not. This means we'll break for prices over 5 digits unless we are using a comma separator,
                     * which we'd do in the EU. Stupid check below to un-break this. Stupid Android.
                     *
                     * TODO: Longer term, everything should be saved with a decimal point
                     * https://code.google.com/p/android/issues/detail?id=22219
                     */
                    if (!TextUtils.isEmpty(priceString) && priceString.contains(",")) {
                        builder.setPrice(priceString);
                    } else {
                        builder.setPrice(priceDouble);
                    }
                    if (!TextUtils.isEmpty(taxString) && taxString.contains(",")) {
                        builder.setTax(taxString);
                    } else {
                        builder.setTax(taxDouble);
                    }
                    final ExchangeRateBuilderFactory exchangeRateBuilder = new ExchangeRateBuilderFactory().setBaseCurrency(trip.getDefaultCurrency());
                    if (!TextUtils.isEmpty(exchangeRateString) && exchangeRateString.contains(",")) {
                        exchangeRateBuilder.setRate(currency, exchangeRateString);
                    } else {
                        exchangeRateBuilder.setRate(currency, exchangeRateDouble);
                    }
                    builder.setExchangeRate(exchangeRateBuilder.build());
                    return builder.build();
                } else {
                    return null;
                }
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public Receipt insertReceiptSerial(Trip parent, Receipt receipt) throws SQLException {
        return insertReceiptSerial(parent, receipt, receipt.getFile());
    }

    public Receipt insertReceiptSerial(Trip parent, Receipt receipt, File newFile) throws SQLException {
        receipt.setFile(newFile);
        return insertReceiptHelper(receipt);
    }

    public void insertReceiptParallel(@NonNull Receipt receipt) {
        if (mReceiptRowListener == null) {
            Log.e(TAG, "No ReceiptRowListener was registered.");
        }
        new InsertReceiptWorker(receipt).execute();
    }

    private Receipt insertReceiptHelper(@NonNull Receipt receipt) throws SQLException {

        final int rcptNum = this.getReceiptsSerial(receipt.getTrip()).size() + 1; // Use this to order things more properly
        final StringBuilder stringBuilder = new StringBuilder(rcptNum + "_");
        final ContentValues values = new ContentValues(20);

        values.put(ReceiptsTable.COLUMN_PARENT, receipt.getTrip().getName());
        if (receipt.getName().length() > 0) {
            stringBuilder.append(receipt.getName().trim());
            values.put(ReceiptsTable.COLUMN_NAME, receipt.getName().trim());
        }
        values.put(ReceiptsTable.COLUMN_CATEGORY, receipt.getCategory());

        // In theory, this hack may cause issue if there are > 1000 receipts. I imagine other bugs will arise before this point
        values.put(ReceiptsTable.COLUMN_DATE, receipt.getDate().getTime() + rcptNum);

        values.put(ReceiptsTable.COLUMN_TIMEZONE, receipt.getTimeZone().getID());
        values.put(ReceiptsTable.COLUMN_COMMENT, receipt.getComment());
        values.put(ReceiptsTable.COLUMN_ISO4217, receipt.getPrice().getCurrencyCode());
        values.put(ReceiptsTable.COLUMN_EXPENSEABLE, receipt.isExpensable());
        values.put(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE, !receipt.isFullPage());
        if (receipt.getPrice().getDecimalFormattedPrice().length() > 0) {
            values.put(ReceiptsTable.COLUMN_PRICE, receipt.getPrice().getDecimalFormattedPrice().replace(",", "."));
        }
        if (receipt.getTax().getDecimalFormattedPrice().length() > 0) {
            values.put(ReceiptsTable.COLUMN_TAX, receipt.getTax().getDecimalFormattedPrice().replace(",", "."));
        }

        File file = receipt.getFile();
        if (file != null) {
            stringBuilder.append('.').append(StorageManager.getExtension(receipt.getFile()));
            final String newName = stringBuilder.toString();
            File renamedFile = mPersistenceManager.getStorageManager().getFile(receipt.getTrip().getDirectory(), newName);
            if (!renamedFile.exists()) { // If this file doesn't exist, let's rename our current one
                Log.e(TAG, "Changing image name from: " + receipt.getFile().getName() + " to: " + newName);
                file = mPersistenceManager.getStorageManager().rename(file, newName); // Returns oldFile on failure
            }
            values.put(ReceiptsTable.COLUMN_PATH, file.getName());
        }

        if (receipt.getPaymentMethod() != null) {
            values.put(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID, receipt.getPaymentMethod().getId());
        } else {
            final Integer integer = null;
            values.put(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID, integer);
        }

        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, receipt.getExtraEditText1());
        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2, receipt.getExtraEditText2());
        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, receipt.getExtraEditText3());

        Receipt insertReceipt;
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = this.getWritableDatabase();
                if (db.insertOrThrow(ReceiptsTable.TABLE_NAME, null, values) == -1) {
                    insertReceipt = null;
                } else {
                    this.updateTripPrice(receipt.getTrip());
                    if (mReceiptCache.containsKey(receipt.getTrip())) {
                        mReceiptCache.remove(receipt.getTrip());
                    }
                    c = db.rawQuery("SELECT last_insert_rowid()", null);
                    if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
                        final int id = c.getInt(0);
                        final Date newDate = new Date(values.getAsLong(ReceiptsTable.COLUMN_DATE));
                        final ReceiptBuilderFactory builder = new ReceiptBuilderFactory(id, receipt);
                        insertReceipt = builder.setDate(newDate).setFile(file).build();
                    } else {
                        insertReceipt = null;
                    }
                }
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }
        if (insertReceipt != null) {
            synchronized (mReceiptCacheLock) {
                if (mReceiptCache.containsKey(receipt.getTrip())) {
                    mReceiptCache.remove(receipt.getTrip());
                }
                mNextReceiptAutoIncrementId = -1;
            }
        }
        return insertReceipt;
    }

    private class InsertReceiptWorker extends AsyncTask<Void, Void, Receipt> {

        private final Receipt mReceipt;
        private SQLException mException;

        public InsertReceiptWorker(@NonNull Receipt receipt) {
            mReceipt = receipt;
        }

        @Override
        protected Receipt doInBackground(Void... params) {
            try {
                return insertReceiptHelper(mReceipt);
            } catch (SQLException ex) {
                mException = ex;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Receipt result) {
            if (mReceiptRowListener != null) {
                if (result != null) {
                    mReceiptRowListener.onReceiptRowInsertSuccess(result);
                } else {
                    mReceiptRowListener.onReceiptRowInsertFailure(mException);
                }
            }
        }
    }

    public Receipt updateReceiptSerial(@NonNull Receipt oldReceipt, @NonNull Receipt updatedReceipt) {
        return updateReceiptHelper(oldReceipt, updatedReceipt);
    }

    public void updateReceiptParallel(@NonNull Receipt oldReceipt, @NonNull Receipt updatedReceipt) {

        if (mReceiptRowListener == null) {
            Log.w(TAG, "No ReceiptRowListener was registered.");
        }
        new UpdateReceiptWorker(oldReceipt, updatedReceipt).execute();
    }

    private Receipt updateReceiptHelper(@NonNull Receipt oldReceipt, @NonNull Receipt updatedReceipt) {

        ContentValues values = new ContentValues(10);
        values.put(ReceiptsTable.COLUMN_NAME, updatedReceipt.getName().trim());

        TimeZone timeZone = oldReceipt.getTimeZone();
        if (!updatedReceipt.getDate().equals(oldReceipt.getDate())) { // Update the timezone if the date changes
            values.put(ReceiptsTable.COLUMN_TIMEZONE, updatedReceipt.getTimeZone().getID());
        }

        if ((updatedReceipt.getDate().getTime() % 3600000) == 0) {
            values.put(ReceiptsTable.COLUMN_DATE, updatedReceipt.getDate().getTime() + oldReceipt.getId());
        } else {
            values.put(ReceiptsTable.COLUMN_DATE, updatedReceipt.getDate().getTime());
        }

        if (updatedReceipt.getPrice().getDecimalFormattedPrice().length() > 0) {
            values.put(ReceiptsTable.COLUMN_PRICE, updatedReceipt.getPrice().getDecimalFormattedPrice().replace(",", "."));
        }
        if (updatedReceipt.getTax().getDecimalFormattedPrice().length() > 0) {
            values.put(ReceiptsTable.COLUMN_TAX, updatedReceipt.getTax().getDecimalFormattedPrice().replace(",", "."));
        }

        values.put(ReceiptsTable.COLUMN_CATEGORY, updatedReceipt.getCategory());
        values.put(ReceiptsTable.COLUMN_COMMENT, updatedReceipt.getComment());
        values.put(ReceiptsTable.COLUMN_ISO4217, updatedReceipt.getPrice().getCurrencyCode());
        values.put(ReceiptsTable.COLUMN_EXPENSEABLE, updatedReceipt.isExpensable());
        values.put(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE, !updatedReceipt.isFullPage());

        if (updatedReceipt.getPaymentMethod() != null) {
            values.put(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID, updatedReceipt.getPaymentMethod().getId());
        } else {
            final Integer integer = null;
            values.put(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID, integer);
        }

        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, updatedReceipt.getExtraEditText1());
        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2, updatedReceipt.getExtraEditText2());
        values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, updatedReceipt.getExtraEditText3());

        Receipt receiptToReturn;
        synchronized (mDatabaseLock) {
            try {
                final SQLiteDatabase db = this.getWritableDatabase();
                if ((db.update(ReceiptsTable.TABLE_NAME, values, ReceiptsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(oldReceipt.getId())}) == 0)) {
                    receiptToReturn = null;
                } else {
                    this.updateTripPrice(updatedReceipt.getTrip());
                    final ReceiptBuilderFactory builder = new ReceiptBuilderFactory(updatedReceipt);
                    receiptToReturn = builder.setDate(values.getAsLong(ReceiptsTable.COLUMN_DATE)).build();
                }
            } catch (SQLException e) {
                return null;
            }
        }
        synchronized (mReceiptCacheLock) {
            mNextReceiptAutoIncrementId = -1;
            if (receiptToReturn != null) {
                mReceiptCache.remove(receiptToReturn.getTrip());
            }
        }
        return receiptToReturn;
    }

    private class UpdateReceiptWorker extends AsyncTask<Void, Void, Receipt> {

        private final Receipt mOldReceipt;
        private final Receipt mUpdatedReceipt;

        public UpdateReceiptWorker(@NonNull Receipt oldReceipt, @NonNull Receipt updatedReceipt) {
            mOldReceipt = oldReceipt;
            mUpdatedReceipt = updatedReceipt;
        }

        @Override
        protected Receipt doInBackground(Void... params) {
            return updateReceiptHelper(mOldReceipt, mUpdatedReceipt);
        }

        @Override
        protected void onPostExecute(Receipt result) {
            if (mReceiptRowListener != null) {
                if (result != null) {
                    mReceiptRowListener.onReceiptRowUpdateSuccess(result);
                } else {
                    mReceiptRowListener.onReceiptRowUpdateFailure();
                }
            }
        }
    }

    public final Receipt updateReceiptFile(final Receipt oldReceipt, final File file) {
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            try {
                db = this.getReadableDatabase();
                ContentValues values = new ContentValues(1);
                if (file == null) {
                    values.put(ReceiptsTable.COLUMN_PATH, NO_DATA);
                } else {
                    values.put(ReceiptsTable.COLUMN_PATH, file.getName());
                }
                if (values == null || (db.update(ReceiptsTable.TABLE_NAME, values, ReceiptsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(oldReceipt.getId())}) == 0)) {
                    return null;
                } else {
                    synchronized (mReceiptCacheLock) {
                        mNextReceiptAutoIncrementId = -1;
                        mReceiptCache.remove(oldReceipt.getTrip());
                    }
                    oldReceipt.setFile(file);
                    return oldReceipt;
                }
            } catch (SQLException e) {
                return null;
            }
        }
    }

    public boolean copyReceiptSerial(Receipt receipt, Trip newTrip) {
        return copyReceiptHelper(receipt, newTrip);
    }

    public void copyReceiptParallel(Receipt receipt, Trip newTrip) {
        if (mReceiptRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No ReceiptRowListener was registered.");
            }
        }
        (new CopyReceiptWorker(receipt, newTrip)).execute(new Void[0]);
    }

    private boolean copyReceiptHelper(Receipt receipt, Trip newTrip) {
        File newFile = null;
        final StorageManager storageManager = mPersistenceManager.getStorageManager();
        if (receipt.hasFile()) {
            try {
                newFile = storageManager.getFile(newTrip.getDirectory(), receipt.getFileName());
                if (!storageManager.copy(receipt.getFile(), newFile, true)) {
                    newFile = null; // Unset on failed copy
                    return false;
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Successfully created a copy of " + receipt.getFileName() + " for " + receipt.getName() + " at " + newFile.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString());
                }
                return false;
            }
        }
        if (insertReceiptSerial(newTrip, receipt, newFile) != null) { // i.e. successfully inserted
            return true;
        } else {
            if (newFile != null) { // roll back
                storageManager.delete(newFile);
            }
            return false;
        }
    }

    private class CopyReceiptWorker extends AsyncTask<Void, Void, Boolean> {

        private final Receipt mReceipt;
        private final Trip mTrip;

        public CopyReceiptWorker(Receipt receipt, Trip currentTrip) {
            mReceipt = receipt;
            mTrip = currentTrip;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return copyReceiptHelper(mReceipt, mTrip);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mReceiptRowListener != null) {
                if (result) {
                    mReceiptRowListener.onReceiptCopySuccess(mTrip);
                } else {
                    mReceiptRowListener.onReceiptCopyFailure();
                }
            }
        }

    }

    public boolean moveReceiptSerial(Receipt receipt, Trip currentTrip, Trip newTrip) {
        return moveReceiptHelper(receipt, currentTrip, newTrip);
    }

    public void moveReceiptParallel(Receipt receipt, Trip currentTrip, Trip newTrip) {
        if (mReceiptRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No ReceiptRowListener was registered.");
            }
        }
        (new MoveReceiptWorker(receipt, currentTrip, newTrip)).execute(new Void[0]);
    }

    private boolean moveReceiptHelper(Receipt receipt, Trip currentTrip, Trip newTrip) {
        if (copyReceiptSerial(receipt, newTrip)) {
            if (deleteReceiptSerial(receipt, currentTrip)) {
                return true;
            } else {
                // TODO: Undo Copy here
                return false;
            }
        } else {
            return false;
        }
    }

    private class MoveReceiptWorker extends AsyncTask<Void, Void, Boolean> {

        private final Receipt mReceipt;
        private final Trip mCurrentTrip, mNewTrip;

        public MoveReceiptWorker(Receipt receipt, Trip currentTrip, Trip newTrip) {
            mReceipt = receipt;
            mCurrentTrip = currentTrip;
            mNewTrip = newTrip;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return moveReceiptHelper(mReceipt, mCurrentTrip, mNewTrip);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mReceiptRowListener != null) {
                if (result) {
                    mReceiptRowListener.onReceiptMoveSuccess(mNewTrip);
                } else {
                    mReceiptRowListener.onReceiptMoveFailure();
                }
            }
        }

    }

    public boolean deleteReceiptSerial(Receipt receipt, Trip currentTrip) {
        return deleteReceiptHelper(receipt, currentTrip);
    }

    public void deleteReceiptParallel(Receipt receipt, Trip currentTrip) {
        if (mReceiptRowListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No ReceiptRowListener was registered.");
            }
        }
        (new DeleteReceiptWorker(receipt, currentTrip)).execute(new Void[0]);
    }

    private boolean deleteReceiptHelper(Receipt receipt, Trip currentTrip) {
        boolean success = false;
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            db = this.getWritableDatabase();
            success = (db.delete(ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(receipt.getId())}) > 0);
        }
        if (success) {
            if (receipt.hasFile()) {
                success = success & mPersistenceManager.getStorageManager().delete(receipt.getFile());
            }
            this.updateTripPrice(currentTrip);
            synchronized (mReceiptCacheLock) {
                mNextReceiptAutoIncrementId = -1;
                mReceiptCache.remove(currentTrip);
            }
        }
        return success;
    }

    private class DeleteReceiptWorker extends AsyncTask<Void, Void, Boolean> {

        private final Receipt mReceipt;
        private final Trip mTrip;

        public DeleteReceiptWorker(Receipt receipt, Trip currentTrip) {
            mReceipt = receipt;
            mTrip = currentTrip;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return deleteReceiptHelper(mReceipt, mTrip);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mReceiptRowListener != null) {
                if (result) {
                    mReceiptRowListener.onReceiptDeleteSuccess(mReceipt);
                } else {
                    mReceiptRowListener.onReceiptDeleteFailure();
                }
            }
        }

    }

    public final boolean moveReceiptUp(final Trip trip, final Receipt receipt) {
        List<Receipt> receipts = getReceiptsSerial(trip);
        int index = 0;
        final int size = receipts.size();
        for (int i = 0; i < size; i++) {
            if (receipt.getId() == receipts.get(i).getId()) {
                index = i - 1;
                break;
            }
        }
        if (index < 0) {
            return false;
        }
        Receipt up = receipts.get(index);
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            try {
                db = this.getWritableDatabase();
                ContentValues upValues = new ContentValues(1);
                ContentValues downValues = new ContentValues(1);
                upValues.put(ReceiptsTable.COLUMN_DATE, receipt.getDate().getTime());
                if (receipt.getDate().getTime() != up.getDate().getTime()) {
                    downValues.put(ReceiptsTable.COLUMN_DATE, up.getDate().getTime());
                } else {
                    downValues.put(ReceiptsTable.COLUMN_DATE, up.getDate().getTime() + 1L);
                }
                if ((db.update(ReceiptsTable.TABLE_NAME, upValues, ReceiptsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(up.getId())}) == 0)) {
                    return false;
                }
                if ((db.update(ReceiptsTable.TABLE_NAME, downValues, ReceiptsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(receipt.getId())}) == 0)) {
                    return false;
                }
                mReceiptCache.remove(trip);
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
    }

    public final boolean moveReceiptDown(final Trip trip, final Receipt receipt) {
        List<Receipt> receipts = getReceiptsSerial(trip);
        final int size = receipts.size();
        int index = size - 1;
        for (int i = 0; i < receipts.size(); i++) {
            if (receipt.getId() == receipts.get(i).getId()) {
                index = i + 1;
                break;
            }
        }
        if (index > (size - 1)) {
            return false;
        }
        Receipt down = receipts.get(index);
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            try {
                db = this.getWritableDatabase();
                ContentValues upValues = new ContentValues(1);
                ContentValues downValues = new ContentValues(1);
                if (receipt.getDate().getTime() != down.getDate().getTime()) {
                    upValues.put(ReceiptsTable.COLUMN_DATE, down.getDate().getTime());
                } else {
                    upValues.put(ReceiptsTable.COLUMN_DATE, down.getDate().getTime() - 1L);
                }
                downValues.put(ReceiptsTable.COLUMN_DATE, receipt.getDate().getTime());
                if ((db.update(ReceiptsTable.TABLE_NAME, upValues, ReceiptsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(receipt.getId())}) == 0)) {
                    return false;
                }
                if ((db.update(ReceiptsTable.TABLE_NAME, downValues, ReceiptsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(down.getId())}) == 0)) {
                    return false;
                }
                mReceiptCache.remove(trip);
                return true;
            } catch (SQLException e) {
                return false;
            }
        }
    }

    public int getNextReceiptAutoIncremenetIdSerial() {
        return getNextReceiptAutoIncremenetIdHelper();
    }

    private int getNextReceiptAutoIncremenetIdHelper() {
        if (mNextReceiptAutoIncrementId > 0) {
            return mNextReceiptAutoIncrementId;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            synchronized (mDatabaseLock) {

                c = db.rawQuery("SELECT seq FROM SQLITE_SEQUENCE WHERE name=?", new String[]{ReceiptsTable.TABLE_NAME});
                if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
                    mNextReceiptAutoIncrementId = c.getInt(0) + 1;
                } else {
                    mNextReceiptAutoIncrementId = 1;
                }
                return mNextReceiptAutoIncrementId;
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // ReceiptRow Graph Methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public void registerReceiptRowGraphListener(ReceiptRowGraphListener listener) {
        mReceiptRowGraphListener = listener;
    }

    public void unregisterReceiptRowGraphListener() {
        mReceiptRowGraphListener = null;
    }

    /**
     * This basic internal delegate is used to prevent code reptition since all the queries will be the same. We only
     * have to swap out column names and how the receipts are built
     */
    private interface GraphProcessorDelegate {
        public String getXAxisColumn(); // refers to the field without aggregation

        public String getSumColumn(); // refers to the SUM() field in SQL

        public Receipt getReceipt(String xaxis, String sum);
    }

    private List<Receipt> getGraphColumnsSerial(Trip trip, GraphProcessorDelegate delegate) {
        return getGraphColumnsHelper(trip, delegate);
    }

    private void getGraphColumnsParallel(Trip trip, GraphProcessorDelegate delegate) {
        if (mReceiptRowGraphListener == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "No ReceiptRowGraphListener was registered.");
            }
        }
        (new GetGraphColumnsWorker(delegate)).execute(trip);
    }

    private class GetGraphColumnsWorker extends AsyncTask<Trip, Void, List<Receipt>> {

        private final GraphProcessorDelegate mDelegate;

        public GetGraphColumnsWorker(GraphProcessorDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
        protected List<Receipt> doInBackground(Trip... params) {
            if (params == null || params.length == 0) {
                return new ArrayList<Receipt>();
            }
            Trip trip = params[0];
            return getGraphColumnsHelper(trip, mDelegate);
        }

        @Override
        protected void onPostExecute(List<Receipt> result) {
            if (mReceiptRowGraphListener != null) {
                mReceiptRowGraphListener.onGraphQuerySuccess(result);
            }
        }

    }

    private final List<Receipt> getGraphColumnsHelper(Trip trip, GraphProcessorDelegate delegate) {
        List<Receipt> receipts;
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = this.getReadableDatabase();
                final String[] columns = new String[]{delegate.getXAxisColumn(), "SUM(" + delegate.getSumColumn() + ") AS " + delegate.getSumColumn()};
                c = db.query(ReceiptsTable.TABLE_NAME, columns, ReceiptsTable.COLUMN_PARENT + "= ?", new String[]{trip.getName()}, delegate.getXAxisColumn(), null, null);
                if (c != null && c.moveToFirst()) {
                    receipts = new ArrayList<Receipt>(c.getCount());
                    final int xIndex = c.getColumnIndex(delegate.getXAxisColumn());
                    final int sumIndex = c.getColumnIndex(delegate.getSumColumn());
                    do {
                        final String xaxis = c.getString(xIndex);
                        final String sum = c.getString(sumIndex);
                        receipts.add(delegate.getReceipt(xaxis, sum));
                    }
                    while (c.moveToNext());
                } else {
                    receipts = new ArrayList<Receipt>();
                }
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }
        return receipts;
    }

    private static class CostPerCategoryDelegate implements GraphProcessorDelegate {
        @Override
        public String getXAxisColumn() {
            return ReceiptsTable.COLUMN_CATEGORY;
        }

        @Override
        public String getSumColumn() {
            return ReceiptsTable.COLUMN_PRICE;
        }

        @Override
        public Receipt getReceipt(String xaxis, String sum) {
            return (new ReceiptBuilderFactory(-1)).setCategory(xaxis).setPrice(sum).build();
        }
    }

    public List<Receipt> getCostPerCategorySerial(final Trip trip) {
        return getGraphColumnsSerial(trip, new CostPerCategoryDelegate());
    }

    public void getCostPerCategoryParallel(final Trip trip) {
        getGraphColumnsParallel(trip, new CostPerCategoryDelegate());
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // Categories Methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public final ArrayList<CharSequence> getCategoriesList() {
        if (mCategoryList != null) {
            return mCategoryList;
        }
        if (mCategories == null) {
            buildCategories();
        }
        mCategoryList = new ArrayList<CharSequence>(mCategories.keySet());
        Collections.sort(mCategoryList, _charSequenceComparator);
        return mCategoryList;
    }

    private final CharSequenceComparator _charSequenceComparator = new CharSequenceComparator();

    private final class CharSequenceComparator implements Comparator<CharSequence> {
        @Override
        public int compare(CharSequence str1, CharSequence str2) {
            return str1.toString().compareToIgnoreCase(str2.toString());
        }
    }

    public final String getCategoryCode(CharSequence categoryName) {
        if (mCategories == null || mCategories.size() == 0) {
            buildCategories();
        }
        return mCategories.get(categoryName);
    }

    private final void buildCategories() {
        mCategories = new HashMap<String, String>();
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = this.getReadableDatabase();
                c = db.query(CategoriesTable.TABLE_NAME, null, null, null, null, null, null);
                if (c != null && c.moveToFirst()) {
                    final int nameIndex = c.getColumnIndex(CategoriesTable.COLUMN_NAME);
                    final int codeIndex = c.getColumnIndex(CategoriesTable.COLUMN_CODE);
                    do {
                        final String name = c.getString(nameIndex);
                        final String code = c.getString(codeIndex);
                        mCategories.put(name, code);
                    }
                    while (c.moveToNext());
                }
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public final ArrayList<CharSequence> getCurrenciesList() {
        if (mCurrencyList != null) {
            return mCurrencyList;
        }
        mCurrencyList = new ArrayList<CharSequence>();
        mCurrencyList.addAll(WBCurrency.getIso4217CurrencyCodes());
        mCurrencyList.addAll(WBCurrency.getNonIso4217CurrencyCodes());
        Collections.sort(mCurrencyList, _charSequenceComparator);
        return mCurrencyList;
    }

    public final boolean insertCategory(final String name, final String code) throws SQLException {
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues(2);
            values.put(CategoriesTable.COLUMN_NAME, name);
            values.put(CategoriesTable.COLUMN_CODE, code);
            if (db.insertOrThrow(CategoriesTable.TABLE_NAME, null, values) == -1) {
                return false;
            } else {
                mCategories.put(name, code);
                mCategoryList.add(name);
                Collections.sort(mCategoryList, _charSequenceComparator);
                return true;
            }
        }
    }

    @SuppressWarnings("resource")
    public final boolean insertCategoryNoCache(final String name, final String code) throws SQLException {
        final SQLiteDatabase db = (_initDB != null) ? _initDB : this.getReadableDatabase();
        ContentValues values = new ContentValues(2);
        values.put(CategoriesTable.COLUMN_NAME, name);
        values.put(CategoriesTable.COLUMN_CODE, code);
        if (db.insertOrThrow(CategoriesTable.TABLE_NAME, null, values) == -1) {
            return false;
        } else {
            return true;
        }
    }

    public final boolean updateCategory(final String oldName, final String newName, final String newCode) {
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues(2);
            values.put(CategoriesTable.COLUMN_NAME, newName);
            values.put(CategoriesTable.COLUMN_CODE, newCode);
            if (db.update(CategoriesTable.TABLE_NAME, values, CategoriesTable.COLUMN_NAME + " = ?", new String[]{oldName}) == 0) {
                return false;
            } else {
                mCategories.remove(oldName);
                mCategoryList.remove(oldName);
                mCategories.put(newName, newCode);
                mCategoryList.add(newName);
                Collections.sort(mCategoryList, _charSequenceComparator);
                return true;
            }

        }
    }

    public final boolean deleteCategory(final String name) {
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            db = this.getWritableDatabase();
            final boolean success = (db.delete(CategoriesTable.TABLE_NAME, CategoriesTable.COLUMN_NAME + " = ?", new String[]{name}) > 0);
            if (success) {
                mCategories.remove(name);
                mCategoryList.remove(name);
            }
            return success;
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // CSV Columns Methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public final List<Column<Receipt>> getCSVColumns() {
        if (mCSVColumns != null) {
            return mCSVColumns;
        }
        mCSVColumns = new ArrayList<Column<Receipt>>();
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = this.getReadableDatabase();
                c = db.query(CSVTable.TABLE_NAME, null, null, null, null, null, null);
                if (c != null && c.moveToFirst()) {
                    final int idIndex = c.getColumnIndex(CSVTable.COLUMN_ID);
                    final int typeIndex = c.getColumnIndex(CSVTable.COLUMN_TYPE);
                    do {
                        final int id = c.getInt(idIndex);
                        final String type = c.getString(typeIndex);
                        final Column<Receipt> column = new ColumnBuilderFactory<Receipt>(mReceiptColumnDefinitions).setColumnId(id).setColumnName(type).build();
                        mCSVColumns.add(column);
                    }
                    while (c.moveToNext());
                }
                return mCSVColumns;
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public final boolean insertCSVColumn() {
        final ContentValues values = new ContentValues(1);
        final Column<Receipt> defaultColumn = mReceiptColumnDefinitions.getDefaultInsertColumn();
        values.put(CSVTable.COLUMN_TYPE, defaultColumn.getName());
        if (mCSVColumns == null) {
            getCSVColumns();
        }
        synchronized (mDatabaseLock) {
            Cursor c = null;
            try {
                final SQLiteDatabase db = this.getWritableDatabase();
                if (db.insertOrThrow(CSVTable.TABLE_NAME, null, values) == -1) {
                    return false;
                } else {
                    c = db.rawQuery("SELECT last_insert_rowid()", null);
                    if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
                        final int id = c.getInt(0);
                        final Column<Receipt> column = new ColumnBuilderFactory<Receipt>(mReceiptColumnDefinitions).setColumnId(id).setColumnName(defaultColumn).build();
                        mCSVColumns.add(column);
                    } else {
                        return false;
                    }
                    return true;
                }
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public final boolean insertCSVColumnNoCache(String column) {
        final ContentValues values = new ContentValues(1);
        values.put(CSVTable.COLUMN_TYPE, column);
        if (_initDB != null) {
            if (_initDB.insertOrThrow(CSVTable.TABLE_NAME, null, values) == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            synchronized (mDatabaseLock) {
                final SQLiteDatabase db = this.getWritableDatabase();
                if (db.insertOrThrow(CSVTable.TABLE_NAME, null, values) == -1) {
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    public final boolean deleteCSVColumn() {
        synchronized (mDatabaseLock) {
            final SQLiteDatabase db = this.getWritableDatabase();
            final Column<Receipt> column = ListUtils.removeLast(mCSVColumns);
            if (column != null) {
                return db.delete(CSVTable.TABLE_NAME, CSVTable.COLUMN_ID + " = ?", new String[]{Integer.toString(column.getId())}) > 0;
            } else {
                return false;
            }
        }
    }

    public final boolean updateCSVColumn(Column<Receipt> oldColumn, Column<Receipt> newColumn) { // Note index here refers to the actual
        if (oldColumn.getName().equals(newColumn.getName())) {
            // Don't bother updating, since we've already set this column type
            return true;
        }
        final ContentValues values = new ContentValues(1);
        values.put(CSVTable.COLUMN_TYPE, newColumn.getName());
        synchronized (mDatabaseLock) {
            try {
                final SQLiteDatabase db = this.getWritableDatabase();
                if (db.update(CSVTable.TABLE_NAME, values, CSVTable.COLUMN_ID + " = ?", new String[]{Integer.toString(oldColumn.getId())}) == 0) {
                    return false;
                } else {
                    ListUtils.replace(mCSVColumns, oldColumn, newColumn);
                    return true;
                }
            } catch (SQLException e) {
                return false;
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // PDF Columns Methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public final List<Column<Receipt>> getPDFColumns() {
        if (mPDFColumns != null) {
            return mPDFColumns;
        }
        mPDFColumns = new ArrayList<Column<Receipt>>();
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = this.getReadableDatabase();
                c = db.query(PDFTable.TABLE_NAME, null, null, null, null, null, null);
                if (c != null && c.moveToFirst()) {
                    final int idIndex = c.getColumnIndex(PDFTable.COLUMN_ID);
                    final int typeIndex = c.getColumnIndex(PDFTable.COLUMN_TYPE);
                    do {
                        final int id = c.getInt(idIndex);
                        final String type = c.getString(typeIndex);
                        final Column<Receipt> column = new ColumnBuilderFactory<Receipt>(mReceiptColumnDefinitions).setColumnId(id).setColumnName(type).build();
                        mPDFColumns.add(column);
                    }
                    while (c.moveToNext());
                }
                return mPDFColumns;
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public final boolean insertPDFColumn() {
        final ContentValues values = new ContentValues(1);
        final Column<Receipt> defaultColumn = mReceiptColumnDefinitions.getDefaultInsertColumn();
        values.put(PDFTable.COLUMN_TYPE, defaultColumn.getName());
        if (mPDFColumns == null) {
            getPDFColumns();
        }
        synchronized (mDatabaseLock) {
            Cursor c = null;
            try {
                final SQLiteDatabase db = this.getWritableDatabase();
                if (db.insertOrThrow(PDFTable.TABLE_NAME, null, values) == -1) {
                    return false;
                } else {
                    c = db.rawQuery("SELECT last_insert_rowid()", null);
                    if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
                        final int id = c.getInt(0);
                        final Column<Receipt> column = new ColumnBuilderFactory<Receipt>(mReceiptColumnDefinitions).setColumnId(id).setColumnName(defaultColumn).build();
                        mPDFColumns.add(column);
                    } else {
                        return false;
                    }
                    return true;
                }
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public final boolean insertPDFColumnNoCache(String column) {
        final ContentValues values = new ContentValues(1);
        values.put(PDFTable.COLUMN_TYPE, column);
        if (_initDB != null) {
            if (_initDB.insertOrThrow(PDFTable.TABLE_NAME, null, values) == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            synchronized (mDatabaseLock) {
                final SQLiteDatabase db = this.getWritableDatabase();
                if (db.insertOrThrow(PDFTable.TABLE_NAME, null, values) == -1) {
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    public final boolean deletePDFColumn() {
        synchronized (mDatabaseLock) {
            final SQLiteDatabase db = this.getWritableDatabase();
            final Column<Receipt> column = ListUtils.removeLast(mPDFColumns);
            if (column != null) {
                return db.delete(PDFTable.TABLE_NAME, PDFTable.COLUMN_ID + " = ?", new String[]{Integer.toString(column.getId())}) > 0;
            } else {
                return false;
            }
        }
    }

    public final boolean updatePDFColumn(Column<Receipt> oldColumn, Column<Receipt> newColumn) { // Note index here refers to the actual
        if (oldColumn.getName().equals(newColumn.getName())) {
            // Don't bother updating, since we've already set this column type
            return true;
        }
        final ContentValues values = new ContentValues(1);
        values.put(PDFTable.COLUMN_TYPE, newColumn.getName());
        synchronized (mDatabaseLock) {
            try {
                final SQLiteDatabase db = this.getWritableDatabase();
                if (db.update(PDFTable.TABLE_NAME, values, PDFTable.COLUMN_ID + " = ?", new String[]{Integer.toString(oldColumn.getId())}) == 0) {
                    return false;
                } else {
                    ListUtils.replace(mPDFColumns, oldColumn, newColumn);
                    return true;
                }
            } catch (SQLException e) {
                return false;
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // PaymentMethod Methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Fetches the list of all {@link PaymentMethod}. This is done on the calling thread.
     *
     * @return the {@link List} of {@link PaymentMethod} objects that we've saved
     */
    public final List<PaymentMethod> getPaymentMethods() {
        if (mPaymentMethods != null) {
            return mPaymentMethods;
        }
        mPaymentMethods = new ArrayList<PaymentMethod>();
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = this.getReadableDatabase();
                c = db.query(PaymentMethodsTable.TABLE_NAME, null, null, null, null, null, null);
                if (c != null && c.moveToFirst()) {
                    final int idIndex = c.getColumnIndex(PaymentMethodsTable.COLUMN_ID);
                    final int methodIndex = c.getColumnIndex(PaymentMethodsTable.COLUMN_METHOD);
                    do {
                        final int id = c.getInt(idIndex);
                        final String method = c.getString(methodIndex);
                        final PaymentMethodBuilderFactory builder = new PaymentMethodBuilderFactory();
                        mPaymentMethods.add(builder.setId(id).setMethod(method).build());
                    }
                    while (c.moveToNext());
                }
                return mPaymentMethods;
            } finally { // Close the cursor and db to avoid memory leaks
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    /**
     * Attempts to fetch a payment method for a given primary key id
     *
     * @param id - the id of the desired {@link PaymentMethod}
     * @return a {@link PaymentMethod} if the id matches or {@code null} if none is found
     */
    public final PaymentMethod findPaymentMethodById(final int id) {
        final List<PaymentMethod> methodsSnapshot = new ArrayList<PaymentMethod>(getPaymentMethods());
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
     * that is returned from {@link #getPaymentMethods()}. This is done on the calling thread.
     *
     * @param method - a {@link String} representing the current method
     * @return a new {@link PaymentMethod} if it was successfully inserted, {@code null} if not
     */
    public final PaymentMethod insertPaymentMethod(final String method) {
        ContentValues values = new ContentValues(1);
        values.put(PaymentMethodsTable.COLUMN_METHOD, method);
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            Cursor c = null;
            try {
                db = this.getWritableDatabase();
                if (db.insertOrThrow(PaymentMethodsTable.TABLE_NAME, null, values) == -1) {
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
    }

    /**
     * Inserts a new {@link PaymentMethod} into our database. This method does not update the underlying list that is
     * returned via {{@link #getPaymentMethods()}
     *
     * @param method - a {@link String} representing the current method
     * @return {@code true} if it was properly inserted. {@code false} if not
     */
    public final boolean insertPaymentMethodNoCache(final String method) {
        ContentValues values = new ContentValues(1);
        values.put(PaymentMethodsTable.COLUMN_METHOD, method);
        if (_initDB != null) {
            if (_initDB.insertOrThrow(PaymentMethodsTable.TABLE_NAME, null, values) == -1) {
                return false;
            } else {
                return true;
            }
        } else {
            synchronized (mDatabaseLock) {
                SQLiteDatabase db = null;
                db = this.getWritableDatabase();
                if (db.insertOrThrow(PaymentMethodsTable.TABLE_NAME, null, values) == -1) {
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    /**
     * Updates a Payment method with a new method type. This method also automatically updates the underlying list that
     * is returned from {@link #getPaymentMethods()}. This is done on the calling thread.
     *
     * @param oldPaymentMethod - the old method to update
     * @param newMethod        - the new string to use as the method
     * @return the new {@link PaymentMethod}
     */
    public final PaymentMethod updatePaymentMethod(final PaymentMethod oldPaymentMethod, final String newMethod) {
        if (oldPaymentMethod == null) {
            Log.e(TAG, "The oldPaymentMethod is null. No update can be performed");
            return null;
        }
        if (oldPaymentMethod.getMethod() == null && newMethod == null) {
            return oldPaymentMethod;
        } else if (newMethod != null && newMethod.equals(oldPaymentMethod.getMethod())) {
            return oldPaymentMethod;
        }

        ContentValues values = new ContentValues(1);
        values.put(PaymentMethodsTable.COLUMN_METHOD, newMethod);
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            try {
                db = this.getWritableDatabase();
                if (db.update(PaymentMethodsTable.TABLE_NAME, values, PaymentMethodsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(oldPaymentMethod.getId())}) > 0) {
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
    }

    /**
     * Deletes a {@link PaymentMethod} from our database. This method also automatically updates the underlying list
     * that is returned from {@link #getPaymentMethods()}. This is done on the calling thread.
     *
     * @param paymentMethod - the {@link PaymentMethod} to delete
     * @return {@code true} if is was successfully remove. {@code false} otherwise
     */
    public final boolean deletePaymenthMethod(final PaymentMethod paymentMethod) {
        synchronized (mDatabaseLock) {
            SQLiteDatabase db = null;
            db = this.getWritableDatabase();
            if (db.delete(PaymentMethodsTable.TABLE_NAME, PaymentMethodsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(paymentMethod.getId())}) > 0) {
                if (mPaymentMethods != null) {
                    mPaymentMethods.remove(paymentMethod);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // Utilities
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public final synchronized boolean merge(String dbPath, String packageName, boolean overwrite) {
        mAreTripsValid = false;
        mReceiptCache.clear();
        synchronized (mDatabaseLock) {
            SQLiteDatabase importDB = null, currDB = null;
            Cursor c = null, countCursor = null;
            try {
                if (dbPath == null) {
                    mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Null database file");
                    return false;
                }
                currDB = this.getWritableDatabase();
                importDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
                // Merge Trips
                try {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Merging Trips");
                    }
                    mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Merging Trips");
                    c = importDB.query(TripsTable.TABLE_NAME, null, null, null, null, null, TripsTable.COLUMN_TO + " DESC");
                    if (c != null && c.moveToFirst()) {
                        final int nameIndex = c.getColumnIndex(TripsTable.COLUMN_NAME);
                        final int fromIndex = c.getColumnIndex(TripsTable.COLUMN_FROM);
                        final int fromTimeZoneIndex = c.getColumnIndex(TripsTable.COLUMN_FROM_TIMEZONE);
                        final int toIndex = c.getColumnIndex(TripsTable.COLUMN_TO);
                        final int toTimeZoneIndex = c.getColumnIndex(TripsTable.COLUMN_TO_TIMEZONE);
                        // final int priceIndex = c.getColumnIndex(TripsTable.COLUMN_PRICE);
                        final int mileageIndex = c.getColumnIndex(TripsTable.COLUMN_MILEAGE);
                        final int commentIndex = c.getColumnIndex(TripsTable.COLUMN_COMMENT);
                        final int filtersIndex = c.getColumnIndex(TripsTable.COLUMN_FILTERS);
                        final int costCenterIndex = c.getColumnIndex(TripsTable.COLUMN_COST_CENTER);
                        final int processingStatusIndex = c.getColumnIndex(TripsTable.COLUMN_PROCESSING_STATUS);
                        final int defaultCurrencyIndex = c.getColumnIndex(TripsTable.COLUMN_DEFAULT_CURRENCY);
                        do {
                            String name = getString(c, nameIndex, "");
                            if (name.contains("wb.receipts")) { // Backwards compatibility stuff
                                if (packageName.equalsIgnoreCase("wb.receipts")) {
                                    name = name.replace("wb.receiptspro/", "wb.receipts/");
                                } else if (packageName.equalsIgnoreCase("wb.receiptspro")) {
                                    name = name.replace("wb.receipts/", "wb.receiptspro/");
                                }
                                File f = new File(name);
                                name = f.getName();
                            }
                            final long from = getLong(c, fromIndex, 0L);
                            final long to = getLong(c, toIndex, 0L);
                            final int mileage = getInt(c, mileageIndex, 0);
                            final String comment = getString(c, commentIndex, "");
                            final String filters = getString(c, filtersIndex, "");
                            final String costCenter = getString(c, costCenterIndex, "");
                            final String processingStatus = getString(c, processingStatusIndex, "");
                            final String defaultCurrency = getString(c, defaultCurrencyIndex, mPersistenceManager.getPreferences().getDefaultCurreny());
                            ContentValues values = new ContentValues(10);
                            values.put(TripsTable.COLUMN_NAME, name);
                            values.put(TripsTable.COLUMN_FROM, from);
                            values.put(TripsTable.COLUMN_TO, to);
                            values.put(TripsTable.COLUMN_COMMENT, comment);
                            values.put(TripsTable.COLUMN_FILTERS, filters);
                            values.put(TripsTable.COLUMN_COST_CENTER, costCenter);
                            values.put(TripsTable.COLUMN_MILEAGE, mileage);
                            values.put(TripsTable.COLUMN_PROCESSING_STATUS, processingStatus);
                            values.put(TripsTable.COLUMN_DEFAULT_CURRENCY, defaultCurrency);
                            if (fromTimeZoneIndex > 0) {
                                final String fromTimeZome = c.getString(fromTimeZoneIndex);
                                values.put(TripsTable.COLUMN_FROM_TIMEZONE, fromTimeZome);
                            }
                            if (toTimeZoneIndex > 0) {
                                final String toTimeZome = c.getString(toTimeZoneIndex);
                                values.put(TripsTable.COLUMN_TO_TIMEZONE, toTimeZome);
                            }
                            if (overwrite) {
                                currDB.insertWithOnConflict(TripsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                            } else {
                                currDB.insertWithOnConflict(TripsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                            }
                        }
                        while (c.moveToNext());
                    }
                } catch (SQLiteException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.toString(), e); // Occurs if Table does not exist
                    }
                    mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Caught sql exception during import at [a1]: " + Utils.getStackTrace(e));
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                        c = null;
                    }
                }

                // Merge Receipts
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Merging Receipts");
                }
                mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Merging Receipts");
                try {
                    final String queryCount = "SELECT COUNT(*), " + ReceiptsTable.COLUMN_ID + " FROM " + ReceiptsTable.TABLE_NAME + " WHERE " + ReceiptsTable.COLUMN_PATH + "=? AND " + ReceiptsTable.COLUMN_NAME + "=? AND " + ReceiptsTable.COLUMN_DATE + "=?";
                    c = importDB.query(ReceiptsTable.TABLE_NAME, null, null, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        final int pathIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PATH);
                        final int nameIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NAME);
                        final int parentIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
                        final int categoryIndex = c.getColumnIndex(ReceiptsTable.COLUMN_CATEGORY);
                        final int priceIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PRICE);
                        final int dateIndex = c.getColumnIndex(ReceiptsTable.COLUMN_DATE);
                        final int commentIndex = c.getColumnIndex(ReceiptsTable.COLUMN_COMMENT);
                        final int expenseableIndex = c.getColumnIndex(ReceiptsTable.COLUMN_EXPENSEABLE);
                        final int currencyIndex = c.getColumnIndex(ReceiptsTable.COLUMN_ISO4217);
                        final int fullpageIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE);
                        final int extra_edittext_1_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1);
                        final int extra_edittext_2_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2);
                        final int extra_edittext_3_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3);
                        final int taxIndex = c.getColumnIndex(ReceiptsTable.COLUMN_TAX);
                        final int timeZoneIndex = c.getColumnIndex(ReceiptsTable.COLUMN_TIMEZONE);
                        final int paymentMethodIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID);
                        final int processingStatusIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PROCESSING_STATUS);
                        do {
                            final String oldPath = getString(c, pathIndex, "");
                            String newPath = new String(oldPath);
                            if (newPath.contains("wb.receipts")) { // Backwards compatibility stuff
                                if (packageName.equalsIgnoreCase("wb.receipts")) {
                                    newPath = oldPath.replace("wb.receiptspro/", "wb.receipts/");
                                } else if (packageName.equalsIgnoreCase("wb.receiptspro")) {
                                    newPath = oldPath.replace("wb.receipts/", "wb.receiptspro/");
                                }
                                File f = new File(newPath);
                                newPath = f.getName();
                            }
                            final String name = getString(c, nameIndex, "");
                            final String oldParent = getString(c, parentIndex, "");
                            String newParent = new String(oldParent);
                            if (newParent.contains("wb.receipts")) { // Backwards compatibility stuff
                                if (packageName.equalsIgnoreCase("wb.receipts")) {
                                    newParent = oldParent.replace("wb.receiptspro/", "wb.receipts/");
                                } else if (packageName.equalsIgnoreCase("wb.receiptspro")) {
                                    newParent = oldParent.replace("wb.receipts/", "wb.receiptspro/");
                                }
                                File f = new File(newParent);
                                newParent = f.getName();
                            }
                            final String category = getString(c, categoryIndex, "");
                            final BigDecimal price = getDecimal(c, priceIndex);
                            final long date = getLong(c, dateIndex, 0L);
                            final String comment = getString(c, commentIndex, "");
                            final boolean expensable = getBoolean(c, expenseableIndex, true);
                            final String currency = getString(c, currencyIndex, mPersistenceManager.getPreferences().getDefaultCurreny());
                            final boolean fullpage = getBoolean(c, fullpageIndex, false);
                            final String extra_edittext_1 = getString(c, extra_edittext_1_Index, null);
                            final String extra_edittext_2 = getString(c, extra_edittext_2_Index, null);
                            final String extra_edittext_3 = getString(c, extra_edittext_3_Index, null);
                            final BigDecimal tax = getDecimal(c, taxIndex);
                            final int paymentMethod = getInt(c, paymentMethodIndex, 0);
                            final String processingStatus = getString(c, processingStatusIndex, "");
                            try {
                                countCursor = currDB.rawQuery(queryCount, new String[]{newPath, name, Long.toString(date)});
                                if (countCursor != null && countCursor.moveToFirst()) {
                                    int count = countCursor.getInt(0);
                                    int updateID = countCursor.getInt(1);
                                    final ContentValues values = new ContentValues(14);
                                    values.put(ReceiptsTable.COLUMN_PATH, newPath);
                                    values.put(ReceiptsTable.COLUMN_NAME, name);
                                    values.put(ReceiptsTable.COLUMN_PARENT, newParent);
                                    values.put(ReceiptsTable.COLUMN_CATEGORY, category);
                                    values.put(ReceiptsTable.COLUMN_PRICE, price.doubleValue());
                                    values.put(ReceiptsTable.COLUMN_DATE, date);
                                    values.put(ReceiptsTable.COLUMN_COMMENT, comment);
                                    values.put(ReceiptsTable.COLUMN_EXPENSEABLE, expensable);
                                    values.put(ReceiptsTable.COLUMN_ISO4217, currency);
                                    values.put(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE, fullpage);
                                    values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, extra_edittext_1);
                                    values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2, extra_edittext_2);
                                    values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, extra_edittext_3);
                                    values.put(ReceiptsTable.COLUMN_TAX, tax.doubleValue());
                                    values.put(ReceiptsTable.COLUMN_PROCESSING_STATUS, processingStatus);
                                    if (timeZoneIndex > 0) {
                                        final String timeZone = c.getString(timeZoneIndex);
                                        values.put(ReceiptsTable.COLUMN_TIMEZONE, timeZone);
                                    }
                                    values.put(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID, paymentMethod);
                                    if (count > 0 && overwrite) { // Update
                                        currDB.update(ReceiptsTable.TABLE_NAME, values, ReceiptsTable.COLUMN_ID + " = ?", new String[]{Integer.toString(updateID)});
                                    } else { // insert
                                        if (overwrite) {
                                            currDB.insertWithOnConflict(ReceiptsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                        } else if (count == 0) {
                                            // If we're not overwriting anything, let's check that there are no entries here
                                            currDB.insertWithOnConflict(ReceiptsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                                        }
                                    }
                                }
                            } finally {
                                if (countCursor != null && !countCursor.isClosed()) {
                                    countCursor.close();
                                    countCursor = null;
                                }
                            }
                        }
                        while (c.moveToNext());
                    }
                } catch (SQLiteException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.toString(), e); // Occurs if Table does not exist
                    }
                    mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Caught sql exception during import at [a2]: " + Utils.getStackTrace(e));
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                        c = null;
                    }
                }

                // Merge Categories
                // No clean way to merge (since auto-increment is not guaranteed to have any order and there isn't
                // enough outlying data) => Always overwirte
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Merging Categories");
                }
                mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Merging Categories");
                try {
                    c = importDB.query(CategoriesTable.TABLE_NAME, null, null, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        currDB.delete(CategoriesTable.TABLE_NAME, null, null); // DELETE FROM Categories
                        final int nameIndex = c.getColumnIndex(CategoriesTable.COLUMN_NAME);
                        final int codeIndex = c.getColumnIndex(CategoriesTable.COLUMN_CODE);
                        final int breakdownIndex = c.getColumnIndex(CategoriesTable.COLUMN_BREAKDOWN);
                        do {
                            final String name = getString(c, nameIndex, "");
                            final String code = getString(c, codeIndex, "");
                            final boolean breakdown = getBoolean(c, breakdownIndex, true);
                            ContentValues values = new ContentValues(3);
                            values.put(CategoriesTable.COLUMN_NAME, name);
                            values.put(CategoriesTable.COLUMN_CODE, code);
                            values.put(CategoriesTable.COLUMN_BREAKDOWN, breakdown);
                            currDB.insert(CategoriesTable.TABLE_NAME, null, values);
                        }
                        while (c.moveToNext());
                    }
                } catch (SQLiteException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.toString(), e); // Occurs if Table does not exist
                    }
                    mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Caught sql exception during import at [a3]: " + Utils.getStackTrace(e));
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                        c = null;
                    }
                }

                // Merge CSV
                // No clean way to merge (since auto-increment is not guaranteed to have any order and there isn't
                // enough outlying data) => Always overwirte
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Merging CSV");
                }
                mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Merging CSV");
                try {
                    c = importDB.query(CSVTable.TABLE_NAME, null, null, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        currDB.delete(CSVTable.TABLE_NAME, null, null); // DELETE * FROM CSVTable
                        final int idxIndex = c.getColumnIndex(CSVTable.COLUMN_ID);
                        final int typeIndex = c.getColumnIndex(CSVTable.COLUMN_TYPE);
                        do {
                            final int index = getInt(c, idxIndex, 0);
                            final String type = getString(c, typeIndex, "");
                            ContentValues values = new ContentValues(2);
                            values.put(CSVTable.COLUMN_ID, index);
                            values.put(CSVTable.COLUMN_TYPE, type);
                            currDB.insert(CSVTable.TABLE_NAME, null, values);
                        }
                        while (c.moveToNext());
                    }
                } catch (SQLiteException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.toString(), e); // Occurs if Table does not exist
                    }
                    mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Caught sql exception during import at [a4]: " + Utils.getStackTrace(e));
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                        c = null;
                    }
                }

                // Merge PDF
                // No clean way to merge (since auto-increment is not guaranteed to have any order and there isn't
                // enough outlying data) => Always overwirte
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Merging PDF");
                }
                mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Merging PDF");
                try {
                    c = importDB.query(PDFTable.TABLE_NAME, null, null, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        currDB.delete(PDFTable.TABLE_NAME, null, null); // DELETE * FROM PDFTable
                        final int idxIndex = c.getColumnIndex(PDFTable.COLUMN_ID);
                        final int typeIndex = c.getColumnIndex(PDFTable.COLUMN_TYPE);
                        do {
                            final int index = getInt(c, idxIndex, 0);
                            final String type = getString(c, typeIndex, "");
                            ContentValues values = new ContentValues(2);
                            values.put(PDFTable.COLUMN_ID, index);
                            values.put(PDFTable.COLUMN_TYPE, type);
                            currDB.insert(PDFTable.TABLE_NAME, null, values);
                        }
                        while (c.moveToNext());
                    }
                } catch (SQLiteException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.toString(), e); // Occurs if Table does not exist
                    }
                    mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Caught sql exception during import at [a5]: " + Utils.getStackTrace(e));
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                        c = null;
                    }
                }

                // Merge Payment methods
                // No clean way to merge (since auto-increment is not guaranteed to have any order and there isn't
                // enough outlying data) => Always overwirte
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Merging Payment Methods");
                }
                mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Payment Methods");
                try {
                    c = importDB.query(PaymentMethodsTable.TABLE_NAME, null, null, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        currDB.delete(PaymentMethodsTable.TABLE_NAME, null, null); // DELETE * FROM PaymentMethodsTable
                        final int idxIndex = c.getColumnIndex(PaymentMethodsTable.COLUMN_ID);
                        final int typeIndex = c.getColumnIndex(PaymentMethodsTable.COLUMN_METHOD);
                        do {
                            final int index = getInt(c, idxIndex, 0);
                            final String type = getString(c, typeIndex, "");
                            ContentValues values = new ContentValues(2);
                            values.put(PaymentMethodsTable.COLUMN_ID, index);
                            values.put(PaymentMethodsTable.COLUMN_METHOD, type);
                            currDB.insert(PaymentMethodsTable.TABLE_NAME, null, values);
                        }
                        while (c.moveToNext());
                    } else {
                        return false;
                    }
                } catch (SQLiteException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.toString(), e); // Occurs if Table does not exist
                    }
                    mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Caught sql exception during import at [a6]: " + Utils.getStackTrace(e));
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                        c = null;
                    }
                }

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Merging Distance");
                }
                mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Distance");
                try {
                    c = importDB.query(DistanceTable.TABLE_NAME, null, null, null, null, null, null);
                    if (c != null && c.moveToFirst()) {
                        final String distanceCountQuery = "SELECT COUNT(*), " + DistanceTable.COLUMN_ID + " FROM " + DistanceTable.TABLE_NAME + " WHERE " + DistanceTable.COLUMN_PARENT + "=? AND " + DistanceTable.COLUMN_LOCATION + "=? AND " + DistanceTable.COLUMN_DATE + "=?";
                        final int parentTripIndex = c.getColumnIndex(DistanceTable.COLUMN_PARENT);
                        final int locationIndex = c.getColumnIndex(DistanceTable.COLUMN_LOCATION);
                        final int distanceIndex = c.getColumnIndex(DistanceTable.COLUMN_DISTANCE);
                        final int rateIndex = c.getColumnIndex(DistanceTable.COLUMN_RATE);
                        final int currencyIndex = c.getColumnIndex(DistanceTable.COLUMN_RATE_CURRENCY);
                        final int dateIndex = c.getColumnIndex(DistanceTable.COLUMN_DATE);
                        final int timezoneIndex = c.getColumnIndex(DistanceTable.COLUMN_TIMEZONE);
                        final int commentIndex = c.getColumnIndex(DistanceTable.COLUMN_COMMENT);
                        do {
                            final ContentValues values = new ContentValues(8);
                            final String parentTripPath = getString(c, parentTripIndex, "");
                            final String location = getString(c, locationIndex, "");
                            final BigDecimal distance = getDecimal(c, distanceIndex);
                            final BigDecimal rate = getDecimal(c, rateIndex);
                            final String currency = getString(c, currencyIndex, mPersistenceManager.getPreferences().getDefaultCurreny());
                            final long date = getLong(c, dateIndex, 0L);
                            final String timezone = getString(c, timezoneIndex, TimeZone.getDefault().getID());
                            final String comment = getString(c, commentIndex, "");
                            values.put(DistanceTable.COLUMN_PARENT, parentTripPath);
                            values.put(DistanceTable.COLUMN_LOCATION, location);
                            values.put(DistanceTable.COLUMN_DISTANCE, distance.doubleValue());
                            values.put(DistanceTable.COLUMN_RATE, rate.doubleValue());
                            values.put(DistanceTable.COLUMN_RATE_CURRENCY, currency);
                            values.put(DistanceTable.COLUMN_DATE, date);
                            values.put(DistanceTable.COLUMN_TIMEZONE, timezone);
                            values.put(DistanceTable.COLUMN_COMMENT, comment);
                            try {
                                countCursor = currDB.rawQuery(distanceCountQuery, new String[]{parentTripPath, location, Long.toString(date)});
                                if (countCursor != null && countCursor.moveToFirst()) {
                                    int count = countCursor.getInt(0);
                                    int updateID = countCursor.getInt(1);
                                    if (count > 0 && overwrite) { // Update
                                        currDB.update(DistanceTable.TABLE_NAME, values, DistanceTable.COLUMN_ID + " = ?", new String[]{Integer.toString(updateID)});
                                    } else { // insert
                                        if (overwrite) {
                                            currDB.insertWithOnConflict(DistanceTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                                        } else if (count == 0) {
                                            // If we're not overwriting anything, let's check that there are no entries here
                                            currDB.insertWithOnConflict(DistanceTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                                        }
                                    }
                                }
                            } finally {
                                if (countCursor != null && !countCursor.isClosed()) {
                                    countCursor.close();
                                    countCursor = null;
                                }
                            }
                        }
                        while (c.moveToNext());
                    } else {
                        return false;
                    }
                } catch (SQLiteException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.toString(), e); // Occurs if Table does not exist
                    }
                    mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Caught sql exception during import at [a6]: " + Utils.getStackTrace(e));
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                        c = null;
                    }
                }

                mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Success");
                return true;
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString());
                }
                mPersistenceManager.getStorageManager().appendTo(ImportTask.LOG_FILE, "Caught fatal db exception during import at [a7]: " + Utils.getStackTrace(e));
                return false;
            } finally {
                if (c != null && !c.isClosed()) {
                    c.close();
                }
                if (countCursor != null && !countCursor.isClosed()) {
                    countCursor.close();
                }
                if (importDB != null) {
                    importDB.close();
                }
            }
        }
    }

    private boolean getBoolean(Cursor cursor, int index, boolean defaultValue) {
        if (index >= 0) {
            return (cursor.getInt(index) > 0);
        } else {
            return defaultValue;
        }
    }

    private int getInt(Cursor cursor, int index, int defaultValue) {
        if (index >= 0) {
            return cursor.getInt(index);
        } else {
            return defaultValue;
        }
    }

    private long getLong(Cursor cursor, int index, long defaultValue) {
        if (index >= 0) {
            return cursor.getLong(index);
        } else {
            return defaultValue;
        }
    }

    private double getDouble(Cursor cursor, int index, double defaultValue) {
        if (index >= 0) {
            return cursor.getDouble(index);
        } else {
            return defaultValue;
        }
    }

    private String getString(Cursor cursor, int index, String defaultValue) {
        if (index >= 0) {
            return cursor.getString(index);
        } else {
            return defaultValue;
        }
    }

    /**
     * Please note that a very frustrating bug exists here. Android cursors only return the first 6
     * characters of a price string if that string contains a '.' character. It returns all of them
     * if not. This means we'll break for prices over 5 digits unless we are using a comma separator,
     * which we'd do in the EU. In the EU (comma separated), Android returns the wrong value when we
     * get a double (instead of a string). This method has been built to handle this edge case to the
     * best of our abilities.
     * <p/>
     * TODO: Longer term, everything should be saved with a decimal point
     *
     * @param cursor - the current {@link android.database.Cursor}
     * @param index  - the index of the column
     * @return a {@link java.math.BigDecimal} value of the decimal
     * @see https://code.google.com/p/android/issues/detail?id=22219.
     */
    private BigDecimal getDecimal(@NonNull Cursor cursor, int index) {
        if (index >= 0) {
            final String decimalString = cursor.getString(index);
            final double decimalDouble = cursor.getDouble(index);
            if (!TextUtils.isEmpty(decimalString) && decimalString.contains(",")) {
                try {
                    return new BigDecimal(decimalString.replace(",", "."));
                } catch (NumberFormatException e) {
                    return new BigDecimal(decimalDouble);
                }
            } else {
                return new BigDecimal(decimalDouble);
            }
        } else {
            return new BigDecimal(0);
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // AutoCompleteTextView Methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Cursor getAutoCompleteCursor(CharSequence text, CharSequence tag) {
        // TODO: Fix SQL vulnerabilities
        final SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "";
        if (tag == TAG_RECEIPTS_NAME) {
            sqlQuery = " SELECT DISTINCT TRIM(" + ReceiptsTable.COLUMN_NAME + ") AS _id " + " FROM " + ReceiptsTable.TABLE_NAME + " WHERE " + ReceiptsTable.COLUMN_NAME + " LIKE '%" + text + "%' " + " ORDER BY " + ReceiptsTable.COLUMN_NAME;
        } else if (tag == TAG_RECEIPTS_COMMENT) {
            sqlQuery = " SELECT DISTINCT TRIM(" + ReceiptsTable.COLUMN_COMMENT + ") AS _id " + " FROM " + ReceiptsTable.TABLE_NAME + " WHERE " + ReceiptsTable.COLUMN_COMMENT + " LIKE '%" + text + "%' " + " ORDER BY " + ReceiptsTable.COLUMN_COMMENT;
        } else if (tag == TAG_TRIPS_NAME) {
            sqlQuery = " SELECT DISTINCT TRIM(" + TripsTable.COLUMN_NAME + ") AS _id " + " FROM " + TripsTable.TABLE_NAME + " WHERE " + TripsTable.COLUMN_NAME + " LIKE '%" + text + "%' " + " ORDER BY " + TripsTable.COLUMN_NAME;
        } else if (tag == TAG_TRIPS_COST_CENTER) {
            sqlQuery = " SELECT DISTINCT TRIM(" + TripsTable.COLUMN_COST_CENTER + ") AS _id " + " FROM " + TripsTable.TABLE_NAME + " WHERE " + TripsTable.COLUMN_COST_CENTER + " LIKE '%" + text + "%' " + " ORDER BY " + TripsTable.COLUMN_COST_CENTER;
        } else if (tag == TAG_DISTANCE_LOCATION) {
            sqlQuery = " SELECT DISTINCT TRIM(" + DistanceTable.COLUMN_LOCATION + ") AS _id " + " FROM " + DistanceTable.TABLE_NAME + " WHERE " + DistanceTable.COLUMN_LOCATION + " LIKE '%" + text + "%' " + " ORDER BY " + DistanceTable.COLUMN_LOCATION;
        }
        synchronized (mDatabaseLock) {
            return db.rawQuery(sqlQuery, null);
        }
    }

    @Override
    public void onItemSelected(CharSequence text, CharSequence tag) {
        // TODO: Make Async

        Cursor c = null;
        SQLiteDatabase db = null;
        final String name = text.toString();
        if (tag == TAG_RECEIPTS_NAME) {
            String category = null, price = null, tmp = null;
            // If we're not predicting, return
            if (!mPersistenceManager.getPreferences().predictCategories()) {
                // price = null;
                // category = null
            } else {
                synchronized (mDatabaseLock) {
                    try {
                        db = this.getReadableDatabase();
                        c = db.query(ReceiptsTable.TABLE_NAME, new String[]{ReceiptsTable.COLUMN_CATEGORY, ReceiptsTable.COLUMN_PRICE}, ReceiptsTable.COLUMN_NAME + "= ?", new String[]{name}, null, null, ReceiptsTable.COLUMN_DATE + " DESC", "2");
                        if (c != null && c.getCount() == 2) {
                            if (c.moveToFirst()) {
                                category = c.getString(0);
                                price = c.getString(1);
                                if (c.moveToNext()) {
                                    tmp = c.getString(0);
                                    if (!category.equalsIgnoreCase(tmp)) {
                                        category = null;
                                    }
                                    tmp = c.getString(1);
                                    if (!price.equalsIgnoreCase(tmp)) {
                                        price = null;
                                    }
                                }
                            }
                        }
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }
                }
            }
            if (mReceiptRowListener != null) {
                mReceiptRowListener.onReceiptRowAutoCompleteQueryResult(name, price, category);
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////
    // Support Methods
    // //////////////////////////////////////////////////////////////////////////////////////////////////
    public void backUpDatabase(final String databasePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final StorageManager storageManager = mPersistenceManager.getStorageManager();
                File sdDB = storageManager.getFile(DateUtils.getCurrentDateAsYYYY_MM_DDString() + "_" + DATABASE_NAME + ".bak");
                try {
                    synchronized (mDatabaseLock) {
                        storageManager.copy(new File(databasePath), sdDB, true);
                    }
                    if (D) {
                        Log.d(TAG, "Backed up database file to: " + sdDB.getName());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failed to back up database: " + e.toString());
                } catch (Exception e) {
                    Log.e(TAG, "Failed to back up database: " + e.toString());
                    // Avoid crashing on an exception here... Just a backup utility anyway
                }
            }
        }).start();
    }
}
