package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;


public class PdfBoxTableGenerator<DataType> implements TableGenerator<PdfBoxTable, DataType> {

    private final PdfBoxContext context;
    private final List<Column<DataType>> mColumns;
    private final Filter<DataType> mFilter;
    private final boolean mPrintHeaders;
    private final boolean mPrintFooters;
    private final float mStartingCursorPosition;
    private float topPadding = 40;
    private float cellPadding = 4;

    public PdfBoxTableGenerator(PdfBoxContext context,
                                List<Column<DataType>> columns,
                                Filter<DataType> receiptFilter,
                                boolean printHeaders,
                                boolean printFooters,
                                float currentLineOffset) {
        this.context = context;
        mColumns = columns;
        mFilter = receiptFilter;
        mPrintHeaders = printHeaders;
        mPrintFooters = printFooters;
        mStartingCursorPosition = currentLineOffset;
    }

    @NonNull
    @Override
    public PdfBoxTable generate(@NonNull List<DataType> list) {
        List<PdfBoxTableRow> rows = new ArrayList<>();
        PdfBoxTableRow footerRow = null;
        PdfBoxTableRow headerRow = null;
        if (!list.isEmpty()) {
            final int colCount = mColumns.size();
            final List<DataType> filteredList = new ArrayList<>(list.size());

            float xStart = context.getPageMarginHorizontal();
            float xEnd = context.getPageSize().getWidth() - context.getPageMarginHorizontal();

            // calculate column widths
            float[] colWidths = calculateColumnWidths(colCount, xEnd - xStart);

            float xPosition = xStart;
            float yPosition = mStartingCursorPosition - topPadding;

            float tableWidth = xEnd - xStart;

            // Add the header
            if (mPrintHeaders) {
                FixedWidthCell[] cells = new FixedWidthCell[colCount];
                for (int i = 0; i < colCount; i++) {
                    FixedWidthCell cell = new FixedWidthCell(
                            colWidths[i],
                            cellPadding,
                            mColumns.get(i).getHeader(),
                            context.getFont("TABLE_HEADER"),
                            context.getColor("DARK_BLUE"));
                    cells[i] = cell;
                    xPosition += colWidths[i];
                }

                headerRow = new PdfBoxTableRow(cells, tableWidth, context.getColor("HEADER_BACKGROUND"));

//                    printRow(headerRow, xStart, yPosition);

//                    // draw the separator line
//                    int lineHeight = 2;
//                    mContentStream.setLineWidth(lineHeight);
//                    mContentStream.setStrokingColor(context.getColor("DARK_BLUE"));
//                    mContentStream.moveTo(xStart, yPosition - headerRow.getHeight());
//                    mContentStream.lineTo(xEnd, yPosition - headerRow.getHeight());
//                    mContentStream.stroke();
//                    mContentStream.setLineWidth(1);

                yPosition -= headerRow.getHeight();

//                    //draw the grid: rows
//                    float nexty = mStartingCursorPosition - topPadding;
//                    for (int i = 0; i <= rowCount; i++) {
//                        mContentStream.moveTo(xStart, nexty);
//                        mContentStream.lineTo(xEnd, nexty);
//                        mContentStream.stroke();
//                        nexty -= rowHeight;
//                    }

//                    //draw the grid: columns
//                    float x = context.getPageMarginHorizontal();
//                    float y = mStartingCursorPosition - topPadding;
//                    for (int i = 0; i <= colCount; i++) {
//                        mContentStream.moveTo(x, y);
//                        mContentStream.lineTo(x, y - rowCount * rowHeight);
//                        mContentStream.stroke();
//                        if (i < colCount) {
//                            x += colWidths[i];
//                        }
//                    }
            }


            // Add each row
            for (int j = 0; j < list.size(); j++) {
                xPosition = xStart;
                final DataType data = list.get(j);

                if (mFilter == null || mFilter.accept(data)) {
                    FixedWidthCell[] cells = new FixedWidthCell[colCount];
                    for (int i = 0; i < colCount; i++) {
                        FixedWidthCell cell = new FixedWidthCell(
                                colWidths[i],
                                cellPadding,
                                mColumns.get(i).getValue(data),
                                context.getFont("DEFAULT"),
                                AWTColor.BLACK);
                        cells[i] = cell;
                        xPosition += colWidths[i];
                    }

                    PdfBoxTableRow row = new PdfBoxTableRow(cells, tableWidth, j % 2 == 0 ? null : context.getColor("CELL_BACKGROUND"));

//                        printRow(row, xStart, yPosition);
                    yPosition -= row.getHeight();

                    rows.add(row);
                    filteredList.add(data);
                }
            }


            // Add the footer
            if (mPrintFooters) {
                xPosition = xStart;
                FixedWidthCell[] cells = new FixedWidthCell[colCount];
                for (int i = 0; i < colCount; i++) {
                    FixedWidthCell cell = new FixedWidthCell(
                            colWidths[i],
                            cellPadding,
                            mColumns.get(i).getFooter(filteredList),
                            context.getFont("TABLE_HEADER"),
                            context.getColor("DARK_BLUE"));
                    cells[i] = cell;
                    xPosition += colWidths[i];
                }
                footerRow = new PdfBoxTableRow(cells, tableWidth, context.getColor("HEADER_BACKGROUND"));

//                    printRow(footerRow, xStart, yPosition);
                yPosition -= footerRow.getHeight();
            }

            return new PdfBoxTable(rows, headerRow, footerRow);

        }

        return null;
    }


    /**
     *
     * @param row
     * @param x The x-coordinate of the upper-left corner of the row
     * @param y The y-coordinate of the upper-left corner of the row
     * @throws IOException
     */
//    private void printRow(PdfBoxTableRow row, float x, float y) throws IOException {
//        // draw the background

//    }


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
