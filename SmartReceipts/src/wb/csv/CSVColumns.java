package wb.csv;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.text.DateFormat;

import wb.receiptslibrary.ReceiptRow;
import wb.receiptslibrary.SmartReceiptsActivity;
import wb.receiptslibrary.DatabaseHelper;
import android.widget.ArrayAdapter;

public class CSVColumns {
	
	public static final String BLANK = "Blank Column";
	public static final String CATEGORY_CODE = "Category Code";
	public static final String CATEGORY_NAME = "Category Name";
	public static final String COMMENT = "Comment";
	public static final String CURRENCY = "Currency";
	public static final String DATE = "Date";
	public static final String NAME = "Name";
	public static final String PRICE = "Price";
	
	private final ArrayList<CSVColumn> _csvColumns;
	private final DecimalFormat _decimalFormat;
	private final DateFormat _dateFormat;
	private final DatabaseHelper _db;
	private final ArrayList<CharSequence> _options;
	
	public CSVColumns(SmartReceiptsActivity activity, DatabaseHelper db) {
		_csvColumns = new ArrayList<CSVColumn>();
		_decimalFormat = new DecimalFormat();
		_decimalFormat.setMaximumFractionDigits(2);
		_decimalFormat.setMinimumFractionDigits(2);
		_decimalFormat.setGroupingUsed(false);
		_dateFormat = android.text.format.DateFormat.getDateFormat(activity);
		_db = db;
		_options = getOptionsList();
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
	
	public final String print(ReceiptRow receipt) { 
		final int size = _csvColumns.size();
		String print = "";
		for (int i=0; i < size; i++) {
			print += build(_csvColumns.get(i), receipt);
			if (i == (size - 1))
				print += "\n";
			else
				print += ",";
		}
		return print;
	}
	
	private final String build(CSVColumn column, ReceiptRow receipt) {
		if (column.columnType.equals(BLANK))
			return "";
		else if (column.columnType.equals(CATEGORY_CODE))
			return _db.getCategoryCode(receipt.category);
		else if (column.columnType.equals(CATEGORY_NAME))
			return receipt.category;
		else if (column.columnType.equals(COMMENT))
			return receipt.comment;
		else if (column.columnType.equals(CURRENCY))
			return receipt.currency.getCurrencyCode(); 
		else if (column.columnType.equals(DATE))
			return _dateFormat.format(receipt.date);
		else if (column.columnType.equals(NAME))
			return receipt.name;
		else if (column.columnType.equals(PRICE))
			return _decimalFormat.format(new Float(receipt.price));
		else
			return "";
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
	
	private static final ArrayList<CharSequence> getOptionsList() {
		ArrayList<CharSequence> options = new ArrayList<CharSequence>(7);
		options.add(BLANK);
		options.add(CATEGORY_CODE);
		options.add(CATEGORY_NAME);
		options.add(COMMENT);
		options.add(CURRENCY);
		options.add(DATE);
		options.add(NAME);
		options.add(PRICE);
		return options;
	}
	
	public static final ArrayAdapter<CharSequence> getNewArrayAdapter(SmartReceiptsActivity activity) {
		return new ArrayAdapter<CharSequence>(activity, android.R.layout.simple_spinner_item, getOptionsList());
	}

}