package wb.receiptslibrary.persistence;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import wb.android.autocomplete.AutoCompleteQueriable;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;
import wb.receiptslibrary.BuildConfig;
import wb.receiptslibrary.CSVColumn;
import wb.receiptslibrary.CSVColumns;
import wb.receiptslibrary.SmartReceiptsActivity;
import wb.receiptslibrary.model.ReceiptRow;
import wb.receiptslibrary.model.TripRow;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

public final class DatabaseHelper extends SQLiteOpenHelper implements AutoCompleteQueriable {

	//Logging Vars
	private static final boolean D = true;
	private static final String TAG = "DatabaseHelper";
	
	//Database Info
	public static final String DATABASE_NAME = "receipts.db";
	private static final int DATABASE_VERSION = 7;
	public static final String NO_DATA = "null";
	static final String MULTI_CURRENCY = "XXXXXX";
	
	//Tags
	public static final String TAG_TRIPS = "Trips";
	public static final String TAG_RECEIPTS = "Receipts";
	
	//InstanceVar
	private static DatabaseHelper INSTANCE = null;
	
	//Caching Vars
	private TripRow[] _tripsCache;
	private boolean _areTripsValid;
	private HashMap<TripRow, ReceiptRow[]> _receiptMapCache;
	private HashMap<String, String> _categories;
	private ArrayList<CharSequence> _categoryList, _currencyList;
	private CSVColumns _csvColumns;
	private Time _now;
	private SmartReceiptsActivity _activity;
	
	//Listeners
	private TripRowListener mTripRowListener;
	private ReceiptRowListener mReceiptRowListener;
	
	//Locks
	private Object mDatabaseLock = new Object();
	private Object mReceiptCacheLock = new Object();
	private Object mTripCacheLock = new Object();
	
	//Hack to prevent Recursive Database Calling
	private SQLiteDatabase _initDB; //This is only set while either onCreate or onUpdate is running. It is null all other times
	
	public interface TripRowListener {
		public void onTripRowsQuerySuccess(TripRow[] trips);
		public void onTripRowInsertSuccess(TripRow trip);
		public void onTripRowInsertFailure(SQLException ex, File directory); //Directory here is out of mDate
		public void onTripRowUpdateSuccess(TripRow trip);
		public void onTripRowUpdateFailure(TripRow oldTrip, File directory); //For rollback info
		public void onTripDeleteSuccess(TripRow oldTrip);
		public void onTripDeleteFailure();
	}
	
	public interface ReceiptRowListener {
		public void onReceiptRowsQuerySuccess(ReceiptRow[] receipts);
		public void onReceiptRowInsertSuccess(ReceiptRow receipt);
		public void onReceiptRowInsertFailure(SQLException ex); //Directory here is out of mDate
		public void onReceiptRowUpdateSuccess(ReceiptRow receipt);
		public void onReceiptRowUpdateFailure(); //For rollback info
		public void onReceiptDeleteSuccess(ReceiptRow receipt);
		public void onReceiptRowAutoCompleteQueryResult(String name, String price, String category); //Any of these can be null!
		public void onReceiptDeleteFailure();
	}
	
	//Tables Declarations
	private static final class TripsTable {
		private TripsTable() {}	
		public static final String TABLE_NAME = "trips";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_FROM = "from_date";
		public static final String COLUMN_TO = "to_date";
		public static final String COLUMN_PRICE = "price";
		public static final String COLUMN_MILEAGE = "miles_new";
	}
	private static final class ReceiptsTable {
	private ReceiptsTable() {}
		public static final String TABLE_NAME = "receipts";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_PATH = "path";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_PARENT = "parent";
		public static final String COLUMN_CATEGORY = "category";
		public static final String COLUMN_PRICE = "price";
		public static final String COLUMN_TAX = "tax";
		public static final String COLUMN_PAYMENTMETHOD = "paymentmethod";
		public static final String COLUMN_DATE = "rcpt_date";
		public static final String COLUMN_COMMENT = "comment";
		public static final String COLUMN_EXPENSEABLE = "expenseable";
		public static final String COLUMN_ISO4217 = "isocode";
		public static final String COLUMN_NOTFULLPAGEIMAGE = "fullpageimage";
		public static final String COLUMN_EXTRA_EDITTEXT_1 = "extra_edittext_1";
		public static final String COLUMN_EXTRA_EDITTEXT_2 = "extra_edittext_2";
		public static final String COLUMN_EXTRA_EDITTEXT_3 = "extra_edittext_3";
	}
	private static final class CategoriesTable {
		private CategoriesTable() {}
		public static final String TABLE_NAME = "categories";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_CODE = "code";
		public static final String COLUMN_BREAKDOWN = "breakdown";
	}
	public static final class CSVTable {
		private CSVTable() {}
		public static final String TABLE_NAME = "csvcolumns";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_TYPE = "type";
	}
	
	private DatabaseHelper(final SmartReceiptsActivity activity, String databasePath) {
		super(activity, databasePath, null, DATABASE_VERSION); //Requests the default cursor factory
		_areTripsValid = false;
		_receiptMapCache = new HashMap<TripRow, ReceiptRow[]>();
		_activity = activity;
		this.getReadableDatabase(); //Called here, so onCreate gets called on the UI thread
	}
	
	public static final DatabaseHelper getInstance(final SmartReceiptsActivity activity) {		
		if (INSTANCE == null) {
			String databasePath = StorageManager.GetRootPath();
			if (BuildConfig.DEBUG) {
				if (databasePath.equals("")) {
					throw new RuntimeException("The SDCard must be created beforoe GetRootPath is called in DBHelper");
				}
			}
			if (!databasePath.endsWith(File.separator))
				databasePath = databasePath + File.separator;
			databasePath = databasePath + DATABASE_NAME;
			INSTANCE = new DatabaseHelper(activity, databasePath);
		}
		return INSTANCE;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//	Begin Abstract Method Overrides
	////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onCreate(final SQLiteDatabase db) {
		synchronized (mDatabaseLock) {
			_initDB = db;
			//N.B. This only gets called if you actually request the database using the getDatabase method
			final String trips = "CREATE TABLE " + TripsTable.TABLE_NAME + " ("
					+ TripsTable.COLUMN_NAME + " TEXT PRIMARY KEY, "
					+ TripsTable.COLUMN_FROM + " DATE, "
					+ TripsTable.COLUMN_TO + " DATE, "
					+ TripsTable.COLUMN_PRICE + " DECIMAL(10, 2) DEFAULT 0.00, "
					+ TripsTable.COLUMN_MILEAGE + " DECIMAL(10, 2) DEFAULT 0.00"
					+ ");";
			final String receipts = "CREATE TABLE " + ReceiptsTable.TABLE_NAME + " ("
					+ ReceiptsTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ ReceiptsTable.COLUMN_PATH + " TEXT, "
					+ ReceiptsTable.COLUMN_PARENT + " TEXT REFERENCES " + TripsTable.TABLE_NAME + " ON DELETE CASCADE, "
					+ ReceiptsTable.COLUMN_NAME + " TEXT DEFAULT \"New Receipt\", "
					+ ReceiptsTable.COLUMN_CATEGORY + " TEXT, "
					+ ReceiptsTable.COLUMN_DATE + " DATE DEFAULT (DATE('now', 'localtime')), "
					+ ReceiptsTable.COLUMN_COMMENT + " TEXT, "
					+ ReceiptsTable.COLUMN_ISO4217 + " TEXT NOT NULL, "
					+ ReceiptsTable.COLUMN_PRICE + " DECIMAL(10, 2) DEFAULT 0.00, "
					+ ReceiptsTable.COLUMN_TAX + " DECIMAL(10, 2) DEFAULT 0.00, "
					+ ReceiptsTable.COLUMN_PAYMENTMETHOD + " TEXT, "
					+ ReceiptsTable.COLUMN_EXPENSEABLE + " BOOLEAN DEFAULT 1, "
					+ ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE + " BOOLEAN DEFAULT 1, "
					+ ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1 + " TEXT, "
					+ ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2 + " TEXT, "
					+ ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3 + " TEXT"
					+ ");";
			final String categories = "CREATE TABLE " + CategoriesTable.TABLE_NAME + " ("
					+ CategoriesTable.COLUMN_NAME + " TEXT PRIMARY KEY, "
					+ CategoriesTable.COLUMN_CODE + " TEXT, "
					+ CategoriesTable.COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1"
					+ ");";
			if (BuildConfig.DEBUG) Log.d(TAG, trips);
			if (BuildConfig.DEBUG) Log.d(TAG, receipts);
			if (BuildConfig.DEBUG) Log.d(TAG, categories);
			db.execSQL(trips);
			db.execSQL(receipts);
			db.execSQL(categories);
			this.createCSVTable(db);
			_activity.insertCategoryDefaults(this);
			_activity.onFirstRun();
			_initDB = null;
		}
	}
	
