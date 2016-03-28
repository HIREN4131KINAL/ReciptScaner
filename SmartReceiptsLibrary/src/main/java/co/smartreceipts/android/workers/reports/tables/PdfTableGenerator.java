package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.itextpdf.text.pdf.PdfPTable;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;

/**
 * Implements the {@link TableGenerator} contract to generate
 * a {@link com.itextpdf.text.pdf.PdfPTable}
 *
 * @author williambaumann
 */
public final class PdfTableGenerator<DataType> implements TableGenerator<PdfPTable, DataType> {

    private final List<Column<DataType>> mColumns;
    private final Filter<DataType> mFilter;
    private final boolean mPrintHeaders;
    private final boolean mPrintFooters;

    public PdfTableGenerator(@NonNull List<Column<DataType>> columns, boolean printHeaders, boolean printFooters) {
        this(columns, null, printHeaders, printFooters);
    }

    public PdfTableGenerator(@NonNull List<Column<DataType>> columns, @Nullable Filter<DataType> filter, boolean printHeaders, boolean printFooters) {
        mColumns = columns;
        mFilter = filter;
        mPrintHeaders = printHeaders;
        mPrintFooters = printFooters;
    }

    @NonNull
    @Override
    public PdfPTable generate(@NonNull List<DataType> list) {
        if (!list.isEmpty()) {
            final int columnCount = mColumns.size();
            final PdfPTable table = new PdfPTable(columnCount);
            table.setWidthPercentage(100);

            // Add the header
            if (mPrintHeaders) {
                for (int i = 0; i < columnCount; i++) {
                    table.addCell(mColumns.get(i).getHeader());
                }
            }

            // Add each row
            final List<DataType> filteredList = new ArrayList<>(list.size());
            for (int j = 0; j < list.size(); j++) {
                final DataType data = list.get(j);
                if (mFilter == null || mFilter.accept(data)) {
                    for (int i = 0; i < columnCount; i++) {
                        table.addCell(mColumns.get(i).getValue(data));
                    }
                    filteredList.add(data);
                }
            }

            // Add the footer
            if (mPrintFooters) {
                for (int i = 0; i < columnCount; i++) {
                    table.addCell(mColumns.get(i).getFooter(filteredList));
                }
            }
            return table;
        } else {
            return new PdfPTable(1); // Just return an empty table if we don't have any objects
        }
    }


}