package co.smartreceipts.android.model;

import java.util.ArrayList;

import wb.android.flex.Flex;
import android.content.Context;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;

/**
 * Defines columns that will be used for CSV and PDF output
 * @author wrb
 *
 */
public class Columns {

	/**
	 * Redo this - Using Column Names as static is bad, since they must all be initialized from the Strings File
	 * @author wrb
	 *
	 */
	public static final class ColumnName {
		public static String BLANK = "Blank Column";
		public static String CATEGORY_CODE = null;
		public static String CATEGORY_NAME = null;
		public static String USER_ID = null;
		public static String REPORT_NAME = null;
		public static String REPORT_START_DATE = null;
		public static String REPORT_END_DATE = null;
		public static String REPORT_COMMENT = null;
        public static String REPORT_COST_CENTER = null;
		public static String IMAGE_FILE_NAME = null;
		public static String IMAGE_PATH = null;
		public static String COMMENT = null;
		public static String CURRENCY = null;
		public static String DATE = null;
		public static String NAME = null;
		public static String PRICE = null;
		public static String TAX = null;
		public static String PICTURED = null;
		public static String EXPENSABLE = null;
		public static String INDEX = null;
		public static String ID = null;
		public static String PAYMENT_METHOD = null;
		public static String EXTRA_EDITTEXT_1 = null;
		public static String EXTRA_EDITTEXT_2 = null;
		public static String EXTRA_EDITTEXT_3= null;
	}

	protected final ArrayList<Column> mColumns;
	private final ArrayList<CharSequence> mOptions;
	private final Context mContext;
	private final PersistenceManager mPersistenceManager;

	public Columns(Context context, DatabaseHelper db, Flex flex, PersistenceManager persistenceManager) {
		mContext = context;
		mColumns = new ArrayList<Column>();
		mPersistenceManager = persistenceManager;
		init(context, flex); // Must be present before generate options list
		mOptions = generateOptionsList(context, flex);
	}

	public Columns(Context context, SmartReceiptsApplication application) {
		mContext = context;
		mColumns = new ArrayList<Column>();
		mPersistenceManager = application.getPersistenceManager();
		init(context, application.getFlex()); // Must be present before generate options list
		mOptions = generateOptionsList(context, application.getFlex());
	}

