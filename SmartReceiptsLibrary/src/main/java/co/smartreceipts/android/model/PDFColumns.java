package co.smartreceipts.android.model;

import wb.android.flex.Flex;
import android.content.Context;
import android.text.TextUtils;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;

import com.itextpdf.text.pdf.PdfPTable;

public class PDFColumns extends Columns {
	
	public PDFColumns(Context context, DatabaseHelper db, Flex flex, PersistenceManager persistenceManager) {
		super(context, db, flex, persistenceManager);
	}
	
	public PDFColumns(Context context, SmartReceiptsApplication application) {
		super(context, application);
	}
	
	public PdfPTable getTableWithHeaders() {
		final PdfPTable table = getPDFTable();
		printHeaders(table);
		return table;
	}
	
	public void printHeaders(PdfPTable table) { 
		final int size = mColumns.size();
		for (int i=0; i < size; i++) {
			table.addCell(mColumns.get(i).getColumnType());
		}
	}
	
	public void print(PdfPTable table, Receipt receipt, Trip currentTrip) {
		final int size = mColumns.size();
		for (int i=0; i < size; i++) {
			String column = generateColumn(mColumns.get(i), receipt, currentTrip);
			if (TextUtils.isEmpty(column)) { // Escape all necessary items
				column = new String();
			}
			table.addCell(column);
		}
	}
	
	public PdfPTable getPDFTable() {
		int size = size();
		if (size == 0) {
			size = 1;
		}
		PdfPTable table = new PdfPTable(size);
		table.setWidthPercentage(100);
		return table;
	}

}
