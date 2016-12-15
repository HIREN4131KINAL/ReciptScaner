package co.smartreceipts.android.workers.reports.tables;

import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import co.smartreceipts.android.filters.Filter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.workers.reports.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;

public class PdfBoxTableGenerator<DataType> implements TableGenerator<Void, DataType> {

    private final PdfBoxContext context;
    private final PDPageContentStream mContentStream;
    private final List<Column<DataType>> mColumns;
    private final Filter<DataType> mFilter;
    private final boolean mPrintHeaders;
    private final boolean mPrintFooters;
    private final float mStartingCursorPosition;
    private int topPadding = 40;
    private float cellPadding = 8;

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
                final int rowCount = filteredList.size()
                        + (mPrintHeaders ? 1 : 0)
                        + (mPrintFooters ? 1 : 0);

                float xStart = context.getPageMarginHorizontal();
                float xEnd = context.getPageSize().getWidth() - context.getPageMarginHorizontal();

                // calculate column widths
                float[] colWidths = calculateColumnWidths(colCount, xEnd - xStart);


                // draw the header
                float xPosition = xStart;
                float yPosition = mStartingCursorPosition - topPadding;

                float rowHeight = 0;
                for (int i = 0; i < colCount; i++) {
                    FixedWidthCell cell = new FixedWidthCell(xPosition, yPosition, colWidths[i]);
//                    PDRectangle cell = new PDRectangle(xPosition,
//                            yPosition - rowHeight,
//                            colWidths[i],
//                            rowHeight);
//                    mContentStream.setNonStrokingColor(255, 0, 255 - 20 * i);
//                    mContentStream.addRect(cell.getLowerLeftX(),
//                            cell.getLowerLeftY(),
//                            cell.getWidth(),
//                            cell.getHeight());
//                    mContentStream.fill();

                    xPosition += colWidths[i];

                    float cellHeight = printTextInCell(cell, mColumns.get(i).getHeader());
                    rowHeight = Math.max(cellHeight, rowHeight);
                }


                //draw the rows
                float nexty = mStartingCursorPosition - topPadding;
                PDRectangle rect = new PDRectangle(xStart, nexty - rowHeight, (xEnd - xStart) / colCount, rowHeight);
//                mContentStream.setNonStrokingColor(255, 0, 255);
//                mContentStream.addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
//                mContentStream.fill();

                for (int i = 0; i <= rowCount; i++) {
                    mContentStream.moveTo(xStart, nexty);
                    mContentStream.lineTo(xEnd, nexty);
                    mContentStream.stroke();
                    nexty -= rowHeight;
                }

                //draw the columns
                float x = context.getPageMarginHorizontal();
                float y = mStartingCursorPosition - topPadding;
                for (int i = 0; i <= colCount; i++) {
                    mContentStream.moveTo(x, y);
                    mContentStream.lineTo(x, y - rowCount * rowHeight);
                    mContentStream.stroke();
                    if (i < colCount) {
                        x += colWidths[i];
                    }
                }

                // Add the header
                if (mPrintHeaders) {
                    for (int i = 0; i < colCount; i++) {
                        // mColumns.get(i).getHeader();
                    }
                }

                // Add each row
                for (int j = 0; j < list.size(); j++) {
                    final DataType data = list.get(j);
                    if (mFilter == null || mFilter.accept(data)) {
                        for (int i = 0; i < colCount; i++) {
                            // mColumns.get(i).getValue(data);
                        }
                        filteredList.add(data);
                    }
                }


                // Add the footer
                if (mPrintFooters) {
                    for (int i = 0; i < colCount; i++) {
                        // mColumns.get(i).getFooter(filteredList)
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }

        return null;
    }

    private float printTextInCell(FixedWidthCell cell, String text) throws IOException {

        PdfBoxContext.FontSpec fontSpec = context.getSmallFont();
        float stringWidth = PdfBoxUtils.getStringWidth(text, fontSpec);

//        if (stringWidth > cell.getWidth()) {
//            printMultiLineTextInCell(cell, text, fontSpec);
//            return;
//        }

        float fontHeight = PdfBoxUtils.getFontHeight(fontSpec);

        float dx = (cell.getWidth() - stringWidth) / 2.0f;
        float dy =  PdfBoxUtils.getFontAboveBaselineHeight(fontSpec) + cellPadding;


        mContentStream.setFont(fontSpec.getFont(), fontSpec.getSize());
        mContentStream.beginText();
        mContentStream.newLineAtOffset(
                cell.getUpperLeftX() + dx,
                cell.getUpperLeftY() - dy);
        mContentStream.showText(text);
        mContentStream.endText();


        return fontHeight + 2*cellPadding;
    }

    private void printMultiLineTextInCell(FixedWidthCell cell, String text, PdfBoxContext.FontSpec fontSpec) throws IOException {
        List<String> multiLineString = createMultiLineString(text, cell.getWidth(), fontSpec);


    }

    private List<String> createMultiLineString(String text, float width, PdfBoxContext.FontSpec fontSpec) throws IOException {
        List<String> strings = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        StringBuilder sb = new StringBuilder();
        while(tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            sb.append(token + " ");
            if (PdfBoxUtils.getStringWidth(sb.toString(), fontSpec) > width) {
                strings.add(sb.substring(0, sb.indexOf(token)));
                sb = new StringBuilder();
                sb.append(token + " ");
            }
        }
        strings.add(sb.toString());
        return strings;

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

    class FixedWidthCell {
        float upperLeftX;
        float upperLeftY;
        private final float width;

        public FixedWidthCell(float upperLeftX, float upperLeftY, float width) {
            this.upperLeftX = upperLeftX;
            this.upperLeftY = upperLeftY;
            this.width = width;
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
    }
}
