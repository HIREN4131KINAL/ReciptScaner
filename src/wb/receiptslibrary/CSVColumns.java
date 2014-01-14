package wb.receiptslibrary;

import java.util.ArrayList;

import wb.android.flex.Flex;
import wb.receiptslibrary.model.ReceiptRow;
import wb.receiptslibrary.model.TripRow;
import wb.receiptslibrary.persistence.DatabaseHelper;
import wb.receiptslibrary.persistence.PersistenceManager;
import android.content.Context;
import android.widget.ArrayAdapter;

public class CSVColumns {
	
	private static final String BLANK = "Blank Column";
	private static final String CATEGORY_CODE = "Category Code";
	private static final String CATEGORY_NAME = "Category Name";
	private static final String USER_ID = "User ID";
	private static final String REPORT_NAME = "Report Name";
	private static final String REPORT_START_DATE = "Report Start Date";
	private static final String REPORT_END_DATE = "Report End Date";
	private static final String IMAGE_FILE_NAME = "Image Name";
	private static final String IMAGE_PATH = "Image Path";
	private static String COMMENT = null;
	private static String CURRENCY = null;
	private static String DATE = null;
	private static String NAME = null;
	private static String PRICE = null;
	private static String TAX = null;
	private static String EXTRA_EDITTEXT_1 = null;
	private static String EXTRA_EDITTEXT_2 = null;
	private static String EXTRA_EDITTEXT_3= null;
		
	private final ArrayList<CSVColumn> mCSVColumns;
	private final DatabaseHelper mDB;
	private final ArrayList<CharSequence> mOptions;
	private final Context mContext;
	private final PersistenceManager mPersistenceManager;
	
	public CSVColumns(Context context, DatabaseHelper db, Flex flex, PersistenceManager persistenceManager) {
		mContext = context;
		mCSVColumns = new ArrayList<CSVColumn>();
		mDB = db;
		mPersistenceManager = persistenceManager;
		mOptions = getOptionsList(context, flex);
		init(context, flex);
	}
	
	public CSVColumns(Context context, SmartReceiptsApplication application) {
		mContext = context;
		mCSVColumns = new ArrayList<CSVColumn>();
		mPersistenceManager = application.getPersistenceManager();
		mDB = mPersistenceManager.getDatabase();
		mOptions = getOptionsList(context, application.getFlex());
		init(context, application.getFlex());
	}
	
