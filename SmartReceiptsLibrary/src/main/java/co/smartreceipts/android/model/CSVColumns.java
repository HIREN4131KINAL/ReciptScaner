package co.smartreceipts.android.model;

import wb.android.flex.Flex;
import android.content.Context;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;

public class CSVColumns extends Columns {

	private static final String QUOTE = "\"";
	private static final String ESCAPED_QUOTE = "\"\"";
	private static final String[] STRINGS_THAT_MUST_BE_QUOTED = { ",", "\"", "\n", "\r\n" };

	public CSVColumns(Context context, DatabaseHelper db, Flex flex, PersistenceManager persistenceManager) {
		super(context, db, flex, persistenceManager);
	}

	public CSVColumns(Context context, SmartReceiptsApplication application) {
		super(context, application);
	}

	public String printHeaders() {
		final int size = mColumns.size();
		String print = "";
		for (int i=0; i < size; i++) {
			print += mColumns.get(i).getColumnType();
			if (i == (size - 1)) {
				print += "\n";
			} else {
				print += ",";
			}
		}
		return print;
	}

	public String print(ReceiptRow receipt, Trip currentTrip) {
		final int size = mColumns.size();
		String print = "", column;
		for (int i=0; i < size; i++) {
			column = generateColumn(mColumns.get(i), receipt, currentTrip);

			// Escape all necessary items
			if (column == null) {
				column = "";
			}

			// Append column to print
			print += column;
			if (i == (size - 1)) {
				print += "\n";
			}
			else {
				print += ",";
			}
		}
		return print;
	}

	@Override
	protected String generateColumn(Column column, ReceiptRow receipt, Trip currentTrip) {
		String csv = super.generateColumn(column, receipt, currentTrip);
		if (csv == null) {
			return "";
		}
		if (csv.contains(QUOTE)) {
			csv = csv.replace(QUOTE, ESCAPED_QUOTE);
		}
		for (int i = 0; i < STRINGS_THAT_MUST_BE_QUOTED.length; i++) {
			if (csv.contains(STRINGS_THAT_MUST_BE_QUOTED[i])) {
				csv = QUOTE + csv + QUOTE;
				break;
			}
		}
		return csv;
	}

}