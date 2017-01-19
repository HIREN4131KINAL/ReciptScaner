package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.DefaultPdfBoxContext;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;


public class PdfBoxTableGenerator<DataType> implements TableGenerator<PdfBoxTable, DataType> {

    private final PdfBoxContext context;
    private final List<Column<DataType>> mColumns;
    private final Filter<DataType> mFilter;
    private final boolean mPrintHeaders;
    private final boolean mPrintFooters;
    private float topPadding = 40;
    private float cellPadding = 4;

    public PdfBoxTableGenerator(PdfBoxContext context,
                                List<Column<DataType>> columns,
                                Filter<DataType> receiptFilter,
                                boolean printHeaders,
                                boolean printFooters) {
        this.context = context;
        mColumns = columns;
        mFilter = receiptFilter;
        mPrintHeaders = printHeaders;
        mPrintFooters = printFooters;
    }

    @NonNull
    @Override
    public PdfBoxTable generate(@NonNull List<DataType> list) {
        List<PdfBoxTableRow> rows = new ArrayList<>();
        PdfBoxTableRow footerRow = null;
        PdfBoxTableRow headerRow = null;
        final int colCount = mColumns.size();
        final List<DataType> filteredList = new ArrayList<>(list.size());

        float xStart = context.getPageMarginHorizontal();
        float xEnd = context.getPageSize().getWidth() - context.getPageMarginHorizontal();

        // calculate column widths
        float[] colWidths = calculateColumnWidths(colCount, xEnd - xStart);


        float tableWidth = xEnd - xStart;

        // Add the header
        if (mPrintHeaders) {
            FixedWidthTextCell[] cells = new FixedWidthTextCell[colCount];
            for (int i = 0; i < colCount; i++) {
                FixedWidthTextCell cell = new FixedWidthTextCell(
                        colWidths[i],
                        cellPadding,
                        mColumns.get(i).getHeader(),
                        context.getFont(DefaultPdfBoxContext.FONT_TABLE_HEADER),
                        context.getColor(DefaultPdfBoxContext.COLOR_DARK_BLUE));
                cells[i] = cell;
            }

            headerRow = new PdfBoxTableRow(cells, tableWidth,
                    context.getColor(DefaultPdfBoxContext.COLOR_HEADER));

        }

        if (!list.isEmpty()) {

            // Add each row
            for (int j = 0; j < list.size(); j++) {
                final DataType data = list.get(j);

                if (mFilter == null || mFilter.accept(data)) {
                    FixedWidthTextCell[] cells = new FixedWidthTextCell[colCount];
                    for (int i = 0; i < colCount; i++) {
                        FixedWidthTextCell cell = new FixedWidthTextCell(
                                colWidths[i],
                                cellPadding,
                                mColumns.get(i).getValue(data),
                                context.getFont(DefaultPdfBoxContext.FONT_DEFAULT),
                                AWTColor.BLACK);
                        cells[i] = cell;
                    }

                    PdfBoxTableRow row = new PdfBoxTableRow(cells, tableWidth, j % 2 == 0 ? null :
                            context.getColor(DefaultPdfBoxContext.COLOR_CELL));

                    rows.add(row);
                    filteredList.add(data);
                }
            }
        }

        // Add the footer
        if (mPrintFooters) {
            FixedWidthTextCell[] cells = new FixedWidthTextCell[colCount];
            for (int i = 0; i < colCount; i++) {
                FixedWidthTextCell cell = new FixedWidthTextCell(
                        colWidths[i],
                        cellPadding,
                        mColumns.get(i).getFooter(filteredList),
                        context.getFont(DefaultPdfBoxContext.FONT_DEFAULT),
                        context.getColor(DefaultPdfBoxContext.COLOR_DARK_BLUE));
                cells[i] = cell;
            }
            footerRow = new PdfBoxTableRow(cells, tableWidth,
                    context.getColor(DefaultPdfBoxContext.COLOR_HEADER));
        }
        return new PdfBoxTable(rows, headerRow, footerRow);
    }


    private float[] calculateColumnWidths(int columnCount, float availableWidth) {
        float[] colWidths = new float[columnCount];
        int[] colWeights = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            String name = mColumns.get(i).getName();
            if (name.equals("Name") || name.contains("Comment")) {
                colWeights[i] = 3;
            } else {
                colWeights[i] = 1;
            }
        }

        int weightSum = 0;
        for (int i = 0; i < columnCount; i++) {
            weightSum += colWeights[i];
        }
        float unitWidth = availableWidth / weightSum;
        for (int i = 0; i < columnCount; i++) {
            colWidths[i] = colWeights[i] * unitWidth;
        }

        return colWidths;
    }


}
