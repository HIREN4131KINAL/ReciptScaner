package co.smartreceipts.android.workers.reports.tables;

import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.IOException;
import java.util.List;

import co.smartreceipts.android.workers.reports.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.pdf.pdfbox.PdfBoxContext;

class TableRow {
    private PdfBoxTableGenerator pdfBoxTableGenerator;
    private final FixedWidthCell[] cells;

    private AWTColor backgroundColor;
    private float width;


    TableRow(PdfBoxTableGenerator pdfBoxTableGenerator, FixedWidthCell[] cells, float width, AWTColor backgroundColor) {
        this.pdfBoxTableGenerator = pdfBoxTableGenerator;
        this.cells = cells;
        this.width = width;
        this.backgroundColor = backgroundColor;
    }

    public AWTColor getBackgroundColor() {
        return backgroundColor;
    }


    public float getWidth() {
        return width;
    }

    public float getHeight() {
        float rowHeight = 0;
        for (int i = 0; i < cells.length; i++) {
            rowHeight = Math.max(cells[i].getHeight(), rowHeight);
        }
        return rowHeight;
    }


    /**
     * Prints the cell content centering the contents vertically and horizontally.
     * @param fixedWidthCell
     * @param contentStream
     * @param cell
     * @param xCell
     * @param yCell
     * @param rowHeight      @throws IOException
     */
    private void printCellContent(PDPageContentStream contentStream,
                                  FixedWidthCell cell,
                                  float xCell,
                                  float yCell,
                                  float rowHeight) throws IOException {

        List<String> lines = cell.getLines();

        PdfBoxContext.FontSpec fontSpec = cell.getFontSpec();

        // unused space above and below the lines, excluding
        float unusedSpace = (rowHeight - 2 * cell.getCellPadding() - (lines.size()) * PdfBoxUtils.getFontHeight(fontSpec));
        // position the cursor where the baseline of the first line should be written
        float y = yCell - cell.getCellPadding() - unusedSpace / 2.0f - PdfBoxUtils.getFontAboveBaselineHeight(fontSpec);

        for (int i = 0; i < lines.size(); i++) {
            float stringWidth = PdfBoxUtils.getStringWidth(lines.get(i), fontSpec);
            float dx = (cell.getWidth() - stringWidth) / 2.0f;


            contentStream.setFont(fontSpec.getFont(), fontSpec.getSize());
            contentStream.setNonStrokingColor(cell.getColor());
            contentStream.beginText();
            contentStream.newLineAtOffset(
                    xCell + dx,
                    y);
            contentStream.showText(lines.get(i));
            contentStream.endText();

            y -= PdfBoxUtils.getFontHeight(fontSpec);

        }

    }

    void printRowContents(PDPageContentStream contentStream, float x, float y) throws IOException {
        float xCell = x;
        float yCell = y;
        for (FixedWidthCell cell : cells) {
            printCellContent(contentStream, cell, xCell, yCell, getHeight());
            xCell += cell.getWidth();
        }
    }


}
