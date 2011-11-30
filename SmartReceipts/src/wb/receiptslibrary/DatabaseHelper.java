package wb.receiptslibrary;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;

public final class DatabaseHelper extends SQLiteOpenHelper {

	//Logging Vars
	private static final boolean D = false;
	private static final String TAG = "DatabaseHelper";
	
	//Database Info
	private static final String DATABASE_NAME = "receipts.db";
	private static final int DATABASE_VERSION = 1;
	static final String NULL = "null";
	
	//Caching Vars
	private TripRow[] _tripsCache;
	private boolean _areTripsValid;
	private HashMap<TripRow, ReceiptRow[]> _receiptMapCache;
	private HashMap<String, String> _categories;
	private ArrayList<CharSequence> _categoryList;
	private Time _now;
	
	//Tables Declarations
	private static final class TripsTable {
		private TripsTable() {}	
		public static final String TABLE_NAME = "trips";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_FROM = "from_date";
		public static final String COLUMN_TO = "to_date";
		public static final String COLUMN_PRICE = "price";
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
		public static final String COLUMN_FULLPAGEIMAGE = "fullpageimage";
	}
	private static final class CategoriesTable {
		private CategoriesTable() {}
		public static final String TABLE_NAME = "categories";
		public static final String COLUMN_NAME = "name";
		public static final String COLUMN_CODE = "code";
	}
	
