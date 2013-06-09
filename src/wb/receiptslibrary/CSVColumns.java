package wb.receiptslibrary;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.text.DateFormat;

import wb.android.flex.Flex;
import wb.android.flex.Flexable;
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
		
	private final ArrayList<CSVColumn> _csvColumns;
	private final DecimalFormat _decimalFormat;
	private final DateFormat _dateFormat;
	private final DatabaseHelper _db;
	private final ArrayList<CharSequence> _options;
	private final SmartReceiptsActivity _activity;
	
	public CSVColumns(SmartReceiptsActivity activity, DatabaseHelper db, Flex flex) {
		_activity = activity;
		_csvColumns = new ArrayList<CSVColumn>();
		_decimalFormat = new DecimalFormat();
		_decimalFormat.setMaximumFractionDigits(2);
		_decimalFormat.setMinimumFractionDigits(2);
		_decimalFormat.setGroupingUsed(false);
		_dateFormat = android.text.format.DateFormat.getDateFormat(activity);
		_db = db;
		COMMENT = flex.getString(R.string.RECEIPTMENU_FIELD_COMMENT);
		CURRENCY = flex.getString(R.string.RECEIPTMENU_FIELD_CURRENCY);
		DATE = flex.getString(R.string.RECEIPTMENU_FIELD_DATE);
		NAME = flex.getString(R.string.RECEIPTMENU_FIELD_NAME);
		PRICE = flex.getString(R.string.RECEIPTMENU_FIELD_PRICE);
		TAX = flex.getString(R.string.RECEIPTMENU_FIELD_TAX);
		EXTRA_EDITTEXT_1 = flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1);
		EXTRA_EDITTEXT_2 = flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2);
		EXTRA_EDITTEXT_3 = flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);
		_options = getOptionsList(flex);
	}
	
	public final void add() {
		_csvColumns.add(new CSVColumn(_csvColumns.size() + 1, BLANK));
	}
	
	public final void add(int index, String columnType) {
		_csvColumns.add(new CSVColumn(index, columnType));
	}
	
	public final void add(String columnType) {
		_csvColumns.add(new CSVColumn(_csvColumns.size() + 1, columnType));
	}
	
	public final CSVColumn update(int arrayListIndex, int optionIndex) {
		CSVColumn column = _csvColumns.get(arrayListIndex);
		if (optionIndex < _options.size()) //No customizations added
			column.columnType = (String) _options.get(optionIndex);
		return column;
	}
	
	public final int removeLast() {
		if (!_csvColumns.isEmpty()) {
			CSVColumn column = _csvColumns.remove(_csvColumns.size() - 1);
			return column.index;
		}
		else
			return -1;
	}
	
	public final String print(ReceiptRow receipt, TripRow currentTrip) { 
		final int size = _csvColumns.size();
		String print = "";
		for (int i=0; i < size; i++) {
			print += build(_csvColumns.get(i), receipt, currentTrip);
			if (i == (size - 1))
				print += "\n";
			else
				print += ",";
		}
		return print;
	}
	
	public final String printHeaders() { 
		final int size = _csvColumns.size();
		String print = "";
		for (int i=0; i < size; i++) {
			print += _csvColumns.get(i).columnType;
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
			csv = _db.getCategoryCode(receipt.category);
		else if (column.columnType.equals(CATEGORY_NAME))
			csv = receipt.category;
		else if (column.columnType.equals(COMMENT))
			csv = receipt.comment;
		else if (column.columnType.equals(CURRENCY))
			csv = receipt.currency.getCurrencyCode(); 
		else if (column.columnType.equals(DATE))
			csv = _dateFormat.format(receipt.date);
		else if (column.columnType.equals(NAME))
			csv = receipt.name;
		else if (column.columnType.equals(PRICE))
			csv = _decimalFormat.format(Float.valueOf(receipt.price));
		else if (column.columnType.equals(TAX))
			csv = _decimalFormat.format(Float.valueOf(receipt.tax));
		else if (column.columnType.equals(REPORT_NAME))
			csv = currentTrip.dir.getName();
		else if (column.columnType.equals(REPORT_START_DATE))
			csv = _dateFormat.format(currentTrip.from);
		else if (column.columnType.equals(REPORT_END_DATE))
			csv = _dateFormat.format(currentTrip.to);
		else if (column.columnType.equals(USER_ID))
			csv = _activity.getPreferences().getUserID();
		else if (column.columnType.equals(IMAGE_FILE_NAME))
			csv = (receipt.img == null) ? "" : receipt.img.getName();
		else if (column.columnType.equals(IMAGE_PATH))
			csv = (receipt.img == null) ? "" : receipt.img.getAbsolutePath();
		else if (column.columnType.equalsIgnoreCase(EXTRA_EDITTEXT_1))
			csv = receipt.extra_edittext_1;
		else if (column.columnType.equalsIgnoreCase(EXTRA_EDITTEXT_2))
			csv = receipt.extra_edittext_2;
		else if (column.columnType.equalsIgnoreCase(EXTRA_EDITTEXT_3))
			csv = receipt.extra_edittext_3;
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
		return _csvColumns.size();
	}
	
	public final boolean isEmpty() {
		return _csvColumns.isEmpty();
	}
	
	public final String getType(int columnIndex) {
		if (columnIndex >= 0 && columnIndex < _csvColumns.size())
			return _csvColumns.get(columnIndex).columnType;
		else
			return "";
	}
	
	private static final ArrayList<CharSequence> getOptionsList(Flex flex) {
		ArrayList<CharSequence> options = new ArrayList<CharSequence>(10);
		if (COMMENT == null) COMMENT = flex.getString(R.string.RECEIPTMENU_FIELD_COMMENT);
		if (CURRENCY == null) CURRENCY = flex.getString(R.string.RECEIPTMENU_FIELD_CURRENCY);
		if (DATE == null) DATE = flex.getString(R.string.RECEIPTMENU_FIELD_DATE);
		if (NAME == null) NAME = flex.getString(R.string.RECEIPTMENU_FIELD_NAME);
		if (PRICE == null) PRICE = flex.getString(R.string.RECEIPTMENU_FIELD_PRICE);
		if (TAX == null) TAX = flex.getString(R.string.RECEIPTMENU_FIELD_TAX);
		if (EXTRA_EDITTEXT_1 == null) EXTRA_EDITTEXT_1 = flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1);
		if (EXTRA_EDITTEXT_2 == null) EXTRA_EDITTEXT_2 = flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2);
		if (EXTRA_EDITTEXT_3 == null) EXTRA_EDITTEXT_3 = flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);
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
	
	public static final ArrayAdapter<CharSequence> getNewArrayAdapter(SmartReceiptsActivity activity, Flex flex) {
		return new ArrayAdapter<CharSequence>(activity, android.R.layout.simple_spinner_item, getOptionsList(flex));
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
	
	public static final String COMMENT(Flex flex) {
		if (COMMENT == null) COMMENT = flex.getString(R.string.RECEIPTMENU_FIELD_COMMENT);
		return COMMENT;
	}
	
	public static final String CURRENCY(Flex flex) {
		if (CURRENCY == null) CURRENCY = flex.getString(R.string.RECEIPTMENU_FIELD_CURRENCY);
		return CURRENCY;
	}
	
	public static final String DATE(Flex flex) {
		if (DATE == null) DATE = flex.getString(R.string.RECEIPTMENU_FIELD_DATE);
		return DATE;
	}
	
	public static final String NAME(Flex flex) {
		if (NAME == null) NAME = flex.getString(R.string.RECEIPTMENU_FIELD_NAME);
		return NAME;
	}
	
	public static final String PRICE(Flex flex) {
		if (PRICE == null) PRICE = flex.getString(R.string.RECEIPTMENU_FIELD_PRICE);
		return PRICE;
	}
	
	public static final String TAX(Flex flex) {
		if (TAX == null) TAX = flex.getString(R.string.RECEIPTMENU_FIELD_TAX);
		return TAX;
	}
	
	public static final String EXTRA_EDITTEXT_1(Flex flex) {
		if (EXTRA_EDITTEXT_1 == null) EXTRA_EDITTEXT_1 = flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1);
		return EXTRA_EDITTEXT_1;
	}
	
	public static final String EXTRA_EDITTEXT_2(Flex flex) {
		if (EXTRA_EDITTEXT_2 == null) EXTRA_EDITTEXT_2 = flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2);
		return EXTRA_EDITTEXT_2;
	}
	
	public static final String EXTRA_EDITTEXT_3(Flex flex) {
		if (EXTRA_EDITTEXT_3 == null) EXTRA_EDITTEXT_3 = flex.getString(R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3);
		return EXTRA_EDITTEXT_3;
	}

}