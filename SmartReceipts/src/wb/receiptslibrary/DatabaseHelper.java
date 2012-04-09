package wb.receiptslibrary;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;

import wb.csv.CSVColumn;
import wb.csv.CSVColumns;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;

public final class DatabaseHelper extends SQLiteOpenHelper {

	//Logging Vars
	private static final boolean D = true;
	private static final String TAG = "DatabaseHelper";
	
	//Database Info
	private static final String DATABASE_NAME = "receipts.db";
	private static final int DATABASE_VERSION = 3;
	static final String NULL = "null";
	static final String MULTI_CURRENCY = "XXXXXX";
	
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
	
	//Tables Declarations
	private static final class TripsTable {
		private TripsTable() {}	
		public static final String TABLE_NAME = "trips";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_FROM = "from_date";
		public static final String COLUMN_TO = "to_date";
		public static final String COLUMN_PRICE = "price";
		public static final String COLUMN_MILEAGE = "miles";
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
		public static final String COLUMN_DATE = "rcpt_date";
		public static final String COLUMN_COMMENT = "comment";
		public static final String COLUMN_EXPENSEABLE = "expenseable";
		public static final String COLUMN_ISO4217 = "isocode";
		public static final String COLUMN_NOTFULLPAGEIMAGE = "fullpageimage";
	}
	private static final class CategoriesTable {
		private CategoriesTable() {}
		public static final String TABLE_NAME = "categories";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_CODE = "code";
		public static final String COLUMN_BREAKDOWN = "breakdown";
	}
	private static final class CSVTable {
		private CSVTable() {}
		public static final String TABLE_NAME = "csvcolumns";
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_TYPE = "type";
	}
	
	private DatabaseHelper(final SmartReceiptsActivity activity) {
		super(activity, DATABASE_NAME, null, DATABASE_VERSION); //Requests the default cursor factory
		_areTripsValid = false;
		_receiptMapCache = new HashMap<TripRow, ReceiptRow[]>();
		_activity = activity;	
	}
	
	public static final DatabaseHelper getInstance(final SmartReceiptsActivity activity) {
		if (INSTANCE == null)
			INSTANCE = new DatabaseHelper(activity);
		return INSTANCE;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//	Begin Abstract Method Overrides
	////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onCreate(final SQLiteDatabase db) {
		//N.B. This only gets called if you actually request the database using the getDatabase method
		final String trips = "CREATE TABLE " + TripsTable.TABLE_NAME + " ("
				+ TripsTable.COLUMN_NAME + " TEXT PRIMARY KEY, "
				+ TripsTable.COLUMN_FROM + " DATE, "
				+ TripsTable.COLUMN_TO + " DATE, "
				+ TripsTable.COLUMN_PRICE + " DECIMAL(10, 2) DEFAULT 0.00, "
				+ TripsTable.COLUMN_MILEAGE + " INTEGER DEFAULT 0"
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
				+ ReceiptsTable.COLUMN_EXPENSEABLE + " BOOLEAN DEFAULT 1, "
				+ ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE + " BOOLEAN DEFAULT 1"
				+ ");";
		final String categories = "CREATE TABLE " + CategoriesTable.TABLE_NAME + " ("
				+ CategoriesTable.COLUMN_NAME + " TEXT PRIMARY KEY, "
				+ CategoriesTable.COLUMN_CODE + " TEXT, "
				+ CategoriesTable.COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1"
				+ ");";
		if (D) Log.d(TAG, trips);
		if (D) Log.d(TAG, receipts);
		if (D) Log.d(TAG, categories);
		db.execSQL(trips);
		db.execSQL(receipts);
		db.execSQL(categories);
		this.createCSVTable(db);
		//********** Insert Default Category Entries Into Categories Table *********//
		final String categoryHeader = "INSERT INTO " + CategoriesTable.TABLE_NAME + "(" + CategoriesTable.COLUMN_NAME + "," + CategoriesTable.COLUMN_CODE + ") VALUES (";
		db.execSQL(categoryHeader + "\"<Category>\", \"NUL\");");
		db.execSQL(categoryHeader + "\"Airfare\", \"AIRP\");");
		db.execSQL(categoryHeader + "\"Breakfast\", \"BRFT\");");
		db.execSQL(categoryHeader + "\"Dinner\", \"DINN\");");
		db.execSQL(categoryHeader + "\"Entertainment\", \"ENT\");");
		db.execSQL(categoryHeader + "\"Gasoline\", \"GAS\");");
		db.execSQL(categoryHeader + "\"Gift\", \"GIFT\");");
		db.execSQL(categoryHeader + "\"Hotel\", \"HTL\");");
		db.execSQL(categoryHeader + "\"Laundry\", \"LAUN\");");
		db.execSQL(categoryHeader + "\"Lunch\", \"LNCH\");");
		db.execSQL(categoryHeader + "\"Other\", \"MISC\");");
		db.execSQL(categoryHeader + "\"Parking/Tolls\", \"PARK\");");
		db.execSQL(categoryHeader + "\"Postage/Shipping\", \"POST\");");
		db.execSQL(categoryHeader + "\"Car Rental\", \"RCAR\");");
		db.execSQL(categoryHeader + "\"Taxi/Bus\", \"TAXI\");");
		db.execSQL(categoryHeader + "\"Telephone/Fax\", \"TELE\");");
		db.execSQL(categoryHeader + "\"Tip\", \"TIP\");");
		db.execSQL(categoryHeader + "\"Train\", \"TRN\");");
		db.execSQL(categoryHeader + "\"Books/Periodicals\", \"ZBKP\");");
		db.execSQL(categoryHeader + "\"Cell Phone\", \"ZCEL\");");
		db.execSQL(categoryHeader + "\"Dues/Subscriptions\", \"ZDUE\");");
		db.execSQL(categoryHeader + "\"Meals (Justified)\", \"ZMEO\");");
		db.execSQL(categoryHeader + "\"Stantionery/Stations\", \"ZSTS\");");
		db.execSQL(categoryHeader + "\"Training Fees\", \"ZTRN\");");
		_activity.onFirstRun();
	}
	