	public DatabaseHelper(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION); //Requests the default cursor factory
		_areTripsValid = false;
		_receiptMapCache = new HashMap<TripRow, ReceiptRow[]>();
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
				+ TripsTable.COLUMN_PRICE + " DECIMAL(10, 2) DEFAULT 0.00"
				+ ");";
		final String receipts = "CREATE TABLE " + ReceiptsTable.TABLE_NAME + " ("
				+ ReceiptsTable.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ ReceiptsTable.COLUMN_PATH + " TEXT, "
				+ ReceiptsTable.COLUMN_PARENT + " TEXT REFERENCES " + TripsTable.TABLE_NAME + " ON DELETE CASCADE, "
				+ ReceiptsTable.COLUMN_NAME + " TEXT DEFAULT \"New Receipt\", "
				+ ReceiptsTable.COLUMN_CATEGORY + " TEXT, "
				+ ReceiptsTable.COLUMN_DATE + " DATE DEFAULT (DATE('now', 'localtime')), "
				+ ReceiptsTable.COLUMN_COMMENT + " TEXT, "
				+ ReceiptsTable.COLUMN_PRICE + " DECIMAL(10, 2) DEFAULT 0.00, "
				+ ReceiptsTable.COLUMN_EXPENSEABLE + " BOOLEAN DEFAULT 1, "
				+ ReceiptsTable.COLUMN_FULLPAGEIMAGE + " BOOLEAN DEFAULT 1"
				+ ");";
		final String categories = "CREATE TABLE " + CategoriesTable.TABLE_NAME + " ("
				+ CategoriesTable.COLUMN_NAME + " TEXT PRIMARY KEY, "
				+ CategoriesTable.COLUMN_CODE + " TEXT"
				+ ");";
		if (D) Log.d(TAG, trips);
		if (D) Log.d(TAG, receipts);
		if (D) Log.d(TAG, categories);
		db.execSQL(trips);
		db.execSQL(receipts);
		db.execSQL(categories);
		//********** Insert Default Category Entries Into Categories Table*********//
		final String header = "INSERT INTO " + CategoriesTable.TABLE_NAME + " VALUES (";
		db.execSQL(header + "\"<Category>\", \"NUL\");");
		db.execSQL(header + "\"Airfare\", \"AIRP\");");
		db.execSQL(header + "\"Breakfast\", \"BRFT\");");
		db.execSQL(header + "\"Dinner\", \"DINN\");");
		db.execSQL(header + "\"Entertainment\", \"ENT\");");
		db.execSQL(header + "\"Gasoline\", \"GAS\");");
		db.execSQL(header + "\"Gift\", \"GIFT\");");
		db.execSQL(header + "\"Hotel\", \"HTL\");");
		db.execSQL(header + "\"Laundry\", \"LAUN\");");
		db.execSQL(header + "\"Lunch\", \"LNCH\");");
		db.execSQL(header + "\"Other\", \"MISC\");");
		db.execSQL(header + "\"Parking/Tools\", \"PARK\");");
		db.execSQL(header + "\"Postage/Shipping\", \"POST\");");
		db.execSQL(header + "\"Car Rental\", \"RCAR\");");
		db.execSQL(header + "\"Taxi/Bus\", \"TAXI\");");
		db.execSQL(header + "\"Telephone/Fax\", \"TELE\");");
		db.execSQL(header + "\"Tip\", \"TIP\");");
		db.execSQL(header + "\"Train\", \"TRN\");");
		db.execSQL(header + "\"Books/Periodicals\", \"ZBKP\");");
		db.execSQL(header + "\"Cell Phone\", \"ZCEL\");");
		db.execSQL(header + "\"Dues/Subscriptions\", \"ZDUE\");");
		db.execSQL(header + "\"Meals (Justified)\", \"ZMEO\");");
		db.execSQL(header + "\"Stantionery/Stations\", \"ZSTS\");");
		db.execSQL(header + "\"Training Fees\", \"ZTRN\");");
	}
	
	@Override
	public final void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		if(D) Log.d(TAG, "Upgrading the database from version " + oldVersion + " to " + newVersion);
		//TODO: Do this is place instead of dropping the table
		db.execSQL("DROP TABLE IF EXISTS " + TripsTable.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ReceiptsTable.TABLE_NAME);
		onCreate(db);
	}
	
	public final void onDestroy() {
		final SQLiteDatabase db = this.getReadableDatabase();
		db.close();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////
	//	TripRow Methods
	////////////////////////////////////////////////////////////////////////////////////////////////////
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
				trips[c.getPosition()] = new TripRow(name, from, to, price);
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
	
	//Returns the trip on success. Null otherwise
	public final TripRow insertTrip(final File dir, final Date from, final Date to) throws SQLException {
		final SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = this.getTripContentValues(dir, from, to);
		if (values == null || db.insertOrThrow(TripsTable.TABLE_NAME, null, values) == -1)
			return null;
		else {
			_areTripsValid = false;
			return new TripRow(dir, from, to);
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
					ContentValues rcptVals = new ContentValues();
					rcptVals.put(ReceiptsTable.COLUMN_PARENT, dir.getCanonicalPath());
					db.update(ReceiptsTable.TABLE_NAME, rcptVals, ReceiptsTable.COLUMN_PARENT + " = ?", new String[] {oldTrip.dir.getCanonicalPath()}); //Consider rollback here
				}
				return new TripRow(dir, from, to);
			}
		}
		catch (IOException e) {
			return null;
		}
		catch (SQLException e) {
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
					receipts[c.getPosition()] = new ReceiptRow(id, path, parent, name, category, date, comment, price, expensable);
				} 
				while (c.moveToNext());
				c.close(); //Be sure to close the cursor to avoid memory leaks
				_receiptMapCache.put(trip, receipts);
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
	
	public final ReceiptRow insertReceiptFile(final TripRow trip, final File img, final File parentDir, final String name, 
			final String category, final Date date, final String comment, final String price, final boolean expensable) throws SQLException {
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
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
				values.put(ReceiptsTable.COLUMN_DATE, date.getTime());
			values.put(ReceiptsTable.COLUMN_COMMENT, comment);
			values.put(ReceiptsTable.COLUMN_EXPENSEABLE, expensable);
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
					return new ReceiptRow(id, img, parentDir, name, category, date, comment, price, expensable);
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
	
	public final ReceiptRow updateReceipt(final ReceiptRow oldReceipt, final TripRow trip, final String name, 
			final String category, final Date date, final String comment, final String price, final boolean expensable) {
		try {
			final SQLiteDatabase db = this.getReadableDatabase();
			ContentValues values = new ContentValues(5);
			values.put(ReceiptsTable.COLUMN_NAME, name);
			values.put(ReceiptsTable.COLUMN_CATEGORY, category);
			values.put(ReceiptsTable.COLUMN_DATE, date.getTime());
			values.put(ReceiptsTable.COLUMN_COMMENT, comment);
			values.put(ReceiptsTable.COLUMN_PRICE, price);
			values.put(ReceiptsTable.COLUMN_EXPENSEABLE, expensable);
			if (values == null || (db.update(ReceiptsTable.TABLE_NAME, values, ReceiptsTable.COLUMN_ID + " = ?", new String[] {Integer.toString(oldReceipt.id)}) == 0))
				return null;
			else {
				_receiptMapCache.remove(trip);
				this.updateTripPrice(trip);
				return new ReceiptRow(oldReceipt.id, oldReceipt.img, oldReceipt.parentDir, name, category, date, comment, price, expensable);
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