	private void init(Context context, Flex flex) {
		/*
		 * Do to the way flex works atm, some of these have to point to the raw name (i.e. R.string.RECEIPTMENU*)
		 * As opposed to the current approach (R.string.column_item_*). Keep this in mind for future changes
		 */
		ColumnName.BLANK = flex.getString(context, R.string.column_item_blank);
		ColumnName.CATEGORY_CODE = flex.getString(context, R.string.column_item_category_code);
		ColumnName.CATEGORY_NAME = flex.getString(context, R.string.column_item_category_name);
		ColumnName.USER_ID = flex.getString(context, R.string.column_item_user_id);
		ColumnName.REPORT_NAME = flex.getString(context, R.string.column_item_report_name);
		ColumnName.REPORT_START_DATE = flex.getString(context, R.string.column_item_report_start_date);
		ColumnName.REPORT_END_DATE = flex.getString(context, R.string.column_item_report_end_date);
		ColumnName.REPORT_COMMENT = flex.getString(context, R.string.column_item_report_comment);
        ColumnName.REPORT_COST_CENTER = flex.getString(context, R.string.column_item_report_cost_center);
		ColumnName.IMAGE_FILE_NAME = flex.getString(context, R.string.column_item_image_file_name);
		ColumnName.IMAGE_PATH = flex.getString(context, R.string.column_item_image_path);
		ColumnName.COMMENT = flex.getString(context, R.string.RECEIPTMENU_FIELD_COMMENT);
		ColumnName.CURRENCY = flex.getString(context, R.string.RECEIPTMENU_FIELD_CURRENCY);
		ColumnName.DATE = flex.getString(context, R.string.RECEIPTMENU_FIELD_DATE);
		ColumnName.NAME = flex.getString(context, R.string.RECEIPTMENU_FIELD_NAME);
		ColumnName.PRICE = flex.getString(context, R.string.RECEIPTMENU_FIELD_PRICE);
		ColumnName.TAX = flex.getString(context, R.string.RECEIPTMENU_FIELD_TAX);
		ColumnName.PICTURED = flex.getString(context, R.string.column_item_pictured);
		ColumnName.EXPENSABLE = flex.getString(context, R.string.column_item_expensable);
		ColumnName.INDEX = flex.getString(context, R.string.column_item_index);
		ColumnName.ID = flex.getString(context, R.string.column_item_id);
		ColumnName.PAYMENT_METHOD = flex.getString(context, R.string.column_item_payment_method);
		ColumnName.EXTRA_EDITTEXT_1 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1);
		ColumnName.EXTRA_EDITTEXT_2 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2);
		ColumnName.EXTRA_EDITTEXT_3 = flex.getString(context, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);
	}

	public int size() {
		return mColumns.size();
	}

	public boolean isEmpty() {
		return mColumns.isEmpty();
	}

	public String getType(int columnIndex) {
		if (columnIndex >= 0 && columnIndex < mColumns.size()) {
			String type = mColumns.get(columnIndex).getColumnType();
			if (type == null) {
				type = ColumnName.BLANK;
				mColumns.get(columnIndex).setColumnType(type);
			}
			return type;
		} else {
			return ColumnName.BLANK;
		}
	}

	public String getSpinnerOptionAt(int index) {
		if (index >= 0 && index < mOptions.size()) {
			CharSequence type = mOptions.get(index);
			if (type == null) {
				type = ColumnName.BLANK;
			}
			return type.toString();
		} else {
			return ColumnName.BLANK;
		}
	}

	public Column get(int columnIndex) {
		return mColumns.get(columnIndex);
	}

	public final void add() {
		mColumns.add(new Column(mColumns.size() + 1, ColumnName.BLANK));
	}

	public final void add(int index, String columnType) {
		mColumns.add(new Column(index, columnType));
	}

	public final void add(String columnType) {
		mColumns.add(new Column(mColumns.size() + 1, columnType));
	}

	public Column update(int arrayListIndex, int optionIndex) {
		Column column = mColumns.get(arrayListIndex);
		if (optionIndex < mOptions.size()) {
			column.setColumnType(mOptions.get(optionIndex).toString());
		}
		return column;
	}

	public int removeLast() {
		if (!mColumns.isEmpty()) {
			Column column = mColumns.remove(mColumns.size() - 1);
			return column.getIndex();
		} else {
			return -1;
		}
	}

	protected String generateColumn(Column column, Receipt receipt, Trip currentTrip) {
		if (column.getColumnType().equals(ColumnName.BLANK)) {
			return "";
		}
		else if (column.getColumnType().equals(ColumnName.CATEGORY_CODE)) {
			return mPersistenceManager.getDatabase().getCategoryCode(receipt.getCategory());
		}
		else if (column.getColumnType().equals(ColumnName.CATEGORY_NAME)) {
			return receipt.getCategory();
		}
		else if (column.getColumnType().equals(ColumnName.COMMENT)) {
			return receipt.getComment();
		}
		else if (column.getColumnType().equals(ColumnName.CURRENCY)) {
			return receipt.getCurrencyCode();
		}
		else if (column.getColumnType().equals(ColumnName.DATE)) {
			return receipt.getFormattedDate(mContext, mPersistenceManager.getPreferences().getDateSeparator());
		}
		else if (column.getColumnType().equals(ColumnName.NAME)) {
			return receipt.getName();
		}
		else if (column.getColumnType().equals(ColumnName.PRICE)) {
			return receipt.getDecimalFormattedPrice();
		}
		else if (column.getColumnType().equals(ColumnName.TAX)) {
			return receipt.getDecimalFormattedTax();
		}
		else if (column.getColumnType().equals(ColumnName.REPORT_NAME)) {
			return currentTrip.getName();
		}
		else if (column.getColumnType().equals(ColumnName.REPORT_START_DATE)) {
			return currentTrip.getFormattedStartDate(mContext, mPersistenceManager.getPreferences().getDateSeparator());
		}
		else if (column.getColumnType().equals(ColumnName.REPORT_END_DATE)) {
			return currentTrip.getFormattedEndDate(mContext, mPersistenceManager.getPreferences().getDateSeparator());
		}
		else if (column.getColumnType().equals(ColumnName.REPORT_COMMENT)) {
			return currentTrip.getComment();
		}
        else if (column.getColumnType().equals(ColumnName.REPORT_COST_CENTER)) {
            return currentTrip.getCostCenter();
        }
		else if (column.getColumnType().equals(ColumnName.USER_ID)) {
			return mPersistenceManager.getPreferences().getUserID();
		}
		else if (column.getColumnType().equals(ColumnName.IMAGE_FILE_NAME)) {
			return receipt.getFileName();
		}
		else if (column.getColumnType().equals(ColumnName.IMAGE_PATH)) {
			return receipt.getFilePath();
		}
		else if (column.getColumnType().equals(ColumnName.PICTURED)) {
			if (receipt.hasImage()) {
				return mContext.getString(R.string.yes);
			}
			else if (receipt.hasPDF()) {
				return mContext.getString(R.string.yes_as_pdf);
			}
			else {
				return mContext.getString(R.string.no);
			}
		}
		else if (column.getColumnType().equals(ColumnName.INDEX)) {
			return Integer.toString(receipt.getIndex());
		}
		else if (column.getColumnType().equals(ColumnName.ID)) {
			return Integer.toString(receipt.getId());
		}
		else if (column.getColumnType().equals(ColumnName.EXPENSABLE)) {
			return (receipt.isExpensable()) ? mContext.getString(R.string.yes) : mContext.getString(R.string.no);
		}
		else if (column.getColumnType().equalsIgnoreCase(ColumnName.PAYMENT_METHOD)) {
			return receipt.hasPaymentMethod() ? receipt.getPaymentMethod().getMethod() : "";
		}
		else if (column.getColumnType().equalsIgnoreCase(ColumnName.EXTRA_EDITTEXT_1)) {
			return receipt.getExtraEditText1();
		}
		else if (column.getColumnType().equalsIgnoreCase(ColumnName.EXTRA_EDITTEXT_2)) {
			return receipt.getExtraEditText2();
		}
		else if (column.getColumnType().equalsIgnoreCase(ColumnName.EXTRA_EDITTEXT_3)) {
			return receipt.getExtraEditText3();
		}
		else {
			return column.getColumnType();
		}
	}

	public ArrayAdapter<CharSequence> generateArrayAdapter(Context context, Flex flex) {
		return new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, generateOptionsList(context, flex));
	}

	public ArrayList<CharSequence> generateOptionsList(Context context, Flex flex) {
        init(context, flex);
		final ArrayList<CharSequence> options = new ArrayList<CharSequence>(18);
		options.add(ColumnName.BLANK);
		options.add(ColumnName.CATEGORY_CODE);
		options.add(ColumnName.CATEGORY_NAME);
		options.add(ColumnName.COMMENT);
		options.add(ColumnName.CURRENCY);
		options.add(ColumnName.DATE);
		options.add(ColumnName.EXPENSABLE);
		options.add(ColumnName.IMAGE_FILE_NAME);
		options.add(ColumnName.IMAGE_PATH);
		options.add(ColumnName.NAME);
		options.add(ColumnName.PAYMENT_METHOD);
		options.add(ColumnName.PICTURED);
		options.add(ColumnName.PRICE);
		options.add(ColumnName.ID); // Appears as Receipt ID
		options.add(ColumnName.INDEX); //Appears as Receipt Index
		options.add(ColumnName.REPORT_COMMENT);
        options.add(ColumnName.REPORT_COST_CENTER);
		options.add(ColumnName.REPORT_NAME);
		options.add(ColumnName.REPORT_START_DATE);
		options.add(ColumnName.REPORT_END_DATE);
		options.add(ColumnName.TAX);
		options.add(ColumnName.USER_ID);
		if (!TextUtils.isEmpty(ColumnName.EXTRA_EDITTEXT_1)) {
			options.add(ColumnName.EXTRA_EDITTEXT_1);
		}
		if (!TextUtils.isEmpty(ColumnName.EXTRA_EDITTEXT_2)) {
			options.add(ColumnName.EXTRA_EDITTEXT_2);
		}
		if (!TextUtils.isEmpty(ColumnName.EXTRA_EDITTEXT_3)) {
			options.add(ColumnName.EXTRA_EDITTEXT_3);
		}
		return options;
	}

	public static final class Column {

		/**
		 * Defines the Index or Columns Number for this column
		 */
		private int mIndex;

		/**
		 * Defines the data type that this column will print
		 */
		private String mColumnType;

		Column(int index, String columnType) {
			mIndex = index;
			mColumnType = columnType;
		}

		public int getIndex() {
			return mIndex;
		}

		public String getColumnType() {
			return mColumnType;
		}

		public void setIndex(int index) {
			mIndex = index;
		}

		public void setColumnType(String columnType) {
			mColumnType = columnType;
		}

		@Override
		public String toString() {
			return mColumnType;
		}
	}

	public static final String BLANK(Flex flex) {
		return ColumnName.BLANK;
	}

	public static final String CATEGORY_CODE(Flex flex) {
		return ColumnName.CATEGORY_CODE;
	}

	public static final String COMMENT(Context context, Flex flex) {
		if (ColumnName.COMMENT == null) {
			try {
				ColumnName.COMMENT = flex.getString(context, R.string.RECEIPTMENU_FIELD_COMMENT);
			}
			catch (Exception e) {
				ColumnName.COMMENT = "Comment"; // Fallback for select 2.X phones that crash under application context
			}
		}
		return ColumnName.COMMENT;
	}

	public static final String CURRENCY(Context context, Flex flex) {
		if (ColumnName.CURRENCY == null) {
			try {
				ColumnName.CURRENCY = flex.getString(context, R.string.RECEIPTMENU_FIELD_CURRENCY);
			}
			catch (Exception e) {
				ColumnName.CURRENCY = "Currency"; // Fallback for select 2.X phones that crash under application context
			}
		}
		return ColumnName.CURRENCY;
	}

	public static final String DATE(Context context, Flex flex) {
		if (ColumnName.DATE == null) {
			try {
				ColumnName.DATE = flex.getString(context, R.string.RECEIPTMENU_FIELD_DATE);
			}
			catch (Exception e) {
				ColumnName.DATE = "Date"; // Fallback for select 2.X phones that crash under application context
			}

		}
		return ColumnName.DATE;
	}

	public static final String NAME(Context context, Flex flex) {
		if (ColumnName.NAME == null) {
			try {
				ColumnName.NAME = flex.getString(context, R.string.RECEIPTMENU_FIELD_NAME);
			}
			catch (Exception e) {
				ColumnName.NAME = "Name"; // Fallback for select 2.X phones that crash under application context
			}

		}
		return ColumnName.NAME;
	}

	public static final String PRICE(Context context, Flex flex) {
		if (ColumnName.PRICE == null) {
			try {
				ColumnName.PRICE = flex.getString(context, R.string.RECEIPTMENU_FIELD_PRICE);
			}
			catch (Exception e) {
				ColumnName.PRICE = "Price"; // Fallback for select 2.X phones that crash under application context
			}

		}
		return ColumnName.PRICE;
	}

}