	@Override
	public final void onUpgrade(final SQLiteDatabase db, int oldVersion, final int newVersion) {
		if(D) Log.d(TAG, "Upgrading the database from version " + oldVersion + " to " + newVersion);
		if (oldVersion == 1) { // Add currency column to receipts table
			final String alterReceipts = "ALTER TABLE " + ReceiptsTable.TABLE_NAME 
					+ " ADD " + ReceiptsTable.COLUMN_ISO4217 + " TEXT NOT NULL "
					+ "DEFAULT " + _activity._currency;
			if (D) Log.d(TAG, alterReceipts);
			db.execSQL(alterReceipts);
			oldVersion++;
		}
		if (oldVersion == 2) { // Add the mileage field to trips, add the breakdown boolean to categories, and create the CSV table 
			final String alterTrips = "ALTER TABLE " + TripsTable.TABLE_NAME 
					+ " ADD " + TripsTable.COLUMN_MILEAGE + " INTEGER DEFAULT 0";
			if (D) Log.d(TAG, alterTrips);
			db.execSQL(alterTrips);
			final String alterCategories = "ALTER TABLE " + CategoriesTable.TABLE_NAME 
					+ " ADD " + CategoriesTable.COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1";
			if(D) Log.d(TAG, alterCategories);
			db.execSQL(alterCategories);
			this.createCSVTable(db);
			oldVersion++;
		}
	}
	
	private final void createCSVTable(final SQLiteDatabase db) { //Called in onCreate and onUpgrade
		final String csv = "CREATE TABLE " + CSVTable.TABLE_NAME + " ("
				+ CSVTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ CSVTable.COLUMN_TYPE + " TEXT"
				+ ");"; 
		if (D) Log.d(TAG, csv);
		db.execSQL(csv);
		//********** Insert Default CSV Entries Into CSV Table *********//
		final String csvHeader = "INSERT INTO " + CSVTable.TABLE_NAME + " (" + CSVTable.COLUMN_TYPE + ") VALUES (";
		db.execSQL(csvHeader + "\"" + CSVColumns.CATEGORY_CODE + "\");");
		db.execSQL(csvHeader + "\"" + CSVColumns.NAME + "\");");
		db.execSQL(csvHeader + "\"" + CSVColumns.PRICE + "\");");
		db.execSQL(csvHeader + "\"" + CSVColumns.CURRENCY + "\");");
		db.execSQL(csvHeader + "\"" + CSVColumns.DATE + "\");");
	}
	
