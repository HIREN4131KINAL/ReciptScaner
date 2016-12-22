package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.workers.reports.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;

import static android.R.attr.rowHeight;


public class PdfBoxTableGenerator<DataType> implements TableGenerator<Void, DataType> {

    private final PdfBoxContext context;
    private final PDPageContentStream mContentStream;
    private final List<Column<DataType>> mColumns;
    private final Filter<DataType> mFilter;
    private final boolean mPrintHeaders;
    private final boolean mPrintFooters;
    private final float mStartingCursorPosition;
    private float topPadding = 40;
    private float cellPadding = 4;

    public PdfBoxTableGenerator(PdfBoxContext context,
                                PDPageContentStream contentStream,
                                List<Column<DataType>> columns,
                                Filter<DataType> receiptFilter,
                                boolean printHeaders,
                                boolean printFooters,
                                float currentLineOffset) {
        this.context = context;
        mContentStream = contentStream;
        mColumns = columns;
        mFilter = receiptFilter;
        mPrintHeaders = printHeaders;
        mPrintFooters = printFooters;
        mStartingCursorPosition = currentLineOffset;
    }

    @NonNull
    @Override
    public Void generate(@NonNull List<DataType> list) {
        try {
            if (!list.isEmpty()) {
                final int colCount = mColumns.size();
                final List<DataType> filteredList = new ArrayList<>(list.size());
//                final int rowCount = filteredList.size()
//                        + (mPrintHeaders ? 1 : 0)
//                        + (mPrintFooters ? 1 : 0);

                float xStart = context.getPageMarginHorizontal();
                float xEnd = context.getPageSize().getWidth() - context.getPageMarginHorizontal();

                // calculate column widths
                float[] colWidths = calculateColumnWidths(colCount, xEnd - xStart);

                float xPosition = xStart;
                float yPosition = mStartingCursorPosition - topPadding;

                // Add the header
                if (mPrintHeaders) {
                    FixedWidthCell[] cells = new PdfBoxTableGenerator.FixedWidthCell[colCount];
                    for (int i = 0; i < colCount; i++) {
                        FixedWidthCell cell = new FixedWidthCell(xPosition,
                                yPosition,
                                colWidths[i],
                                mColumns.get(i).getHeader(),
                                context.getFont("TABLE_HEADER"),
                                context.getColor("DARK_BLUE"));
                        cells[i] = cell;
                        xPosition += colWidths[i];
                    }

                    Row headerRow = new Row(cells, context.getColor("HEADER_BACKGROUND"));

                    printRow(headerRow);

                    // draw the separator line
                    int lineHeight = 2;
                    mContentStream.setLineWidth(lineHeight);
                    mContentStream.setStrokingColor(context.getColor("DARK_BLUE"));
                    mContentStream.moveTo(xStart, yPosition - rowHeight);
                    mContentStream.lineTo(xEnd, yPosition - rowHeight);
                    mContentStream.stroke();
                    mContentStream.setLineWidth(1);

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
                        FixedWidthCell[] cells = new PdfBoxTableGenerator.FixedWidthCell[colCount];
                        for (int i = 0; i < colCount; i++) {
                            FixedWidthCell cell = new FixedWidthCell(xPosition,
                                    yPosition,
                                    colWidths[i],
                                    mColumns.get(i).getValue(data),
                                    context.getFont("DEFAULT"),
                                    AWTColor.BLACK);
                            cells[i] = cell;
                            xPosition += colWidths[i];
                        }

                        Row row = new Row(cells, j % 2 == 0 ? null : context.getColor("CELL_BACKGROUND"));

                        printRow(row);
                        yPosition -= row.getHeight();
                    }
                }


                // Add the footer
                if (mPrintFooters) {
                    xPosition = xStart;
                    FixedWidthCell[] cells = new PdfBoxTableGenerator.FixedWidthCell[colCount];
                    for (int i = 0; i < colCount; i++) {
                        FixedWidthCell cell = new FixedWidthCell(xPosition,
                                yPosition,
                                colWidths[i],
                                mColumns.get(i).getFooter(filteredList),
                                context.getFont("TABLE_HEADER"),
                                context.getColor("DARK_BLUE"));
                        cells[i] = cell;
                        xPosition += colWidths[i];
                    }

                    Row footerRow = new Row(cells, context.getColor("HEADER_BACKGROUND"));

                    printRow(footerRow);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

        return null;
    }


    public void printRow(Row row) throws IOException {
        // draw the background
        if (row.getBackgroundColor() != null) {
            PDRectangle rect = new PDRectangle(row.getUpperLeftX(), row.getUpperLeftY() - row.getHeight(),
                    row.getWidth(), row.getHeight());
            mContentStream.setNonStrokingColor(row.getBackgroundColor());
            mContentStream.addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
            mContentStream.fill();
            mContentStream.setNonStrokingColor(AWTColor.BLACK);
        }

        // draw the cells contents
        for (int i = 0; i < row.getCells().length; i++) {
            row.getCells()[i].printCellContent(row.getHeight());
        }

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


    class Row {
        private final FixedWidthCell[] cells;
        private AWTColor backgroundColor;


        Row(FixedWidthCell[] cells, AWTColor backgroundColor) {
            this.cells = cells;
            this.backgroundColor = backgroundColor;
        }

        public AWTColor getBackgroundColor() {
            return backgroundColor;
        }

        public FixedWidthCell[] getCells() {
            return cells;
        }

        private float getWidth() {
            return cells[cells.length - 1].getUpperLeftX() + cells[cells.length - 1].getWidth() - cells[0].getUpperLeftX();
        }

        private float getHeight() {
            float rowHeight = 0;
            for (int i = 0; i < cells.length; i++) {
                rowHeight = Math.max(cells[i].getHeight(), rowHeight);
            }
            return rowHeight;
        }

        private float getUpperLeftX() {
            return cells[0].getUpperLeftX();
        }

        private float getUpperLeftY() {
            return cells[0].getUpperLeftY();
        }

    }


    class FixedWidthCell {
        private final float upperLeftX;
        private final float upperLeftY;
        private final float width;
        private final String text;
        private final PdfBoxContext.FontSpec fontSpec;
        private final AWTColor color;
        private List<String> lines;

        public FixedWidthCell(float upperLeftX, float upperLeftY, float width, String text,
                              PdfBoxContext.FontSpec fontSpec,
                              AWTColor color) {
            this.upperLeftX = upperLeftX;
            this.upperLeftY = upperLeftY;
            this.width = width;
            this.text = text;
            this.fontSpec = fontSpec;
            this.color = color;
        }

        private void breakUpString(String text) throws IOException {
            lines = new ArrayList<>();

            StringTokenizer tokenizer = new StringTokenizer(text, " ");
            StringBuilder sb = new StringBuilder();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                sb.append(token).append(" ");
                String currentLine = sb.toString();
                if (PdfBoxUtils.getStringWidth(currentLine, fontSpec) > width) {
                    if (sb.indexOf(token) > 0) {
                        lines.add(sb.substring(0, sb.indexOf(token)));
                        sb = new StringBuilder();
                        sb.append(token).append(" ");
                    } else {
                        lines.add(sb.toString().trim());
                        sb = new StringBuilder();
                    }
                }
            }
            if (!sb.toString().isEmpty()) {
                lines.add(sb.toString().trim());
            }
        }

        public float getUpperLeftX() {
            return upperLeftX;
        }

        public float getUpperLeftY() {
            return upperLeftY;
        }

        public float getWidth() {
            return width;
        }

        public float getHeight() {
            try {
                breakUpString(text);
                return PdfBoxUtils.getFontHeight(fontSpec) * lines.size() + 2 * cellPadding;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }

        }

        public List<String> getLines() {
            return lines;
        }

        /**
         * Prints the cell content centering the contents vertically and horizontally.
         *
         * @param cell
         * @param rowHeight
         * @throws IOException
         */
        public void printCellContent(float rowHeight) throws IOException {

            List<String> lines = getLines();

            float x = getUpperLeftX();
            float y = getUpperLeftY();

            // unused space above and below the lines, excluding
            float unusedSpace = (rowHeight - 2 * cellPadding - (lines.size()) * PdfBoxUtils.getFontHeight(fontSpec));
            // position the cursor where the baseline of the first line should be written
            y = y - cellPadding - unusedSpace / 2.0f - PdfBoxUtils.getFontAboveBaselineHeight(fontSpec);

            for (int i = 0; i < lines.size(); i++) {
                float stringWidth = PdfBoxUtils.getStringWidth(lines.get(i), fontSpec);
                float dx = (getWidth() - stringWidth) / 2.0f;


                mContentStream.setFont(fontSpec.getFont(), fontSpec.getSize());
                mContentStream.setNonStrokingColor(color);
                mContentStream.beginText();
                mContentStream.newLineAtOffset(
                        x + dx,
                        y);
                mContentStream.showText(lines.get(i));
                mContentStream.endText();

                y -= PdfBoxUtils.getFontHeight(fontSpec);

            }

        }
    }
}