	@Override
	public final void onUpgrade(final SQLiteDatabase db, int oldVersion, final int newVersion) {
		synchronized (mDatabaseLock) {
			
			if(D) Log.d(TAG, "Upgrading the database from version " + oldVersion + " to " + newVersion);
			
			//Try to backup the database to the SD Card for support reasons
			final StorageManager storageManager = _activity.getPersistenceManager().getStorageManager();
			File sdDB = storageManager.getFile(DATABASE_NAME + "." + oldVersion + ".bak");
			try {
				storageManager.copy(new File(db.getPath()), sdDB, true);
				if(D) Log.d(TAG, "Backed up database file to: " + sdDB.getName());
			}
			catch (IOException e) {
				Log.e(TAG, "Failed to back up database: " + e.toString());
			}
			
			_initDB = db;
			if (oldVersion == 1) { // Add mCurrency column to receipts table
				final String alterReceipts = "ALTER TABLE " + ReceiptsTable.TABLE_NAME 
						+ " ADD " + ReceiptsTable.COLUMN_ISO4217 + " TEXT NOT NULL "
						+ "DEFAULT " + _activity.getPersistenceManager().getPreferences().getDefaultCurreny();
				if (BuildConfig.DEBUG) Log.d(TAG, alterReceipts);
				db.execSQL(alterReceipts);
				oldVersion++;
			}
			if (oldVersion == 2) { // Add the mileage field to trips, add the breakdown boolean to categories, and create the CSV table 
				final String alterCategories = "ALTER TABLE " + CategoriesTable.TABLE_NAME 
						+ " ADD " + CategoriesTable.COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1";
				if(D) Log.d(TAG, alterCategories);
				db.execSQL(alterCategories);
				this.createCSVTable(db);
				oldVersion++;
			}
			if (oldVersion == 3) { // Add extra_edittext columns
				final String alterReceipts1 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME
						+ " ADD " + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1 + " TEXT";
				final String alterReceipts2 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME
						+ " ADD " + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2 + " TEXT";
				final String alterReceipts3 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME
						+ " ADD " + ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3 + " TEXT";
				if (BuildConfig.DEBUG) Log.d(TAG, alterReceipts1);
				if (BuildConfig.DEBUG) Log.d(TAG, alterReceipts2);
				if (BuildConfig.DEBUG) Log.d(TAG, alterReceipts3);
				db.execSQL(alterReceipts1);
				db.execSQL(alterReceipts2);
				db.execSQL(alterReceipts3);
				oldVersion++;
			}
			if (oldVersion == 4) { //Change Mileage to Decimal instead of Integer
				final String alterMiles = "ALTER TABLE " + TripsTable.TABLE_NAME 
						+ " ADD " + TripsTable.COLUMN_MILEAGE + " DECIMAL(10, 2) DEFAULT 0.00";
				final String alterReceipts1 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME
						+ " ADD " + ReceiptsTable.COLUMN_TAX + " DECIMAL(10, 2) DEFAULT 0.00";
				final String alterReceipts2 = "ALTER TABLE " + ReceiptsTable.TABLE_NAME
						+ " ADD " + ReceiptsTable.COLUMN_PAYMENTMETHOD + " TEXT";
				if (BuildConfig.DEBUG) Log.d(TAG, alterMiles);
				if (BuildConfig.DEBUG) Log.d(TAG, alterReceipts1);
				if (BuildConfig.DEBUG) Log.d(TAG, alterReceipts2);
				db.execSQL(alterMiles);
				db.execSQL(alterReceipts1);
				db.execSQL(alterReceipts2);
				oldVersion++;
			}
			if (oldVersion == 5) {
				oldVersion++; //Skipped b/c I forgot to include the update stuff
			}
			if (oldVersion == 6) { //Fix the database to replace absolute paths with relative ones
				/**
				 * TODO: Add a test to remove a trailing slash => /data/android/<name>/
				 */
				final Cursor tripsCursor = db.query(TripsTable.TABLE_NAME, new String[] {TripsTable.COLUMN_NAME}, null, null, null, null, null);
				if (tripsCursor != null && tripsCursor.moveToFirst()) {
					final int nameIndex = tripsCursor.getColumnIndex(TripsTable.COLUMN_NAME);
					do {
						String absPath = tripsCursor.getString(nameIndex);
						if (absPath.endsWith(File.separator))
							absPath = absPath.substring(0, absPath.length()-1);
						final String relPath = absPath.substring(absPath.lastIndexOf(File.separatorChar) + 1, absPath.length());
						if (BuildConfig.DEBUG) Log.d(TAG, "Updating Abs. Trip Path: " + absPath + " => " + relPath);
						final ContentValues tripValues = new ContentValues(1);
						tripValues.put(TripsTable.COLUMN_NAME, relPath);
						if (db.update(TripsTable.TABLE_NAME, tripValues, TripsTable.COLUMN_NAME + " = ?", new String[] {absPath}) == 0) {
							if (BuildConfig.DEBUG) Log.e(TAG, "Trip Update Error Occured");
						}
					} 
					while (tripsCursor.moveToNext());
				}
				//TODO: Finally clause here
				tripsCursor.close();
				
				final Cursor receiptsCursor = db.query(ReceiptsTable.TABLE_NAME, new String[] {ReceiptsTable.COLUMN_ID, ReceiptsTable.COLUMN_PARENT, ReceiptsTable.COLUMN_PATH}, null, null, null, null, null);
				if (receiptsCursor != null && receiptsCursor.moveToFirst()) {
					final int idIdx = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_ID);
					final int parentIdx = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
					final int imgIdx = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PATH);
					do {
						final int id = receiptsCursor.getInt(idIdx);
						String absParentPath = receiptsCursor.getString(parentIdx);
						if (absParentPath.endsWith(File.separator))
							absParentPath = absParentPath.substring(0, absParentPath.length()-1);
						final String absImgPath = receiptsCursor.getString(imgIdx);
						final ContentValues receiptValues  = new ContentValues(2);
						final String relParentPath = absParentPath.substring(absParentPath.lastIndexOf(File.separatorChar) + 1, absParentPath.length());
						receiptValues.put(ReceiptsTable.COLUMN_PARENT, relParentPath);
						if (BuildConfig.DEBUG) Log.d(TAG, "Updating Abs. Parent Path for Receipt" + id + ": " + absParentPath + " => " + relParentPath);;
						if (!absImgPath.equalsIgnoreCase(NO_DATA)) { //This can be either a path or NO_DATA
							final String relImgPath = absImgPath.substring(absImgPath.lastIndexOf(File.separatorChar) + 1, absImgPath.length());
							receiptValues.put(ReceiptsTable.COLUMN_PATH, relImgPath);
							if (BuildConfig.DEBUG) Log.d(TAG, "Updating Abs. Img Path for Receipt" + id + ": " + absImgPath + " => " + relImgPath);
						}
						if (db.update(ReceiptsTable.TABLE_NAME, receiptValues, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(id)}) == 0) {
							if (BuildConfig.DEBUG) Log.e(TAG, "Receipt Update Error Occured");
						}
					} 
					while (receiptsCursor.moveToNext());
				}
				receiptsCursor.close();
			}
			_initDB = null;
		}
	}
	
	/*
	public final void testPrintDBValues() {
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor tripsCursor = db.query(TripsTable.TABLE_NAME, new String[] {TripsTable.COLUMN_NAME}, null, null, null, null, null);
		String data = "";
		if (BuildConfig.DEBUG) Log.d(TAG, "=================== Printing Trips ===================");
		if (BuildConfig.DEBUG) data += "=================== Printing Trips ===================" + "\n";
		if (tripsCursor != null && tripsCursor.moveToFirst()) {
			final int nameIndex = tripsCursor.getColumnIndex(TripsTable.COLUMN_NAME);
			do {
				if (BuildConfig.DEBUG) Log.d(TAG, tripsCursor.getString(nameIndex));
				if (BuildConfig.DEBUG) data += "\"" + tripsCursor.getString(nameIndex) + "\"";
				
			} 
			while (tripsCursor.moveToNext());
		}
		
		final Cursor receiptsCursor = db.query(ReceiptsTable.TABLE_NAME, new String[] {ReceiptsTable.COLUMN_ID, ReceiptsTable.COLUMN_PARENT, ReceiptsTable.COLUMN_PATH}, null, null, null, null, null);
		if (BuildConfig.DEBUG) Log.d(TAG, "=================== Printing Receipts ===================");
		if (BuildConfig.DEBUG) data +=  "=================== Printing Receipts ===================" + "\n";
		if (receiptsCursor != null && receiptsCursor.moveToFirst()) {
			final int idIdx = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_ID);
			final int parentIdx = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
			final int imgIdx = receiptsCursor.getColumnIndex(ReceiptsTable.COLUMN_PATH);
			do {
				if (BuildConfig.DEBUG) Log.d(TAG, "(" + receiptsCursor.getInt(idIdx) + ", " + receiptsCursor.getString(parentIdx) + ", " + receiptsCursor.getString(imgIdx) + ")");
				if (BuildConfig.DEBUG) data += "(" + receiptsCursor.getInt(idIdx) + ", " + receiptsCursor.getString(parentIdx) + ", " + receiptsCursor.getString(imgIdx) + ")" + "\n";
			} 
			while (receiptsCursor.moveToNext());
		}
		_activity.getStorageManager().write("db.txt", data);
	}*/
	
	private final void createCSVTable(final SQLiteDatabase db) { //Called in onCreate and onUpgrade
		final String csv = "CREATE TABLE " + CSVTable.TABLE_NAME + " ("
				+ CSVTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CSVTable.COLUMN_TYPE + " TEXT"
				+ ");"; 
		if (BuildConfig.DEBUG) Log.d(TAG, csv);
		db.execSQL(csv);
		_activity.insertCSVDefaults(this);
	}
	
	void onDestroy() {
		synchronized (mDatabaseLock) {
			if (this.getReadableDatabase().isOpen()) {
				this.getReadableDatabase().close();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//	TripRow Methods
	////////////////////////////////////////////////////////////////////////////////////////////////////
	public void registerTripRowListener(TripRowListener listener) {
		mTripRowListener = listener;
	}
	
	public void unregisterTripRowListener(TripRowListener listener) {
		mTripRowListener = null;
	}
	
	public TripRow[] getTripsSerial() {
		synchronized (mTripCacheLock) {
			if (_areTripsValid)
				return _tripsCache;
		}
		TripRow[] trips = getTripsHelper();
		synchronized (mTripCacheLock) {
			if (!_areTripsValid) {
				_areTripsValid = true;
				_tripsCache = trips;
			}
			return _tripsCache;
		}
	}
	
	public void getTripsParallel() {
		if (mTripRowListener == null) {
			if (BuildConfig.DEBUG) Log.d(TAG, "No TripRowListener was registered.");
		}
		else {
			synchronized (mTripCacheLock) {
				if (_areTripsValid) {
					mTripRowListener.onTripRowsQuerySuccess(_tripsCache);
					return;
				}
			}
			(new GetTripsWorker()).execute(new Void[0]);
		}		
	}
	
	// TODO: Move all closes into finally statement
	private static final String CURR_CNT_QUERY = "SELECT COUNT(*), " + ReceiptsTable.COLUMN_ISO4217 + " FROM (SELECT COUNT(*), " + ReceiptsTable.COLUMN_ISO4217 + " FROM " + ReceiptsTable.TABLE_NAME + " WHERE " + ReceiptsTable.COLUMN_PARENT + "=? GROUP BY " + ReceiptsTable.COLUMN_ISO4217 + ");";
	private TripRow[] getTripsHelper() {
		SQLiteDatabase db = null;
		Cursor c = null;
		synchronized (mDatabaseLock) {
			try {
				db = this.getReadableDatabase();
				c = db.query(TripsTable.TABLE_NAME, null, null, null, null, null, TripsTable.COLUMN_TO + " DESC");
				if (c != null && c.moveToFirst()) {
					TripRow[] trips = new TripRow[c.getCount()];
					final int nameIndex = c.getColumnIndex(TripsTable.COLUMN_NAME);
					final int fromIndex = c.getColumnIndex(TripsTable.COLUMN_FROM);
					final int toIndex = c.getColumnIndex(TripsTable.COLUMN_TO);
					final int priceIndex = c.getColumnIndex(TripsTable.COLUMN_PRICE);
					final int milesIndex = c.getColumnIndex(TripsTable.COLUMN_MILEAGE);
					do {
						final String name = c.getString(nameIndex);
						final long from = c.getLong(fromIndex);
						final long to = c.getLong(toIndex);
						final String price = c.getString(priceIndex);
						final float miles = c.getFloat(milesIndex);
						final Cursor qc = db.rawQuery(CURR_CNT_QUERY, new String[]{name});
						int cnt; String curr = MULTI_CURRENCY;
						if (qc != null && qc.moveToFirst() && qc.getColumnCount() > 0) {
							cnt = qc.getInt(0);
							if (cnt == 1) curr = qc.getString(1);
							else if (cnt == 0) curr = _activity.getPersistenceManager().getPreferences().getDefaultCurreny();
						}
						TripRow.Builder builder = new TripRow.Builder();
						trips[c.getPosition()] = builder.setDirectory(_activity.getPersistenceManager().getStorageManager().getFile(name))
														.setStartDate(from)
														.setEndDate(to)
														.setPrice(price)
														.setCurrency(curr)
														.setMileage(miles)
														.setSourceAsCache()
														.build();
					} 
					while (c.moveToNext());
					return trips;
				}
				else {
					return new TripRow[0];
				}
			}
			finally { // Close the cursor and db to avoid memory leaks
				if (c != null) c.close(); 	
			}
		}
	}
	
	private class GetTripsWorker extends AsyncTask<Void, Void, TripRow[]> {

		@Override
		protected TripRow[] doInBackground(Void... params) {
			return getTripsHelper();
		}
		
		@Override
		protected void onPostExecute(TripRow[] result) {
			synchronized (mTripCacheLock) {
				_areTripsValid = true;
				_tripsCache = result;
			}
			if (mTripRowListener != null)
				mTripRowListener.onTripRowsQuerySuccess(result);
		}
		
	}
	
	public final TripRow getTripByName(final String name) {
		if (name == null || name.length() == 0)
			return null;
		synchronized (mTripCacheLock) {
			if (_areTripsValid) {
				for(int i=0; i < _tripsCache.length; i++) {
					if (_tripsCache[i].getName().equals(name))
						return _tripsCache[i];
				}
			}
		}
		SQLiteDatabase db = null;
		Cursor c = null, qc = null;
		synchronized (mDatabaseLock) {
			try {
				db = this.getReadableDatabase();
				c = db.query(TripsTable.TABLE_NAME, null, TripsTable.COLUMN_NAME + " = ?", new String[] {name}, null, null, null);
				if (c != null && c.moveToFirst()) {
					final int fromIndex = c.getColumnIndex(TripsTable.COLUMN_FROM);
					final int toIndex = c.getColumnIndex(TripsTable.COLUMN_TO);
					final int priceIndex = c.getColumnIndex(TripsTable.COLUMN_PRICE);
					final int milesIndex = c.getColumnIndex(TripsTable.COLUMN_MILEAGE);
					final long from = c.getLong(fromIndex);
					final long to = c.getLong(toIndex);
					final float miles = c.getFloat(milesIndex);
					final String price = c.getString(priceIndex);
					qc = db.rawQuery(CURR_CNT_QUERY, new String[]{name});
					int cnt; String curr = MULTI_CURRENCY;;
					if (qc != null && qc.moveToFirst() && qc.getColumnCount() > 0) {
						cnt = qc.getInt(0);
						if (cnt == 1) { 
							curr = qc.getString(1);
						}
						else if (cnt == 0) {
							curr = _activity.getPersistenceManager().getPreferences().getDefaultCurreny();
						}
					}
					TripRow.Builder builder = new TripRow.Builder();
					return builder.setDirectory(_activity.getPersistenceManager().getStorageManager().getFile(name))
								  .setStartDate(from)
								  .setEndDate(to)
								  .setPrice(price)
								  .setCurrency(curr)
								  .setMileage(miles)
								  .setSourceAsCache()
								  .build();
				}
				else {
					return null;
				}
			}
			finally { // Close the cursor and db to avoid memory leaks
				if (c != null) c.close();
				if (qc != null) qc.close(); 
			}
		}
	}
	
	//Returns the trip on success. Null otherwise
	public final TripRow insertTripSerial(final File dir, final Date from, final Date to) throws SQLException {
		TripRow trip = insertTripHelper(dir, from, to);
		if (trip != null) {
			synchronized (mTripCacheLock) {
				_areTripsValid = false;
			}
		}
		return trip;
	}
	
	public void insertTripParallel(final File dir, final Date from, final Date to) {
		if (mTripRowListener == null) {
			if (BuildConfig.DEBUG) Log.d(TAG, "No TripRowListener was registered.");
		}
		(new InsertTripRowWorker(dir, from, to)).execute(new Void[0]);
	}
	
	private TripRow insertTripHelper(final File dir, final Date from, final Date to) throws SQLException {
		ContentValues values = new ContentValues(3);
		values.put(TripsTable.COLUMN_NAME, dir.getName());
		values.put(TripsTable.COLUMN_FROM, from.getTime());
		values.put(TripsTable.COLUMN_TO, to.getTime());
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			db = this.getWritableDatabase();
			if (values == null || db.insertOrThrow(TripsTable.TABLE_NAME, null, values) == -1)
				return null;
			else {
				return (new TripRow.Builder()).setDirectory(dir)
									   		  .setStartDate(from)
									   		  .setEndDate(to)
									   		  .setCurrency(_activity.getPersistenceManager().getPreferences().getDefaultCurreny())
									   		  .setSourceAsCache()
									   		  .build();
			}
		}
	}
	
	private class InsertTripRowWorker extends AsyncTask<Void, Void, TripRow> {

		private final File mDir;
		private final Date mFrom, mTo;
		private SQLException mException;
		
		public InsertTripRowWorker(final File dir, final Date from, final Date to) {
			mDir = dir;
			mFrom = from;
			mTo = to;
			mException = null;
		}
		
		@Override
		protected TripRow doInBackground(Void... params) {
			try {
				return insertTripHelper(mDir, mFrom, mTo);
			}
			catch (SQLException ex) {
				mException = ex;
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(TripRow result) {
			if (result != null) {
				synchronized (mTripCacheLock) {
					_areTripsValid = false;
				}
				if (mTripRowListener != null) mTripRowListener.onTripRowInsertSuccess(result);
			}
			else {
				if (mTripRowListener != null) mTripRowListener.onTripRowInsertFailure(mException, mDir);
			}
		}
		
	}
	
	public final TripRow updateTripSerial(TripRow oldTrip, File dir, Date from, Date to) {
		TripRow trip = updateTripHelper(oldTrip, dir, from, to);
		if (trip != null) {
			synchronized (mTripCacheLock) {
				_areTripsValid = false;
			}
		}
		return trip;
	}
	
	public void updateTripParallel(TripRow oldTrip, File dir, Date from, Date to) {
		if (mTripRowListener == null) {
			if (BuildConfig.DEBUG) Log.d(TAG, "No TripRowListener was registered.");
		}
		(new UpdateTripRowWorker(oldTrip, dir, from, to)).execute(new Void[0]);
	}
	
	private TripRow updateTripHelper(TripRow oldTrip, File dir, Date from, Date to) {
		ContentValues values = new ContentValues(3);
		values.put(TripsTable.COLUMN_NAME, dir.getName());
		values.put(TripsTable.COLUMN_FROM, from.getTime());
		values.put(TripsTable.COLUMN_TO, to.getTime());
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			try {
				db = this.getWritableDatabase();
				if (values == null || (db.update(TripsTable.TABLE_NAME, values, TripsTable.COLUMN_NAME + " = ?", new String[] {oldTrip.getName()}) == 0))
					return null;
				else {
					if (!oldTrip.getName().equalsIgnoreCase(dir.getName())) {
						synchronized (mReceiptCacheLock) {
							if (_receiptMapCache.containsKey(oldTrip)) _receiptMapCache.remove(oldTrip);
						}
						String oldName = oldTrip.getName();
						String newName = dir.getName();
						ContentValues rcptVals = new ContentValues(1);
						rcptVals.put(ReceiptsTable.COLUMN_PARENT, newName);
						//Update parent
						db.update(ReceiptsTable.TABLE_NAME, rcptVals, ReceiptsTable.COLUMN_PARENT + " = ?", new String[] {oldName}); //Consider rollback here
					}
					return (new TripRow.Builder()).setDirectory(dir)
												  .setStartDate(from)
												  .setEndDate(to)
												  .setCurrency(oldTrip.getCurrency())
												  .setSourceAsCache()
												  .build();
				}
			}
			catch (SQLException e) {
				if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
				return null;
			}
		}
	}
	
	private class UpdateTripRowWorker extends AsyncTask<Void, Void, TripRow> {

		private final File mDir;
		private final Date mFrom, mTo;
		private final TripRow mOldTrip;
		
		public UpdateTripRowWorker(TripRow oldTrip, File dir, Date from, Date to) {
			mOldTrip = oldTrip;
			mDir = dir;
			mFrom = from;
			mTo = to;
		}
		
		@Override
		protected TripRow doInBackground(Void... params) {
			return updateTripHelper(mOldTrip, mDir, mFrom, mTo);
		}
		
		@Override
		protected void onPostExecute(TripRow result) {
			if (result != null) {
				synchronized (mTripCacheLock) {
					_areTripsValid = false;
				}
				if (mTripRowListener != null) mTripRowListener.onTripRowUpdateSuccess(result);
			}
			else {
				if (mTripRowListener != null) mTripRowListener.onTripRowUpdateFailure(mOldTrip, mDir);
			}
		}
		
	}
		
	public boolean deleteTripSerial(TripRow trip) {
		if (mTripRowListener == null) {
			if (BuildConfig.DEBUG) Log.d(TAG, "No TripRowListener was registered.");
		}
		boolean success = deleteTripHelper(trip);
		if (success) {
			synchronized (mTripCacheLock) {
				_areTripsValid = false;
			}
		}
		return success;
	}
	
	public void deleteTripParallel(TripRow trip) {
		(new DeleteTripRowWorker()).execute(trip);
	}
	
	private boolean deleteTripHelper(TripRow trip) {
		boolean success = false;
		SQLiteDatabase db = null;
		db = this.getWritableDatabase();
		//Delete all child receipts (technically ON DELETE CASCADE should handle this, but i'm not certain)
		synchronized (mDatabaseLock) {
			// TODO: Fix errors when the disk is not yet mounted
			success = (db.delete(ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_PARENT + " = ?", new String[] {trip.getName()}) >= 0);
		}
		if (success) {
			synchronized (mReceiptCacheLock) {
				_receiptMapCache.remove(trip);
			}
		}
		else {
			return false;
		}
		synchronized (mDatabaseLock) {
			success = (db.delete(TripsTable.TABLE_NAME, TripsTable.COLUMN_NAME + " = ?", new String[] {trip.getName()}) > 0);
		}
		return success;
	}
	
	private class DeleteTripRowWorker extends AsyncTask<TripRow, Void, Boolean> {

		private TripRow mOldTrip;
		
		@Override
		protected Boolean doInBackground(TripRow... params) {
			if (params == null || params.length == 0)
				return false;
			mOldTrip = params[0];
			return deleteTripHelper(mOldTrip);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				synchronized (mTripCacheLock) {
					_areTripsValid = false;
				}
			}
			if (mTripRowListener != null) {
				if (result) mTripRowListener.onTripDeleteSuccess(mOldTrip);
				else mTripRowListener.onTripDeleteFailure();
			}
		}
		
	}
	
	public final boolean addMiles(final TripRow trip, final String current, final String delta) {
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
			final float currentMiles = Float.parseFloat(current);
			final float deltaMiles = Float.parseFloat(delta);
			float total = currentMiles + deltaMiles;
			if (total < 0)
				total = 0;
			ContentValues values = new ContentValues(1);
			values.put(TripsTable.COLUMN_MILEAGE, total);
			trip.setMileage(total);
			return (db.update(TripsTable.TABLE_NAME, values, TripsTable.COLUMN_NAME + " = ?", new String[] {trip.getName()}) > 0);
		}
		catch (NumberFormatException e) {
			return false;
		}
	}
	
	private final void updateTripPrice(final TripRow trip) {
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			Cursor c = null;
			try {
				_areTripsValid = false;
				db = this.getReadableDatabase();
				c = db.query(ReceiptsTable.TABLE_NAME, new String[] {"SUM(" + ReceiptsTable.COLUMN_PRICE + ")"}, 
						ReceiptsTable.COLUMN_PARENT + "= ? AND " + ReceiptsTable.COLUMN_EXPENSEABLE + " = 1", new String[] {trip.getName()}, null, null, null);
				if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
					final String sum = c.getString(0);
					trip.setPrice(sum);
					ContentValues values = new ContentValues(1);
					values.put(TripsTable.COLUMN_PRICE, sum);
					db.update(TripsTable.TABLE_NAME, values, TripsTable.COLUMN_NAME + " = ?", new String[] {trip.getName()});
				}
			}
			finally { // Close the cursor and db to avoid memory leaks
				if (c != null) c.close(); 
			}
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
	
	public ReceiptRow[] getReceiptsSerial(final TripRow trip) {
		synchronized (mReceiptCacheLock) {
			if (_receiptMapCache.containsKey(trip)) //only cache the default way (otherwise we get into issues with asc v desc)
				return _receiptMapCache.get(trip);
		}
		return this.getReceiptsHelper(trip, true);
	}
	
	public ReceiptRow[] getReceiptsSerial(final TripRow trip, final boolean desc) { //Only the email writer should use this
		return getReceiptsHelper(trip, desc);
	}
	
	public void getReceiptsParallel(final TripRow trip) {
		if (mReceiptRowListener == null) {
			if (BuildConfig.DEBUG) Log.d(TAG, "No ReceiptRowListener was registered.");
		}
		synchronized (mReceiptCacheLock) {
			if (_receiptMapCache.containsKey(trip)) { //only cache the default way (otherwise we get into issues with asc v desc)
				if (mReceiptRowListener != null)
					mReceiptRowListener.onReceiptRowsQuerySuccess(_receiptMapCache.get(trip));
				return;
			}
		}
		(new GetReceiptsWorker()).execute(trip);
	}
	
	private final ReceiptRow[] getReceiptsHelper(final TripRow trip, final boolean desc) {
		ReceiptRow[] receipts;
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			Cursor c = null;
			try {
				db = this.getReadableDatabase();
				c = db.query(ReceiptsTable.TABLE_NAME, null, 
						ReceiptsTable.COLUMN_PARENT + "= ?", new String[] {trip.getName()}, null, null, ReceiptsTable.COLUMN_DATE + ((desc)?" DESC":" ASC"));
				if (c != null && c.moveToFirst()) {
					receipts = new ReceiptRow[c.getCount()];
					final int idIndex = c.getColumnIndex(ReceiptsTable.COLUMN_ID);
					final int pathIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PATH);
					final int nameIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NAME);
					final int categoryIndex = c.getColumnIndex(ReceiptsTable.COLUMN_CATEGORY);
					final int priceIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PRICE);
					final int taxIndex = c.getColumnIndex(ReceiptsTable.COLUMN_TAX);
					final int dateIndex = c.getColumnIndex(ReceiptsTable.COLUMN_DATE);
					final int commentIndex = c.getColumnIndex(ReceiptsTable.COLUMN_COMMENT);
					final int expenseableIndex = c.getColumnIndex(ReceiptsTable.COLUMN_EXPENSEABLE);
					final int currencyIndex = c.getColumnIndex(ReceiptsTable.COLUMN_ISO4217);
					final int fullpageIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE);
					final int extra_edittext_1_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1);
					final int extra_edittext_2_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2);
					final int extra_edittext_3_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3);
					do {
						final int id = c.getInt(idIndex);
						final String path = c.getString(pathIndex);
						final String name = c.getString(nameIndex);
						final String category = c.getString(categoryIndex);
						final String price = c.getString(priceIndex);
						final String tax = c.getString(taxIndex);
						final long date = c.getLong(dateIndex);
						final String comment = c.getString(commentIndex);
						final boolean expensable = c.getInt(expenseableIndex)>0;
						final String currency = c.getString(currencyIndex);
						final boolean fullpage = !(c.getInt(fullpageIndex)>0);
						final String extra_edittext_1 = c.getString(extra_edittext_1_Index);
						final String extra_edittext_2 = c.getString(extra_edittext_2_Index);
						final String extra_edittext_3 = c.getString(extra_edittext_3_Index);
						File img = null;
						if (!path.equalsIgnoreCase(DatabaseHelper.NO_DATA))
							img = _activity.getPersistenceManager().getStorageManager().getFile(trip.getDirectory(), path);
						ReceiptRow.Builder builder = new ReceiptRow.Builder(id);
						receipts[c.getPosition()] =  builder.setName(name)
															.setCategory(category)
															.setImage(img)
															.setDate(date)
															.setComment(comment)
															.setPrice(price)
															.setTax(tax)
															.setIsExpenseable(expensable)
															.setCurrency(currency)
															.setIsFullPage(fullpage)
															.setExtraEditText1(extra_edittext_1)
															.setExtraEditText2(extra_edittext_2)
															.setExtraEditText3(extra_edittext_3)
															.build();
					} 
					while (c.moveToNext());
				}
				else {
					receipts = new ReceiptRow[0];
				}
			}
			finally { // Close the cursor and db to avoid memory leaks
				if (c != null) c.close(); 
			}
		}
		synchronized (mReceiptCacheLock) {
			if (desc) //Don't Cache the EmailWriterVariety
				_receiptMapCache.put(trip, receipts);
		}
		return receipts;
	}
	
	private class GetReceiptsWorker extends AsyncTask<TripRow, Void, ReceiptRow[]> {

		@Override
		protected ReceiptRow[] doInBackground(TripRow... params) {
			if (params == null || params.length == 0)
				return new ReceiptRow[0];
			TripRow trip = params[0];
			return getReceiptsHelper(trip, true);
		}
		
		@Override
		protected void onPostExecute(ReceiptRow[] result) {
			if (mReceiptRowListener != null)
				mReceiptRowListener.onReceiptRowsQuerySuccess(result);
		}
		
	}
	
	public final ReceiptRow getReceiptByID(final int id) {
		if (id <= 0)
			return null;
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			Cursor c = null;
			try {
				db = this.getReadableDatabase();
				c = db.query(ReceiptsTable.TABLE_NAME, null, ReceiptsTable.COLUMN_ID + "= ?", new String[] {Integer.toString(id)}, null, null, null);
				if (c != null && c.moveToFirst()) {
					final int pathIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PATH);
					final int parentIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PARENT);
					final int nameIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NAME);
					final int categoryIndex = c.getColumnIndex(ReceiptsTable.COLUMN_CATEGORY);
					final int priceIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PRICE);
					final int taxIndex = c.getColumnIndex(ReceiptsTable.COLUMN_TAX);
					final int dateIndex = c.getColumnIndex(ReceiptsTable.COLUMN_DATE);
					final int commentIndex = c.getColumnIndex(ReceiptsTable.COLUMN_COMMENT);
					final int expenseableIndex = c.getColumnIndex(ReceiptsTable.COLUMN_EXPENSEABLE);
					final int currencyIndex = c.getColumnIndex(ReceiptsTable.COLUMN_ISO4217);
					final int fullpageIndex = c.getColumnIndex(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE);
					final int extra_edittext_1_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1);
					final int extra_edittext_2_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2);
					final int extra_edittext_3_Index = c.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3);
					final String path = c.getString(pathIndex);
					final String parent = c.getString(parentIndex);
					final String name = c.getString(nameIndex);
					final String category = c.getString(categoryIndex);
					final String price = c.getString(priceIndex);
					final String tax = c.getString(taxIndex);
					final long date = c.getLong(dateIndex);
					final String comment = c.getString(commentIndex);
					final boolean expensable = c.getInt(expenseableIndex)>0;
					final String currency = c.getString(currencyIndex);
					final boolean fullpage = !(c.getInt(fullpageIndex)>0);
					final String extra_edittext_1 = c.getString(extra_edittext_1_Index);
					final String extra_edittext_2 = c.getString(extra_edittext_2_Index);
					final String extra_edittext_3 = c.getString(extra_edittext_3_Index);
					File img = null;
					if (!path.equalsIgnoreCase(DatabaseHelper.NO_DATA)) {
						final StorageManager storageManager = _activity.getPersistenceManager().getStorageManager();
						img = storageManager.getFile(storageManager.getFile(parent), path);
					}
					ReceiptRow.Builder builder = new ReceiptRow.Builder(id);
					return builder.setName(name)
								  .setCategory(category)
								  .setImage(img)
								  .setDate(date)
								  .setComment(comment)
								  .setPrice(price)
								  .setTax(tax)
								  .setIsExpenseable(expensable)
								  .setCurrency(currency)
								  .setIsFullPage(fullpage)
								  .setExtraEditText1(extra_edittext_1)
								  .setExtraEditText2(extra_edittext_2)
								  .setExtraEditText3(extra_edittext_3)
								  .build();
				}
				else {
					return null;
				}
			}
			finally { // Close the cursor and db to avoid memory leaks
				if (c != null) c.close(); 
			}
		}
	}
	
	public ReceiptRow insertReceiptSerial(TripRow trip, File img, String name, String category, Date date, 
			String comment, String price, String tax, boolean expensable, String currency, boolean fullpage, 
			String extra_edittext_1, String extra_edittext_2, String extra_edittext_3) throws SQLException {
		
		return insertReceiptHelper(trip, img, name, category, date, comment, price, tax, expensable, currency, 
				fullpage, extra_edittext_1, extra_edittext_2, extra_edittext_3);
	}
	
	public void insertReceiptParallel(TripRow trip, File img, String name, String category, Date date, 
			String comment, String price, String tax, boolean expensable, String currency, boolean fullpage, 
			String extra_edittext_1, String extra_edittext_2, String extra_edittext_3) {
		
		if (mReceiptRowListener == null) {
			if (BuildConfig.DEBUG) Log.d(TAG, "No ReceiptRowListener was registered.");
		}
		(new InsertReceiptWorker(trip, img, name, category, date, comment, price, tax, expensable, currency, 
				fullpage, extra_edittext_1, extra_edittext_2, extra_edittext_3)).execute(new Void[0]);
	}
	
	private ReceiptRow insertReceiptHelper(TripRow trip, File img, String name, String category, Date date, 
			String comment, String price, String tax, boolean expensable, String currency, boolean fullpage, 
			String extra_edittext_1, String extra_edittext_2, String extra_edittext_3) throws SQLException {
		
		final int rcptCnt = this.getReceiptsSerial(trip).length; //Use this to order things more properly
		ContentValues values = new ContentValues(10);
		if (img == null)
			values.put(ReceiptsTable.COLUMN_PATH, NO_DATA);
		else
			values.put(ReceiptsTable.COLUMN_PATH, img.getName());
		values.put(ReceiptsTable.COLUMN_PARENT, trip.getName());
		if (name.length() > 0)
			values.put(ReceiptsTable.COLUMN_NAME, name.trim());
		values.put(ReceiptsTable.COLUMN_CATEGORY, category);
		if (date == null) {
			if (_now == null)
				_now = new Time();
			_now.setToNow();
			values.put(ReceiptsTable.COLUMN_DATE, _now.toMillis(false));
		}
		else
			values.put(ReceiptsTable.COLUMN_DATE, date.getTime()+rcptCnt); //In theory, this hack may cause issue if there are > 1000 receipts. I imagine other bugs will arise before this point
		values.put(ReceiptsTable.COLUMN_COMMENT, comment);
		values.put(ReceiptsTable.COLUMN_EXPENSEABLE, expensable);
		values.put(ReceiptsTable.COLUMN_ISO4217, currency);
		values.put(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE, !fullpage);
		if (price.length() > 0)
			values.put(ReceiptsTable.COLUMN_PRICE, price);
		if (tax.length() > 0)
			values.put(ReceiptsTable.COLUMN_TAX, tax);
		//Extras
		if (extra_edittext_1 == null)
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, NO_DATA);
		else {
			if (extra_edittext_1.equalsIgnoreCase("null")) extra_edittext_1 = "";
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, extra_edittext_1);
		}
		if (extra_edittext_2 == null)
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2, NO_DATA);
		else {
			if (extra_edittext_2.equalsIgnoreCase("null")) extra_edittext_2 = "";
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2, extra_edittext_2);
		}
		if (extra_edittext_3 == null)
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, NO_DATA);
		else {
			if (extra_edittext_3.equalsIgnoreCase("null")) extra_edittext_3 = "";
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, extra_edittext_3);
		}
		
		ReceiptRow insertReceipt;
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			Cursor c = null;
			try {
				db = this.getWritableDatabase();
				if (db.insertOrThrow(ReceiptsTable.TABLE_NAME, null, values) == -1)
					insertReceipt = null;
				else {
					this.updateTripPrice(trip);
					if (_receiptMapCache.containsKey(trip))
						_receiptMapCache.remove(trip);
					c = db.rawQuery("SELECT last_insert_rowid()", null);
					if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
						final int id = c.getInt(0);
						date.setTime(date.getTime()+rcptCnt);
						ReceiptRow.Builder builder = new ReceiptRow.Builder(id);
						insertReceipt = builder.setName(name)
											   .setCategory(category)
											   .setImage(img)
											   .setDate(date)
											   .setComment(comment)
											   .setPrice(price)
											   .setTax(tax)
											   .setIsExpenseable(expensable)
											   .setCurrency(currency)
											   .setIsFullPage(fullpage)
											   .setExtraEditText1(extra_edittext_1)
											   .setExtraEditText2(extra_edittext_2)
											   .setExtraEditText3(extra_edittext_3)
											   .build();
					}
					else {
						insertReceipt = null;
					}
				}
			}
			finally { // Close the cursor and db to avoid memory leaks
				if (c != null) c.close(); 
			}
		}
		if (insertReceipt != null) {
			synchronized (mReceiptCacheLock) {
				if (_receiptMapCache.containsKey(trip))
					_receiptMapCache.remove(trip);
			}
		}
		return insertReceipt;
	}
	
	private class InsertReceiptWorker extends AsyncTask<Void, Void, ReceiptRow> {
		
		private final TripRow mTrip;
		private final File mImg;
		private final String mName, mCategory, mComment, mPrice, mTax, mCurrency, mExtra_edittext_1, mExtra_edittext_2, mExtra_edittext_3;
		private final Date mDate;
		private final boolean mExpensable, mFullpage;
		private SQLException mException;
		
		public InsertReceiptWorker(TripRow trip, File img, String name, String category, Date date, 
				String comment, String price, String tax, boolean expensable, String currency, boolean fullpage, 
				String extra_edittext_1, String extra_edittext_2, String extra_edittext_3) {
			mTrip = trip;
			mImg = img;
			mName = name;
			mCategory = category;
			mDate = date;
			mComment = comment;
			mPrice = price;
			mTax = tax;
			mExpensable = expensable;
			mCurrency = currency;
			mFullpage = fullpage;
			mExtra_edittext_1 = extra_edittext_1;
			mExtra_edittext_2 = extra_edittext_2;
			mExtra_edittext_3 = extra_edittext_3;
		}

		@Override
		protected ReceiptRow doInBackground(Void... params) {
			try {
				return insertReceiptHelper(mTrip, mImg, mName, mCategory, mDate, mComment, mPrice, mTax, mExpensable, mCurrency, mFullpage, mExtra_edittext_1, mExtra_edittext_2, mExtra_edittext_3);
			}
			catch (SQLException ex) {
				mException = ex;
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(ReceiptRow result) {
			if (mReceiptRowListener != null) {
				if (result != null) 
					mReceiptRowListener.onReceiptRowInsertSuccess(result);
				else
					mReceiptRowListener.onReceiptRowInsertFailure(mException);
			}
		}
	}
	
	public ReceiptRow updateReceiptSerial(ReceiptRow oldReceipt, TripRow trip, String name, String category, Date date, 
			String comment, String price, String tax, boolean expensable, String currency, boolean fullpage, 
			String extra_edittext_1, String extra_edittext_2, String extra_edittext_3) {
		return updateReceiptHelper(oldReceipt, trip, name, category, date, comment, price, tax, expensable, currency, fullpage, extra_edittext_1, extra_edittext_2, extra_edittext_3);
	}
	
	public void updateReceiptParallel(ReceiptRow oldReceipt, TripRow trip, String name, String category, Date date, 
			String comment, String price, String tax, boolean expensable, String currency, boolean fullpage, 
			String extra_edittext_1, String extra_edittext_2, String extra_edittext_3) {
		
		if (mReceiptRowListener == null) {
			if (BuildConfig.DEBUG) Log.d(TAG, "No ReceiptRowListener was registered.");
		}
		(new UpdateReceiptWorker(oldReceipt, trip, name, category, date, comment, price, tax, expensable, currency, 
				fullpage, extra_edittext_1, extra_edittext_2, extra_edittext_3)).execute(new Void[0]);
	}
	
	private ReceiptRow updateReceiptHelper(ReceiptRow oldReceipt, TripRow trip, String name, String category, Date date, 
			String comment, String price, String tax, boolean expensable, String currency, boolean fullpage, 
			String extra_edittext_1, String extra_edittext_2, String extra_edittext_3) {
		
		ContentValues values = new ContentValues(10);
		values.put(ReceiptsTable.COLUMN_NAME, name.trim());
		values.put(ReceiptsTable.COLUMN_CATEGORY, category);
		if ((date.getTime() % 3600000) == 0)
			values.put(ReceiptsTable.COLUMN_DATE, date.getTime() + oldReceipt.getId());
		else
			values.put(ReceiptsTable.COLUMN_DATE, date.getTime());
		values.put(ReceiptsTable.COLUMN_COMMENT, comment);
		if (price.length() > 0)
			values.put(ReceiptsTable.COLUMN_PRICE, price);
		if (tax.length() > 0)
			values.put(ReceiptsTable.COLUMN_TAX, tax);
		values.put(ReceiptsTable.COLUMN_EXPENSEABLE, expensable);
		values.put(ReceiptsTable.COLUMN_ISO4217, currency);
		values.put(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE, !fullpage);
		////Extras
		if (extra_edittext_1 == null)
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, NO_DATA);
		else {
			if (extra_edittext_1.equalsIgnoreCase("null")) extra_edittext_1 = "";
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, extra_edittext_1);
		}
		if (extra_edittext_2 == null)
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2, NO_DATA);
		else {
			if (extra_edittext_2.equalsIgnoreCase("null")) extra_edittext_2 = "";
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2, extra_edittext_2);
		}
		if (extra_edittext_3 == null)
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, NO_DATA);
		else {
			if (extra_edittext_3.equalsIgnoreCase("null")) extra_edittext_3 = "";
			values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, extra_edittext_3);
		}
		
		ReceiptRow updatedReceipt;
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			try {
				db = this.getWritableDatabase();
				if (values == null || (db.update(ReceiptsTable.TABLE_NAME, values, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(oldReceipt.getId())}) == 0))
					updatedReceipt = null;
				else {
					this.updateTripPrice(trip);
					ReceiptRow.Builder builder = new ReceiptRow.Builder(oldReceipt.getId());
					updatedReceipt = builder.setName(name)
										   	.setCategory(category)
										   	.setImage(oldReceipt.getImage())
										   	.setDate(date)
										   	.setComment(comment)
										   	.setPrice(price)
										   	.setTax(tax)
										   	.setIsExpenseable(expensable)
										   	.setCurrency(currency)
										   	.setIsFullPage(fullpage)
										   	.setExtraEditText1(extra_edittext_1)
										   	.setExtraEditText2(extra_edittext_2)
										   	.setExtraEditText3(extra_edittext_3)
										   	.build();
					
				}
			}
			catch (SQLException e) {
				return null;
			}
		}
		synchronized (mReceiptCacheLock) {
			if (updatedReceipt != null)
				_receiptMapCache.remove(trip);
		}
		return updatedReceipt;
	}
	
	private class UpdateReceiptWorker extends AsyncTask<Void, Void, ReceiptRow> {
		
		private final ReceiptRow mOldReceipt;
		private final TripRow mTrip;
		private final String mName, mCategory, mComment, mPrice, mTax, mCurrency, mExtra_edittext_1, mExtra_edittext_2, mExtra_edittext_3;
		private final Date mDate;
		private final boolean mExpensable, mFullpage;
		private SQLException mException;
		
		public UpdateReceiptWorker(ReceiptRow oldReceipt, TripRow trip, String name, String category, Date date, 
				String comment, String price, String tax, boolean expensable, String currency, boolean fullpage, 
				String extra_edittext_1, String extra_edittext_2, String extra_edittext_3) {
			mOldReceipt = oldReceipt;
			mTrip = trip;
			mName = name;
			mCategory = category;
			mDate = date;
			mComment = comment;
			mPrice = price;
			mTax = tax;
			mExpensable = expensable;
			mCurrency = currency;
			mFullpage = fullpage;
			mExtra_edittext_1 = extra_edittext_1;
			mExtra_edittext_2 = extra_edittext_2;
			mExtra_edittext_3 = extra_edittext_3;
		}

		@Override
		protected ReceiptRow doInBackground(Void... params) {
			return updateReceiptHelper(mOldReceipt, mTrip, mName, mCategory, mDate, mComment, mPrice, mTax, mExpensable, mCurrency, mFullpage, mExtra_edittext_1, mExtra_edittext_2, mExtra_edittext_3);
		}
		
		@Override
		protected void onPostExecute(ReceiptRow result) {
			if (mReceiptRowListener != null) {
				if (result != null) 
					mReceiptRowListener.onReceiptRowUpdateSuccess(result);
				else
					mReceiptRowListener.onReceiptRowUpdateFailure();
			}
		}
	}
	
	
	public final ReceiptRow updateReceiptImg(final ReceiptRow oldReceipt, final File img) {
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			try {
				db = this.getReadableDatabase();
				ContentValues values = new ContentValues(1);
				if (img == null)
					values.put(ReceiptsTable.COLUMN_PATH, NO_DATA);
				else
					values.put(ReceiptsTable.COLUMN_PATH, img.getName());
				if (values == null || (db.update(ReceiptsTable.TABLE_NAME, values, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(oldReceipt.getId())}) == 0))
					return null;
				else {
					oldReceipt.setImage(img);
					return oldReceipt;
				}
			}
			catch (SQLException e) {
				return null;
			}
		}
	}
	
	public boolean deleteReceiptSerial(ReceiptRow receipt, TripRow currentTrip) {
		return deleteReceiptHelper(receipt, currentTrip);
	}
	
	public void deleteReceiptParallel(ReceiptRow receipt, TripRow currentTrip) {
		if (mReceiptRowListener == null) {
			if (BuildConfig.DEBUG) Log.d(TAG, "No ReceiptRowListener was registered.");
		}
		(new DeleteReceiptWorker(receipt, currentTrip)).execute(new Void[0]);
	}
	
	private boolean deleteReceiptHelper(ReceiptRow receipt, TripRow currentTrip) {
		boolean success = false;
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			db = this.getWritableDatabase();
			success = (db.delete(ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(receipt.getId())}) > 0);
		}
		if (success) {
			this.updateTripPrice(currentTrip);
			synchronized (mReceiptCacheLock) {
				_receiptMapCache.remove(currentTrip);
			}
		}
		return success;
	}
	
	private class DeleteReceiptWorker extends AsyncTask<Void, Void, Boolean> {
		
		private final ReceiptRow mReceipt;
		private final TripRow mTrip;
		
		public DeleteReceiptWorker(ReceiptRow receipt, TripRow currentTrip) {
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
				if (result)
					mReceiptRowListener.onReceiptDeleteSuccess(mReceipt);
				else
					mReceiptRowListener.onReceiptDeleteFailure();
			}
		}
		
	}
	
	public final boolean moveReceiptUp(final TripRow trip, final ReceiptRow receipt) {
		ReceiptRow[] receipts = getReceiptsSerial(trip);
		int index = 0;
		for (int i =0; i < receipts.length; i++) {
			if (receipt.getId() == receipts[i].getId()) {
				index = i-1;
				break;
			}
		}
		if (index < 0)
			return false;
		ReceiptRow up = receipts[index];
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			try {
				db = this.getWritableDatabase();
				ContentValues upValues = new ContentValues(1);
				ContentValues downValues = new ContentValues(1);
				upValues.put(ReceiptsTable.COLUMN_DATE, receipt.getDate().getTime());
				if (receipt.getDate().getTime() != up.getDate().getTime())
					downValues.put(ReceiptsTable.COLUMN_DATE, up.getDate().getTime());
				else
					downValues.put(ReceiptsTable.COLUMN_DATE, up.getDate().getTime()+1L);
				if ((db.update(ReceiptsTable.TABLE_NAME, upValues, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(up.getId())}) == 0))
					return false;
				if ((db.update(ReceiptsTable.TABLE_NAME, downValues, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(receipt.getId())}) == 0))
					return false;
				_receiptMapCache.remove(trip);
				return true;
			}
			catch (SQLException e) {
				return false;
			}
		}
	}
	
	public final boolean moveReceiptDown(final TripRow trip, final ReceiptRow receipt) {
		ReceiptRow[] receipts = getReceiptsSerial(trip);
		int index = receipts.length-1;
		for (int i =0; i < receipts.length; i++) {
			if (receipt.getId() == receipts[i].getId()) {
				index = i+1;
				break;
			}
		}
		if (index > (receipts.length-1))
			return false;
		ReceiptRow down = receipts[index];
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			try {
				db = this.getWritableDatabase();
				ContentValues upValues = new ContentValues(1);
				ContentValues downValues = new ContentValues(1);
				if (receipt.getDate().getTime() != down.getDate().getTime())
					upValues.put(ReceiptsTable.COLUMN_DATE, down.getDate().getTime());
				else
					upValues.put(ReceiptsTable.COLUMN_DATE, down.getDate().getTime()-1L);
				downValues.put(ReceiptsTable.COLUMN_DATE, receipt.getDate().getTime());
				if ((db.update(ReceiptsTable.TABLE_NAME, upValues, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(receipt.getId())}) == 0))
					return false;
				if ((db.update(ReceiptsTable.TABLE_NAME, downValues, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(down.getId())}) == 0))
					return false;
				_receiptMapCache.remove(trip);
				return true;
			}
			catch (SQLException e) {
				return false;
			}
		}	
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//	Categories Methods
	////////////////////////////////////////////////////////////////////////////////////////////////////
	public final ArrayList<CharSequence> getCategoriesList() {
		if (_categoryList != null) return _categoryList;
		if (_categories == null) buildCategories();
		_categoryList = new ArrayList<CharSequence>(_categories.keySet());
		Collections.sort(_categoryList, _charSequenceComparator);
		return _categoryList;
	}
	
	private final CharSequenceComparator _charSequenceComparator = new CharSequenceComparator();
	private final class CharSequenceComparator implements Comparator<CharSequence> {
		@Override
		public int compare(CharSequence str1, CharSequence str2) {
			return str1.toString().compareToIgnoreCase(str2.toString());
		}
	}
	
	public final String getCategoryCode(String categoryName) {
		if (_categories == null || _categories.size() == 0) buildCategories();
		return _categories.get(categoryName);
	}
	
	private final void buildCategories() {
		_categories = new HashMap<String, String>();
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
						_categories.put(name, code);
					} 
					while (c.moveToNext());
				}
			}
			finally { // Close the cursor and db to avoid memory leaks
				if (c != null) c.close(); 
			}
		}
	}
	
	public final ArrayList<CharSequence> getCurrenciesList() {
		if (_currencyList != null) return _currencyList;
		_currencyList = new ArrayList<CharSequence>();
		Locale[] locales = Locale.getAvailableLocales();
		final int size = locales.length;
		for (int i=0; i<size; i++) {
			try {
				CharSequence iso4217 = Currency.getInstance(locales[i]).getCurrencyCode();
				if (!_currencyList.contains(iso4217))
					_currencyList.add(iso4217);
			} catch (IllegalArgumentException ex) {} //Catches unsupported currencies
		}
		addAdditionalCurrencies();
		Collections.sort(_currencyList, _charSequenceComparator);
		return _currencyList;
	}
	
	private final void addAdditionalCurrencies() {
		if (_currencyList == null) return;
		ArrayList<CharSequence> otherCurrencies = new ArrayList<CharSequence>();
		otherCurrencies.add("AOA"); otherCurrencies.add("ARS"); otherCurrencies.add("AED"); otherCurrencies.add("BIF"); otherCurrencies.add("BSF"); otherCurrencies.add("CDF");
		otherCurrencies.add("CLP"); otherCurrencies.add("DJF"); otherCurrencies.add("ETB"); 
		otherCurrencies.add("GMD"); otherCurrencies.add("GHS"); otherCurrencies.add("GNF"); otherCurrencies.add("ISK");
		otherCurrencies.add("KES"); otherCurrencies.add("LSL"); otherCurrencies.add("LRD");
		otherCurrencies.add("MWK"); otherCurrencies.add("MUR"); otherCurrencies.add("MRO"); otherCurrencies.add("MYR");
		otherCurrencies.add("MZM"); otherCurrencies.add("RWF"); otherCurrencies.add("SCR");
		otherCurrencies.add("SAR"); otherCurrencies.add("SLL"); otherCurrencies.add("SOS"); otherCurrencies.add("THB");
		otherCurrencies.add("TZS"); otherCurrencies.add("UGX"); 
		otherCurrencies.add("ZMK"); otherCurrencies.add("ZWD");
		
		otherCurrencies.add("DRC"); otherCurrencies.add("XOF"); // Bad Currencies => WBCurrency
		CharSequence code;
		final int size = otherCurrencies.size();
		for (int i=0; i < size; i++) {
			code = otherCurrencies.get(i);
			if (!_currencyList.contains(code))
				_currencyList.add(code);
		}
		
	}
	
	public final boolean insertCategory(final String name, final String code) throws SQLException {
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			db = this.getWritableDatabase();
			ContentValues values = new ContentValues(2);
			values.put(CategoriesTable.COLUMN_NAME, name);
			values.put(CategoriesTable.COLUMN_CODE, code);
			if (db.insertOrThrow(CategoriesTable.TABLE_NAME, null, values) == -1)
				return false;
			else {
				_categories.put(name, code);
				_categoryList.add(name);
				Collections.sort(_categoryList, _charSequenceComparator);
				return true;
			}
		}
	}
	
	public final boolean insertCategoryNoCache(final String name, final String code) throws SQLException {
		final SQLiteDatabase db = (_initDB != null) ? _initDB : this.getReadableDatabase();
		ContentValues values = new ContentValues(2);
		values.put(CategoriesTable.COLUMN_NAME, name);
		values.put(CategoriesTable.COLUMN_CODE, code);
		if (db.insertOrThrow(CategoriesTable.TABLE_NAME, null, values) == -1)
			return false;
		else 
			return true;
	}
	
	public final boolean updateCategory(final String oldName, final String newName, final String newCode) {
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			db = this.getWritableDatabase();
			ContentValues values = new ContentValues(2);
			values.put(CategoriesTable.COLUMN_NAME, newName);
			values.put(CategoriesTable.COLUMN_CODE, newCode);
			if (db.update(CategoriesTable.TABLE_NAME, values, CategoriesTable.COLUMN_NAME + " = ?", new String[] {oldName}) == 0)
				return false;
			else {
				_categories.remove(oldName);
				_categoryList.remove(oldName);
				_categories.put(newName, newCode);
				_categoryList.add(newName);
				Collections.sort(_categoryList, _charSequenceComparator);
				return true;
			}
			
		}
	}
	
	public final boolean deleteCategory(final String name) {
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			db = this.getWritableDatabase();
			final boolean success = (db.delete(CategoriesTable.TABLE_NAME, CategoriesTable.COLUMN_NAME + " = ?", new String[] {name}) > 0); 
			if (success) {
				_categories.remove(name);
				_categoryList.remove(name);
			}
			return success;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//	CSV Column Methods
	////////////////////////////////////////////////////////////////////////////////////////////////////
	public final CSVColumns getCSVColumns(Flex flex) {
		if (_csvColumns != null)
			return _csvColumns;
		_csvColumns = new CSVColumns(_activity, this, flex);
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			Cursor c = null;
			try {
				db = this.getReadableDatabase();
				c = db.query(CSVTable.TABLE_NAME, null, null, null, null, null, null);
				if (c != null && c.moveToFirst()) {
					final int idxIndex = c.getColumnIndex(CSVTable.COLUMN_ID);
					final int typeIndex = c.getColumnIndex(CSVTable.COLUMN_TYPE);
					do {
						final int index = c.getInt(idxIndex);
						final String type = c.getString(typeIndex);
						_csvColumns.add(index, type);
					} 
					while (c.moveToNext());
				}
				return _csvColumns;
			}
			finally { // Close the cursor and db to avoid memory leaks
				if (c != null) c.close(); 
			}
		}
	}
	
	public final boolean insertCSVColumn() {
		ContentValues values = new ContentValues(1);
		values.put(CSVTable.COLUMN_TYPE, CSVColumns.BLANK(_activity.getFlex()));
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			Cursor c = null;
			try {
				db = this.getWritableDatabase();
				if (db.insertOrThrow(CSVTable.TABLE_NAME, null, values) == -1)
					return false;
				else {
					c = db.rawQuery("SELECT last_insert_rowid()", null);
					if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
						final int idx = c.getInt(0);
						c.close();
						_csvColumns.add(idx, CSVColumns.BLANK(_activity.getFlex()));
					}
					else {
						c.close();
						return false;
					}
					return true;
				}
			}
			finally { // Close the cursor and db to avoid memory leaks
				if (c != null) c.close(); 
			}
		}
	}
	
	public final boolean insertCSVColumnNoCache(String column) {
		ContentValues values = new ContentValues(1);
		values.put(CSVTable.COLUMN_TYPE, column);
		if (_initDB != null) {
			//TODO: Determine if database lock should be used here
			if (_initDB.insertOrThrow(CSVTable.TABLE_NAME, null, values) == -1)
				return false;
			else
				return true;
		}
		else {
			synchronized (mDatabaseLock) {
				SQLiteDatabase db = null;
				db = this.getWritableDatabase();
				if (db.insertOrThrow(CSVTable.TABLE_NAME, null, values) == -1)
					return false;
				else
					return true;
			}
		}
	}
	
	public final boolean deleteCSVColumn() {
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			db = this.getReadableDatabase();
			int idx = _csvColumns.removeLast();
			if (idx < 0)
				return false;
			return db.delete(CSVTable.TABLE_NAME, CSVTable.COLUMN_ID + " = ?", new String[] {Integer.toString(idx)}) > 0; 
		}
	}
	
	public final boolean updateCSVColumn(int arrayListIndex, int optionIndex) { //Note index here refers to the actual index and not the ID
		ContentValues values = new ContentValues(1);
		synchronized (mDatabaseLock) {
			SQLiteDatabase db = null;
			try {
				db = this.getReadableDatabase();
				CSVColumn column = _csvColumns.update(arrayListIndex, optionIndex);
				values.put(CSVTable.COLUMN_TYPE, column.columnType);
				if (db.update(CSVTable.TABLE_NAME, values, CSVTable.COLUMN_ID + " = ?", new String[] {Integer.toString(column.index)}) == 0)
					return false;
				else 
					return true;
			}
			catch (SQLException e) {
				return false;
			}
		}
	}
	
	public final synchronized boolean merge(String dbPath, String packageName, boolean overwrite) {
		_areTripsValid = false;
		_receiptMapCache.clear();
		synchronized (mDatabaseLock) {
			SQLiteDatabase importDB = null, currDB = null;
			Cursor c = null, countCursor = null;
			try {
				if (dbPath == null)
					return false;
				currDB = this.getWritableDatabase();
				importDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
				//Merge Trips
				if(BuildConfig.DEBUG) Log.d(TAG, "Merging Trips");
				c = importDB.query(TripsTable.TABLE_NAME, null, null, null, null, null, TripsTable.COLUMN_TO + " DESC");
				if (c != null && c.moveToFirst()) {
					final int nameIndex = c.getColumnIndex(TripsTable.COLUMN_NAME);
					final int fromIndex = c.getColumnIndex(TripsTable.COLUMN_FROM);
					final int toIndex = c.getColumnIndex(TripsTable.COLUMN_TO);
					final int priceIndex = c.getColumnIndex(TripsTable.COLUMN_PRICE);
					final int mileageIndex = c.getColumnIndex(TripsTable.COLUMN_MILEAGE);
					do {
						String name = c.getString(nameIndex);
						if (name.contains("wb.receipts")) { //Backwards compatibility stuff
							if (packageName.equalsIgnoreCase("wb.receipts")) { name = name.replace("wb.receiptspro/", "wb.receipts/"); }
							else if (packageName.equalsIgnoreCase("wb.receiptspro")) { name = name.replace("wb.receipts/", "wb.receiptspro/"); }
							File f = new File(name);
							name = f.getName();
						}
						final long from = c.getLong(fromIndex);
						final long to = c.getLong(toIndex);
						final String price = c.getString(priceIndex);
						final int mileage = c.getInt(mileageIndex);
						ContentValues values = new ContentValues(5);
						values.put(TripsTable.COLUMN_NAME, name);
						values.put(TripsTable.COLUMN_FROM, from);
						values.put(TripsTable.COLUMN_TO, to);
						values.put(TripsTable.COLUMN_PRICE, price);
						values.put(TripsTable.COLUMN_MILEAGE, mileage);
						if (overwrite) currDB.insertWithOnConflict(TripsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
						else currDB.insertWithOnConflict(TripsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
					} 
					while (c.moveToNext());
				}
				else {
					return false;
				}
				
				//Merge Receipts
				if(BuildConfig.DEBUG) Log.d(TAG, "Merging Receipts");
				final String queryCount = "SELECT COUNT(*), " + ReceiptsTable.COLUMN_ID + " FROM " + ReceiptsTable.TABLE_NAME + " WHERE " + ReceiptsTable.COLUMN_PATH + "=? AND " + ReceiptsTable.COLUMN_NAME + "=? AND " + ReceiptsTable.COLUMN_DATE + "=?";
				c = importDB.query(ReceiptsTable.TABLE_NAME, null, null, null, null, null, null);
				if (c != null && c.moveToFirst()) {
					final int idIndex = c.getColumnIndex(ReceiptsTable.COLUMN_ID);
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
					do {
						final int id = c.getInt(idIndex);
						final String oldPath = c.getString(pathIndex);
						String newPath = new String(oldPath);
						if (newPath.contains("wb.receipts")) { //Backwards compatibility stuff
							if (packageName.equalsIgnoreCase("wb.receipts")) { newPath = oldPath.replace("wb.receiptspro/", "wb.receipts/"); }
							else if (packageName.equalsIgnoreCase("wb.receiptspro")) { newPath = oldPath.replace("wb.receipts/", "wb.receiptspro/"); }
							File f = new File(newPath);
							newPath = f.getName();
						}
						final String name = c.getString(nameIndex);
						final String oldParent = c.getString(parentIndex);
						String newParent = new String(oldParent);
						if (newParent.contains("wb.receipts")) { //Backwards compatibility stuff
							if (packageName.equalsIgnoreCase("wb.receipts")) { newParent = oldParent.replace("wb.receiptspro/", "wb.receipts/"); }
							else if (packageName.equalsIgnoreCase("wb.receiptspro")) { newParent = oldParent.replace("wb.receipts/", "wb.receiptspro/"); }
							File f = new File(newParent);
							newParent = f.getName();
						}
						final String category = c.getString(categoryIndex);
						final String price = c.getString(priceIndex);
						final long date = c.getLong(dateIndex);
						final String comment = c.getString(commentIndex);
						final boolean expensable = c.getInt(expenseableIndex)>0;
						final String currency = c.getString(currencyIndex);
						final boolean fullpage = !(c.getInt(fullpageIndex)>0);
						final String extra_edittext_1 = c.getString(extra_edittext_1_Index);
						final String extra_edittext_2 = c.getString(extra_edittext_2_Index);
						final String extra_edittext_3 = c.getString(extra_edittext_3_Index);
						countCursor = currDB.rawQuery(queryCount, new String[] {newPath, name, Long.toString(date)});
						if (countCursor != null && countCursor.moveToFirst()) {
							int count = countCursor.getInt(0);
							int updateID = countCursor.getInt(1);
							final ContentValues values = new ContentValues(14);
							values.put(ReceiptsTable.COLUMN_ID, id);
							values.put(ReceiptsTable.COLUMN_PATH, newPath);
							values.put(ReceiptsTable.COLUMN_NAME, name);
							values.put(ReceiptsTable.COLUMN_PARENT, newParent);
							values.put(ReceiptsTable.COLUMN_CATEGORY, category);
							values.put(ReceiptsTable.COLUMN_PRICE, price);
							values.put(ReceiptsTable.COLUMN_DATE, date);
							values.put(ReceiptsTable.COLUMN_COMMENT, comment);
							values.put(ReceiptsTable.COLUMN_EXPENSEABLE, expensable);
							values.put(ReceiptsTable.COLUMN_ISO4217, currency);
							values.put(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE, fullpage);
							values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1, extra_edittext_1);
							values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2, extra_edittext_2);
							values.put(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3, extra_edittext_3);
							if (count > 0 && overwrite) { //Update
								currDB.update(ReceiptsTable.TABLE_NAME, values, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(updateID)});
							}
							else { //insert
								if (overwrite) currDB.insertWithOnConflict(ReceiptsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
								else currDB.insertWithOnConflict(ReceiptsTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
							}
						}
					} 
					while (c.moveToNext());
				}
				else {
					return false;
				}
				
				//Merge Categories
				//No clean way to merge (since auto-increment is not guaranteed to have any order and there isn't enough outlying data) => Always overwirte
				if(BuildConfig.DEBUG) Log.d(TAG, "Merging Categories");
				c = importDB.query(CategoriesTable.TABLE_NAME, null, null, null, null, null, null);
				if (c != null && c.moveToFirst()) {
					currDB.delete(CategoriesTable.TABLE_NAME, null, null); //DELETE FROM Categories
					final int nameIndex = c.getColumnIndex(CategoriesTable.COLUMN_NAME);
					final int codeIndex = c.getColumnIndex(CategoriesTable.COLUMN_CODE);
					final int breakdownIndex = c.getColumnIndex(CategoriesTable.COLUMN_BREAKDOWN);
					do {
						final String name = c.getString(nameIndex);
						final String code = c.getString(codeIndex);
						final boolean breakdown = c.getInt(breakdownIndex)>0;
						ContentValues values = new ContentValues(3);
						values.put(CategoriesTable.COLUMN_NAME, name);
						values.put(CategoriesTable.COLUMN_CODE, code);
						values.put(CategoriesTable.COLUMN_BREAKDOWN, breakdown);
						currDB.insert(CategoriesTable.TABLE_NAME, null, values);
					} 
					while (c.moveToNext());
				}
				else {
					return false;
				}
				
				//Merge CSV
				//No clean way to merge (since auto-increment is not guaranteed to have any order and there isn't enough outlying data) => Always overwirte
				if(BuildConfig.DEBUG) Log.d(TAG, "Merging CSV");
				c = importDB.query(CSVTable.TABLE_NAME, null, null, null, null, null, null);
				if (c != null && c.moveToFirst()) {
					currDB.delete(CSVTable.TABLE_NAME, null, null); //DELETE FROM CSVTable
					final int idxIndex = c.getColumnIndex(CSVTable.COLUMN_ID);
					final int typeIndex = c.getColumnIndex(CSVTable.COLUMN_TYPE);
					do {
						final int index = c.getInt(idxIndex);
						final String type = c.getString(typeIndex);
						ContentValues values = new ContentValues(2);
						values.put(CSVTable.COLUMN_ID, index);
						values.put(CSVTable.COLUMN_TYPE, type);
						currDB.insert(CSVTable.TABLE_NAME, null, values);
					} 
					while (c.moveToNext());
				}
				else {
					return false;
				}
				return true;
			}
			catch (Exception e) {
				if(BuildConfig.DEBUG) Log.e(TAG, e.toString());
				return false;
			}
			finally {
				if (c != null) c.close();
				if (countCursor != null) countCursor.close();
				if (importDB != null) importDB.close();
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//	AutoCompleteTextView Methods
	////////////////////////////////////////////////////////////////////////////////////////////////////
	public Cursor getAutoCompleteCursor(CharSequence text, CharSequence tag) {
		//TODO: Fix SQL vulnerabilities
		final SQLiteDatabase db = this.getReadableDatabase();
		String sqlQuery = "";
		if (tag == TAG_RECEIPTS) {
			sqlQuery = " SELECT DISTINCT TRIM(" + ReceiptsTable.COLUMN_NAME + ") AS _id " + 
				       " FROM " + ReceiptsTable.TABLE_NAME +
				       " WHERE " + ReceiptsTable.COLUMN_NAME + " LIKE '%" + text + "%' " +
				       " ORDER BY " + ReceiptsTable.COLUMN_NAME;
		}
		else if (tag == TAG_TRIPS) {
			sqlQuery = " SELECT DISTINCT TRIM(" + TripsTable.COLUMN_NAME + ") AS _id " + 
				       " FROM " + TripsTable.TABLE_NAME +
				       " WHERE " + TripsTable.COLUMN_NAME + " LIKE '%" + text + "%' " +
				       " ORDER BY " + TripsTable.COLUMN_NAME;
		}
		synchronized (mDatabaseLock) {
			return db.rawQuery(sqlQuery, null);
		}
	}

	@Override
	public void onItemSelected(CharSequence text, CharSequence tag) {
		//TODO: Make Async
		Cursor c = null;
		SQLiteDatabase db = null;
		final String name = text.toString();
		if (tag == TAG_RECEIPTS) {
			String category = null, price = null, tmp = null;
			synchronized (mDatabaseLock) {
				try {
					db = this.getReadableDatabase();
					c = db.query(ReceiptsTable.TABLE_NAME,
								 new String[] {ReceiptsTable.COLUMN_CATEGORY, ReceiptsTable.COLUMN_PRICE},
								 ReceiptsTable.COLUMN_NAME + "= ?", 
								 new String[] {name}, 
								 null, 
								 null, 
								 ReceiptsTable.COLUMN_DATE + " DESC",
								 "2");
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
				}
				finally {
					if (c != null) c.close();
				}
			}
			if (mReceiptRowListener != null) {
				mReceiptRowListener.onReceiptRowAutoCompleteQueryResult(name, price, category);
			}
		}
	}
		

}