	public final void onDestroy() {
		final SQLiteDatabase db = this.getReadableDatabase();
		db.close();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//	TripRow Methods
	////////////////////////////////////////////////////////////////////////////////////////////////////
	private final String CURR_CNT_QUERY = "SELECT COUNT(*), " + ReceiptsTable.COLUMN_ISO4217 + " FROM (SELECT COUNT(*), " + ReceiptsTable.COLUMN_ISO4217 + " FROM " + ReceiptsTable.TABLE_NAME + " WHERE " + ReceiptsTable.COLUMN_PARENT + "=? GROUP BY " + ReceiptsTable.COLUMN_ISO4217 + ");";
	public final TripRow[] getTrips() {
		if (_areTripsValid)
			return _tripsCache;
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor c = db.query(TripsTable.TABLE_NAME, null, null, null, null, null, TripsTable.COLUMN_TO + " DESC");
		if (c != null && c.moveToFirst()) {
			TripRow[] trips = new TripRow[c.getCount()];
			final int nameIndex = c.getColumnIndex(TripsTable.COLUMN_NAME);
			final int fromIndex = c.getColumnIndex(TripsTable.COLUMN_FROM);
			final int toIndex = c.getColumnIndex(TripsTable.COLUMN_TO);
			final int priceIndex = c.getColumnIndex(TripsTable.COLUMN_PRICE);
			do {
				final String name = c.getString(nameIndex);
				final long from = c.getLong(fromIndex);
				final long to = c.getLong(toIndex);
				final String price = c.getString(priceIndex);
				final Cursor qc = db.rawQuery(CURR_CNT_QUERY, new String[]{name});
				int cnt; String curr = MULTI_CURRENCY;;
				if (qc != null && qc.moveToFirst() && qc.getColumnCount() > 0) {
					cnt = qc.getInt(0);
					if (cnt == 1) curr = qc.getString(1);
					else if (cnt == 0) curr = _activity._currency;
					qc.close();
				}
				trips[c.getPosition()] = new TripRow(name, from, to, price, curr);
			} 
			while (c.moveToNext());
			c.close(); //Be sure to close the cursor to avoid memory leaks
			_areTripsValid = true;
			_tripsCache = trips;
			return trips;
		}
		else {
			c.close(); //Be sure to close the cursor to avoid memory leaks
			return new TripRow[0];
		}
	}
	
	public final TripRow getTripByName(final String name) {
		if (name == null || name.length() == 0)
			return null;
		if (_areTripsValid) {
			for(int i=0; i < _tripsCache.length; i++) {
				try {
					if (_tripsCache[i].dir.getCanonicalPath().equals(name))
						return _tripsCache[i];
				}
				catch (IOException e) {}
			}
		}
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor c = db.query(TripsTable.TABLE_NAME, null, TripsTable.COLUMN_NAME + " = ?", new String[] {name}, null, null, null);
		if (c != null && c.moveToFirst()) {
			final int fromIndex = c.getColumnIndex(TripsTable.COLUMN_FROM);
			final int toIndex = c.getColumnIndex(TripsTable.COLUMN_TO);
			final int priceIndex = c.getColumnIndex(TripsTable.COLUMN_PRICE);
			final long from = c.getLong(fromIndex);
			final long to = c.getLong(toIndex);
			final String price = c.getString(priceIndex);
			final Cursor qc = db.rawQuery(CURR_CNT_QUERY, new String[]{name});
			int cnt; String curr = MULTI_CURRENCY;;
			if (qc != null && qc.moveToFirst() && qc.getColumnCount() > 0) {
				cnt = qc.getInt(0);
				if (cnt == 1) curr = qc.getString(1);
				else if (cnt == 0) curr = _activity._currency;
				qc.close();
			}
			c.close(); //Be sure to close the cursor to avoid memory leaks
			return new TripRow(name, from, to, price, curr);
		}
		else {
			c.close(); //Be sure to close the cursor to avoid memory leaks
			return null;
		}
	}
	
	//Returns the trip on success. Null otherwise
	public final TripRow insertTrip(final File dir, final Date from, final Date to) throws SQLException {
		final SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = this.getTripContentValues(dir, from, to);
		if (values == null || db.insertOrThrow(TripsTable.TABLE_NAME, null, values) == -1)
			return null;
		else {
			_areTripsValid = false;
			return new TripRow(dir, from, to, _activity._currency);
		}
	}
	
	public final TripRow updateTrip(final TripRow oldTrip, final File dir, final Date from, final Date to) {
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
			ContentValues values = this.getTripContentValues(dir, from, to);
			if (values == null || (db.update(TripsTable.TABLE_NAME, values, TripsTable.COLUMN_NAME + " = ?", new String[] {oldTrip.dir.getCanonicalPath()}) == 0))
				return null;
			else {
				_areTripsValid = false;
				if (!oldTrip.dir.getName().equalsIgnoreCase(dir.getName())) {
					if (_receiptMapCache.containsKey(oldTrip)) _receiptMapCache.remove(oldTrip);
					String oldPath = oldTrip.dir.getCanonicalPath();
					String newPath = dir.getCanonicalPath();
					ContentValues rcptVals = new ContentValues(1);
					rcptVals.put(ReceiptsTable.COLUMN_PARENT, newPath);
					db.update(ReceiptsTable.TABLE_NAME, rcptVals, ReceiptsTable.COLUMN_PARENT + " = ?", new String[] {oldPath}); //Consider rollback here				
					//Update paths
					final Cursor c = db.query(ReceiptsTable.TABLE_NAME, new String[] {ReceiptsTable.COLUMN_ID, ReceiptsTable.COLUMN_PATH}, ReceiptsTable.COLUMN_PARENT + " = ?", new String[] {newPath}, null, null, null);
					if (c != null && c.moveToFirst()) {
						final int pathIndex = c.getColumnIndex(ReceiptsTable.COLUMN_PATH);
						final int idIndex = c.getColumnIndex(ReceiptsTable.COLUMN_ID);
						do {
							final int id = c.getInt(idIndex);
							String path = c.getString(pathIndex);
							path = path.replace(oldPath, newPath);  //Don't use replace first or anything with RegEx
							rcptVals = new ContentValues(1);
							rcptVals.put(ReceiptsTable.COLUMN_PATH, path);
							db.update(ReceiptsTable.TABLE_NAME, rcptVals, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(id)});
						} 
						while (c.moveToNext());
						c.close(); //Be sure to close the cursor to avoid memory leaks
					}
					else {
						c.close(); //Be sure to close the cursor to avoid memory leaks
					}
				}
				return new TripRow(dir, from, to, oldTrip.currency);
			}
		}
		catch (IOException e) {
			Log.e(TAG, e.toString());
			return null;
		}
		catch (SQLException e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}
	