	private void init(Context context, Flex flex) {
		COMMENT = flex.getString(context, R.string.RECEIPTMENU_FIELD_COMMENT);
		CURRENCY = flex.getString(context, R.string.RECEIPTMENU_FIELD_CURRENCY);
		DATE = flex.getString(context, R.string.RECEIPTMENU_FIELD_DATE);
		NAME = flex.getString(context, R.string.RECEIPTMENU_FIELD_NAME);
		PRICE = flex.getString(context, R.string.RECEIPTMENU_FIELD_PRICE);
		TAX = flex.getString(context, R.string.RECEIPTMENU_FIELD_TAX);
		EXTRA_EDITTEXT_1 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1);
		EXTRA_EDITTEXT_2 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2);
		EXTRA_EDITTEXT_3 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);
	}
	
	public final void add() {
		mCSVColumns.add(new CSVColumn(mCSVColumns.size() + 1, BLANK));
	}
	
	public final void add(int index, String columnType) {
		mCSVColumns.add(new CSVColumn(index, columnType));
	}
	
	public final void add(String columnType) {
		mCSVColumns.add(new CSVColumn(mCSVColumns.size() + 1, columnType));
	}
	
	public final CSVColumn update(int arrayListIndex, int optionIndex) {
		CSVColumn column = mCSVColumns.get(arrayListIndex);
		if (optionIndex < mOptions.size()) //No customizations added
			column.columnType = (String) mOptions.get(optionIndex);
		return column;
	}
	
	public final int removeLast() {
		if (!mCSVColumns.isEmpty()) {
			CSVColumn column = mCSVColumns.remove(mCSVColumns.size() - 1);
			return column.index;
		}
		else
			return -1;
	}
	
	public final String print(ReceiptRow receipt, TripRow currentTrip) { 
		final int size = mCSVColumns.size();
		String print = "";
		for (int i=0; i < size; i++) {
			print += build(mCSVColumns.get(i), receipt, currentTrip);
			if (i == (size - 1))
				print += "\n";
			else
				print += ",";
		}
		return print;
	}
	
	public final String printHeaders() { 
		final int size = mCSVColumns.size();
		String print = "";
		for (int i=0; i < size; i++) {
			print += mCSVColumns.get(i).columnType;
			if (i == (size - 1))
				print += "\n";
			else
				print += ",";
		}
		return print;
	}
	
	private static final String QUOTE = "\"";
	private static final String ESCAPED_QUOTE = "\"\"";
	private static final String[] STRINGS_THAT_MUST_BE_QUOTED = { ",", "\"", "\n", "\r\n" };
	private final String build(CSVColumn column, ReceiptRow receipt, TripRow currentTrip) {
		String csv;		
		if (column.columnType.equals(BLANK))
			csv = "";
		else if (column.columnType.equals(CATEGORY_CODE))
			csv = mDB.getCategoryCode(receipt.getCategory());
		else if (column.columnType.equals(CATEGORY_NAME))
			csv = receipt.getCategory();
		else if (column.columnType.equals(COMMENT))
			csv = receipt.getComment();
		else if (column.columnType.equals(CURRENCY))
			csv = receipt.getCurrencyCode(); 
		else if (column.columnType.equals(DATE))
			csv = receipt.getFormattedDate(mContext, mPersistenceManager.getPreferences().getDateSeparator());
		else if (column.columnType.equals(NAME))
			csv = receipt.getName();
		else if (column.columnType.equals(PRICE))
			csv = receipt.getDecimalFormattedPrice();
		else if (column.columnType.equals(TAX))
			csv = receipt.getDecimalFormattedTax();
		else if (column.columnType.equals(REPORT_NAME))
			csv = currentTrip.getName();
		else if (column.columnType.equals(REPORT_START_DATE))
			csv = currentTrip.getFormattedStartDate(mContext, mPersistenceManager.getPreferences().getDateSeparator());
		else if (column.columnType.equals(REPORT_END_DATE))
			csv = currentTrip.getFormattedEndDate(mContext, mPersistenceManager.getPreferences().getDateSeparator());
		else if (column.columnType.equals(USER_ID))
			csv = mPersistenceManager.getPreferences().getUserID();
		else if (column.columnType.equals(IMAGE_FILE_NAME))
			csv = (receipt.getImage() == null) ? "" : receipt.getImage().getName();
		else if (column.columnType.equals(IMAGE_PATH))
			csv = (receipt.getImage() == null) ? "" : receipt.getImage().getAbsolutePath();
		else if (column.columnType.equalsIgnoreCase(EXTRA_EDITTEXT_1))
			csv = receipt.getExtraEditText1();
		else if (column.columnType.equalsIgnoreCase(EXTRA_EDITTEXT_2))
			csv = receipt.getExtraEditText2();
		else if (column.columnType.equalsIgnoreCase(EXTRA_EDITTEXT_3))
			csv = receipt.getExtraEditText3();
		else
			csv = column.columnType;
		if (csv == null)
			return "";
		if (csv.contains(QUOTE))
			csv.replace(QUOTE, ESCAPED_QUOTE);
		for (int i = 0; i < STRINGS_THAT_MUST_BE_QUOTED.length; i++) {
			if (csv.contains(STRINGS_THAT_MUST_BE_QUOTED[i])) {
				csv = QUOTE + csv + QUOTE;
				break;
			}
		}
		return csv;
	}
	
	public final int size() {
		return mCSVColumns.size();
	}
	
	public final boolean isEmpty() {
		return mCSVColumns.isEmpty();
	}
	
	public final String getType(int columnIndex) {
		if (columnIndex >= 0 && columnIndex < mCSVColumns.size())
			return mCSVColumns.get(columnIndex).columnType;
		else
			return "";
	}
	
	private static final ArrayList<CharSequence> getOptionsList(Context context, Flex flex) {
		ArrayList<CharSequence> options = new ArrayList<CharSequence>(10);
		if (COMMENT == null) COMMENT = flex.getString(context, R.string.RECEIPTMENU_FIELD_COMMENT);
		if (CURRENCY == null) CURRENCY = flex.getString(context, R.string.RECEIPTMENU_FIELD_CURRENCY);
		if (DATE == null) DATE = flex.getString(context, R.string.RECEIPTMENU_FIELD_DATE);
		if (NAME == null) NAME = flex.getString(context, R.string.RECEIPTMENU_FIELD_NAME);
		if (PRICE == null) PRICE = flex.getString(context, R.string.RECEIPTMENU_FIELD_PRICE);
		if (TAX == null) TAX = flex.getString(context, R.string.RECEIPTMENU_FIELD_TAX);
		if (EXTRA_EDITTEXT_1 == null) EXTRA_EDITTEXT_1 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1);
		if (EXTRA_EDITTEXT_2 == null) EXTRA_EDITTEXT_2 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2);
		if (EXTRA_EDITTEXT_3 == null) EXTRA_EDITTEXT_3 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);
		options.add(BLANK);
		options.add(CATEGORY_CODE);
		options.add(CATEGORY_NAME);
		options.add(COMMENT);
		options.add(CURRENCY);
		options.add(DATE);
		options.add(IMAGE_FILE_NAME);
		options.add(IMAGE_PATH);
		options.add(NAME);
		options.add(PRICE);
		options.add(REPORT_NAME);
		options.add(REPORT_START_DATE);
		options.add(REPORT_END_DATE);
		options.add(TAX);
		options.add(USER_ID);
		if (EXTRA_EDITTEXT_1.length() > 0) options.add(EXTRA_EDITTEXT_1);
		if (EXTRA_EDITTEXT_2.length() > 0) options.add(EXTRA_EDITTEXT_2);
		if (EXTRA_EDITTEXT_3.length() > 0) options.add(EXTRA_EDITTEXT_3);
		return options;
	}
	
	public static final ArrayAdapter<CharSequence> getNewArrayAdapter(Context context, Flex flex) {
		return new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, getOptionsList(context, flex));
	}
	
	public static final String BLANK(Flex flex) {
		return BLANK;
	}
	
	public static final String CATEGORY_CODE(Flex flex) {
		return CATEGORY_CODE;
	}
	
	public static final String CATEGORY_NAME(Flex flex) {
		return CATEGORY_NAME;
	}
	
	public static final String USER_ID(Flex flex) {
		return USER_ID;
	}
	
	public static final String EXPENSE_REPORT_NAME(Flex flex) {
		return REPORT_NAME;
	}
	
	public static final String EXPENSE_REPORT_START(Flex flex) {
		return REPORT_START_DATE;
	}
	
	public static final String EXPENSE_REPORT_END(Flex flex) {
		return REPORT_END_DATE;
	}

	public static final String COMMENT(Context context, Flex flex) {
		if (COMMENT == null) {
			try {
				COMMENT = flex.getString(context, R.string.RECEIPTMENU_FIELD_COMMENT);
			}
			catch (Exception e) {
				COMMENT = "Comment"; // Fallback for select 2.X phones that crash under application context
			}
		}
		return COMMENT;
	}
	
	public static final String CURRENCY(Context context, Flex flex) {
		if (CURRENCY == null) {
			try {
				CURRENCY = flex.getString(context, R.string.RECEIPTMENU_FIELD_CURRENCY);
			}
			catch (Exception e) {
				CURRENCY = "Currency"; // Fallback for select 2.X phones that crash under application context
			}
		}
		return CURRENCY;
	}
	
	public static final String DATE(Context context, Flex flex) {
		if (DATE == null) {
			try {
				DATE = flex.getString(context, R.string.RECEIPTMENU_FIELD_DATE);
			}
			catch (Exception e) {
				DATE = "Date"; // Fallback for select 2.X phones that crash under application context
			}
			
		} 
		return DATE;
	}
	
	public static final String NAME(Context context, Flex flex) {
		if (NAME == null) {
			try {
				NAME = flex.getString(context, R.string.RECEIPTMENU_FIELD_NAME);
			}
			catch (Exception e) {
				NAME = "Name"; // Fallback for select 2.X phones that crash under application context
			}
			
		} 
		return NAME;
	}
	
	public static final String PRICE(Context context, Flex flex) {
		if (PRICE == null) {
			try {
				PRICE = flex.getString(context, R.string.RECEIPTMENU_FIELD_PRICE);
			}
			catch (Exception e) {
				PRICE = "Price"; // Fallback for select 2.X phones that crash under application context
			}
			
		} 
		return PRICE;
	}
	
	public static final String TAX(Context context, Flex flex) {
		if (TAX == null) {
			try {
				TAX = flex.getString(context, R.string.RECEIPTMENU_FIELD_TAX);
			}
			catch (Exception e) {
				TAX = "Tax"; // Fallback for select 2.X phones that crash under application context
			}
			
		} 
		return TAX;
	}
	
	public static final String EXTRA_EDITTEXT_1(Context context, Flex flex) {
		if (EXTRA_EDITTEXT_1 == null) EXTRA_EDITTEXT_1 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1);
		return EXTRA_EDITTEXT_1;
	}
	
	public static final String EXTRA_EDITTEXT_2(Context context, Flex flex) {
		if (EXTRA_EDITTEXT_2 == null) EXTRA_EDITTEXT_2 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2);
		return EXTRA_EDITTEXT_2;
	}
	
	public static final String EXTRA_EDITTEXT_3(Context context, Flex flex) {
		if (EXTRA_EDITTEXT_3 == null) EXTRA_EDITTEXT_3 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);
		return EXTRA_EDITTEXT_3;
	}

}