	public final boolean deleteTrip(final TripRow trip) {
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
			final String tripPath = trip.dir.getCanonicalPath();
			//Delete all child receipts (technically ON DELETE CASCADE should handle this, but i'm not certain)
			boolean success = (db.delete(ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_PARENT + " = ?", new String[] {tripPath}) >= 0); 
			if (success)
				_receiptMapCache.remove(trip);
			else
				return false;
			success = (db.delete(TripsTable.TABLE_NAME, TripsTable.COLUMN_NAME + " = ?", new String[] {trip.dir.getCanonicalPath()}) > 0);
			if (success)
				_areTripsValid = false;
			return success;
		}
		catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		}
	}
	
	private final ContentValues getTripContentValues(final File dir, final Date from, final Date to) {
		try {
			ContentValues values = new ContentValues(3);
			values.put(TripsTable.COLUMN_NAME, dir.getCanonicalPath());
			values.put(TripsTable.COLUMN_FROM, from.getTime());
			values.put(TripsTable.COLUMN_TO, to.getTime());
			return values;
		}
		catch (IOException e) {
			return null;
		}
	}
	
	private final void updateTripPrice(final TripRow trip) {
		try {
			_areTripsValid = false;
			final String dir = trip.dir.getCanonicalPath();
			final SQLiteDatabase db = this.getReadableDatabase();
			final Cursor c = db.query(ReceiptsTable.TABLE_NAME, new String[] {"SUM(" + ReceiptsTable.COLUMN_PRICE + ")"}, 
					ReceiptsTable.COLUMN_PARENT + "= ? AND " + ReceiptsTable.COLUMN_EXPENSEABLE + " = 1", new String[] {dir}, null, null, null);
			if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
				final String sum = c.getString(0);
				c.close();
				ContentValues values = new ContentValues(1);
				values.put(TripsTable.COLUMN_PRICE, sum);
				db.update(TripsTable.TABLE_NAME, values, TripsTable.COLUMN_NAME + " = ?", new String[] {dir});
			}
		}
		catch (IOException e) {}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//	ReceiptRow Methods
	////////////////////////////////////////////////////////////////////////////////////////////////////
	public final ReceiptRow[] getReceipts(final TripRow trip) {
		if (_receiptMapCache.containsKey(trip)) //only cache the default way (otherwise we get into issues with asc v desc)
			return _receiptMapCache.get(trip);
		return this.getReceipts(trip, true);
	}
	
	public final ReceiptRow[] getReceipts(final TripRow trip, final boolean desc) {
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
			final Cursor c = db.query(ReceiptsTable.TABLE_NAME, null, 
					ReceiptsTable.COLUMN_PARENT + "= ?", new String[] {trip.dir.getCanonicalPath()}, null, null, ReceiptsTable.COLUMN_DATE + ((desc)?" DESC":" ASC"));
			if (c != null && c.moveToFirst()) {
				ReceiptRow[] receipts = new ReceiptRow[c.getCount()];
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
				do {
					final int id = c.getInt(idIndex);
					final String path = c.getString(pathIndex);
					final String name = c.getString(nameIndex);
					final String parent = c.getString(parentIndex);
					final String category = c.getString(categoryIndex);
					final String price = c.getString(priceIndex);
					final long date = c.getLong(dateIndex);
					final String comment = c.getString(commentIndex);
					final boolean expensable = c.getInt(expenseableIndex)>0;
					final String currency = c.getString(currencyIndex);
					final boolean fullpage = !(c.getInt(fullpageIndex)>0);
					receipts[c.getPosition()] = new ReceiptRow(id, path, parent, name, category, date, comment, price, expensable, currency, fullpage);
				} 
				while (c.moveToNext());
				c.close(); //Be sure to close the cursor to avoid memory leaks
				if (desc) _receiptMapCache.put(trip, receipts);  //Don't Cache the EmailWriterVariety
				return receipts;
			}
			else {
				c.close(); //Be sure to close the cursor to avoid memory leaks
				return new ReceiptRow[0];
			}
		}
		catch (IOException e) {
			return new ReceiptRow[0];
		}
	}
	
	public final ReceiptRow getReceiptByID(final int id) {
		if (id <= 0)
			return null;
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor c = db.query(ReceiptsTable.TABLE_NAME, null, ReceiptsTable.COLUMN_ID + "= ?", new String[] {Integer.toString(id)}, null, null, null);
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
			final String path = c.getString(pathIndex);
			final String name = c.getString(nameIndex);
			final String parent = c.getString(parentIndex);
			final String category = c.getString(categoryIndex);
			final String price = c.getString(priceIndex);
			final long date = c.getLong(dateIndex);
			final String comment = c.getString(commentIndex);
			final boolean expensable = c.getInt(expenseableIndex)>0;
			final String currency = c.getString(currencyIndex);
			final boolean fullpage = !(c.getInt(fullpageIndex)>0);
			c.close(); //Be sure to close the cursor to avoid memory leaks
			return new ReceiptRow(id, path, parent, name, category, date, comment, price, expensable, currency, fullpage);
		}
		else {
			c.close(); //Be sure to close the cursor to avoid memory leaks
			return null;
		}
	}
	
	public final ReceiptRow insertReceiptFile(final TripRow trip, final File img, final File parentDir, final String name, final String category, 
			final Date date, final String comment, final String price, final boolean expensable, final String currency, final boolean fullpage) throws SQLException {
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
			final int rcptCnt = this.getReceipts(trip).length; //Use this to order things more properly
			ContentValues values = new ContentValues(7);
			if (img == null)
				values.put(ReceiptsTable.COLUMN_PATH, NULL);
			else
				values.put(ReceiptsTable.COLUMN_PATH, img.getCanonicalPath());
			values.put(ReceiptsTable.COLUMN_PARENT, parentDir.getCanonicalPath());
			if (name.length() > 0)
				values.put(ReceiptsTable.COLUMN_NAME, name);
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
			if (db.insertOrThrow(ReceiptsTable.TABLE_NAME, null, values) == -1)
				return null;
			else {
				this.updateTripPrice(trip);
				if (_receiptMapCache.containsKey(trip))
					_receiptMapCache.remove(trip);
				final Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);
				if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
					final int id = c.getInt(0);
					c.close();
					date.setTime(date.getTime()+rcptCnt);
					return new ReceiptRow(id, img, parentDir, name, category, date, comment, price, expensable, currency, fullpage);
				}
				else {
					c.close();
					return null;
				}
			}
		}
		catch (IOException e) {
			return null;
		}
	}
	
	public final ReceiptRow updateReceipt(final ReceiptRow oldReceipt, final TripRow trip, final String name, final String category, 
			final Date date, final String comment, final String price, final boolean expensable, final String currency, final boolean fullpage) {
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
			ContentValues values = new ContentValues(8);
			values.put(ReceiptsTable.COLUMN_NAME, name);
			values.put(ReceiptsTable.COLUMN_CATEGORY, category);
			if ((date.getTime() % 3600000) == 0)
				values.put(ReceiptsTable.COLUMN_DATE, date.getTime() + oldReceipt.id);
			else
				values.put(ReceiptsTable.COLUMN_DATE, date.getTime());
			values.put(ReceiptsTable.COLUMN_COMMENT, comment);
			values.put(ReceiptsTable.COLUMN_PRICE, price);
			values.put(ReceiptsTable.COLUMN_EXPENSEABLE, expensable);
			values.put(ReceiptsTable.COLUMN_ISO4217, currency);
			values.put(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE, !fullpage);
			if (values == null || (db.update(ReceiptsTable.TABLE_NAME, values, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(oldReceipt.id)}) == 0))
				return null;
			else {
				_receiptMapCache.remove(trip);
				this.updateTripPrice(trip);
				return new ReceiptRow(oldReceipt.id, oldReceipt.img, oldReceipt.parentDir, name, category, date, comment, price, expensable, currency, fullpage);
			}
		}
		catch (SQLException e) {
			return null;
		}
	}
	
	public final ReceiptRow updateReceiptImg(final ReceiptRow oldReceipt, final File img) {
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
			ContentValues values = new ContentValues(1);
			if (img == null)
				values.put(ReceiptsTable.COLUMN_PATH, NULL);
			else
				values.put(ReceiptsTable.COLUMN_PATH, img.getCanonicalPath());
			if (values == null || (db.update(ReceiptsTable.TABLE_NAME, values, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(oldReceipt.id)}) == 0))
				return null;
			else {
				oldReceipt.img = img;
				return oldReceipt;
			}
		}
		catch (SQLException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	public final boolean deleteReceipt(final ReceiptRow receipt, final TripRow currentTrip) {
		final SQLiteDatabase db = this.getReadableDatabase();
		final boolean success = (db.delete(ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(receipt.id)}) > 0); 
		if (success) {
			this.updateTripPrice(currentTrip);
			_receiptMapCache.remove(currentTrip);
		}
		return success;
	}
	
	public final boolean moveReceiptUp(final TripRow trip, final ReceiptRow receipt) {
		ReceiptRow[] receipts = getReceipts(trip);
		int index = 0;
		for (int i =0; i < receipts.length; i++) {
			if (receipt == receipts[i]) {
				index = i-1;
				break;
			}
		}
		if (index < 0)
			return false;
		ReceiptRow up = receipts[index];
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
			ContentValues upValues = new ContentValues(1);
			ContentValues downValues = new ContentValues(1);
			upValues.put(ReceiptsTable.COLUMN_DATE, receipt.date.getTime());
			downValues.put(ReceiptsTable.COLUMN_DATE, up.date.getTime());
			if ((db.update(ReceiptsTable.TABLE_NAME, upValues, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(up.id)}) == 0))
				return false;
			if ((db.update(ReceiptsTable.TABLE_NAME, downValues, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(receipt.id)}) == 0))
				return false;
			_receiptMapCache.remove(trip);
			return true;
		}
		catch (SQLException e) {
			return false;
		}
	}
	
	public final boolean moveReceiptDown(final TripRow trip, final ReceiptRow receipt) {
		ReceiptRow[] receipts = getReceipts(trip);
		int index = receipts.length-1;
		for (int i =0; i < receipts.length; i++) {
			if (receipt == receipts[i]) {
				index = i+1;
				break;
			}
		}
		if (index > (receipts.length-1))
			return false;
		ReceiptRow down = receipts[index];
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
			ContentValues upValues = new ContentValues(1);
			ContentValues downValues = new ContentValues(1);
			upValues.put(ReceiptsTable.COLUMN_DATE, down.date.getTime());
			downValues.put(ReceiptsTable.COLUMN_DATE, receipt.date.getTime());
			if ((db.update(ReceiptsTable.TABLE_NAME, upValues, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(receipt.id)}) == 0))
				return false;
			if ((db.update(ReceiptsTable.TABLE_NAME, downValues, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(down.id)}) == 0))
				return false;
			_receiptMapCache.remove(trip);
			return true;
		}
		catch (SQLException e) {
			return false;
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
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor c = db.query(CategoriesTable.TABLE_NAME, null, null, null, null, null, null);
		if (c != null && c.moveToFirst()) {
			final int nameIndex = c.getColumnIndex(CategoriesTable.COLUMN_NAME);
			final int codeIndex = c.getColumnIndex(CategoriesTable.COLUMN_CODE);
			do {
				final String name = c.getString(nameIndex);
				final String code = c.getString(codeIndex);
				_categories.put(name, code);
			} 
			while (c.moveToNext());
			c.close(); //Be sure to close the cursor to avoid memory leaks
		}
		else {
			c.close(); //Be sure to close the cursor to avoid memory leaks
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
		Collections.sort(_currencyList, _charSequenceComparator);
		return _currencyList;
	}
	
	public final boolean insertCategory(final String name, final String code) throws SQLException {
		final SQLiteDatabase db = this.getReadableDatabase();
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
	
	public final boolean updateCategory(final String oldName, final String newName, final String newCode) {
		final SQLiteDatabase db = this.getReadableDatabase();
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
	
	public final boolean deleteCategory(final String name) {
		final SQLiteDatabase db = this.getReadableDatabase();
		final boolean success = (db.delete(CategoriesTable.TABLE_NAME, CategoriesTable.COLUMN_NAME + " = ?", new String[] {name}) > 0); 
		if (success) {
			_categories.remove(name);
			_categoryList.remove(name);
		}
		return success;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//	CSV Column Methods
	////////////////////////////////////////////////////////////////////////////////////////////////////
	public final CSVColumns getCSVColumns() {
		if (_csvColumns != null)
			return _csvColumns;
		_csvColumns = new CSVColumns(_activity, this);
		final SQLiteDatabase db = this.getReadableDatabase();
		final Cursor c = db.query(CSVTable.TABLE_NAME, null, null, null, null, null, null);
		if (c != null && c.moveToFirst()) {
			final int idxIndex = c.getColumnIndex(CSVTable.COLUMN_ID);
			final int typeIndex = c.getColumnIndex(CSVTable.COLUMN_TYPE);
			do {
				final int index = c.getInt(idxIndex);
				final String type = c.getString(typeIndex);
				_csvColumns.add(index, type);
			} 
			while (c.moveToNext());
			c.close(); //Be sure to close the cursor to avoid memory leaks
		}
		else {
			c.close(); //Be sure to close the cursor to avoid memory leaks
		}
		return _csvColumns;
	}
	
	public final boolean insertCSVColumn() {
		final SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues(1);
		values.put(CSVTable.COLUMN_TYPE, CSVColumns.BLANK);
		if (db.insertOrThrow(CSVTable.TABLE_NAME, null, values) == -1)
			return false;
		else {
			final Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);
			if (c != null && c.moveToFirst() && c.getColumnCount() > 0) {
				final int idx = c.getInt(0);
				c.close();
				_csvColumns.add(idx, CSVColumns.BLANK);
			}
			else {
				c.close();
				return false;
			}
			return true;
		}
	}
	
	public final boolean deleteCSVColumn() {
		final SQLiteDatabase db = this.getReadableDatabase();
		int idx = _csvColumns.removeLast();
		if (idx < 0)
			return false;
		return db.delete(CSVTable.TABLE_NAME, CSVTable.COLUMN_ID + " = ?", new String[] {Integer.toString(idx)}) > 0; 
	}
	
	public final boolean updateCSVColumn(int arrayListIndex, int optionIndex) { //Note index here refers to the actual index and not the ID
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
			ContentValues values = new ContentValues(1);
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
	
	////////////////////////////////////////////////////////////////////////////////////////////////////
	//	Debug Methods
	////////////////////////////////////////////////////////////////////////////////////////////////////
	final void debugClearTables() {
		if (!D) return; //Only allow this if we're in debug mode
		_areTripsValid = false;
		_receiptMapCache.clear();
		final SQLiteDatabase db = this.getReadableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + TripsTable.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ReceiptsTable.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_NAME);
		onCreate(db);
	}

}