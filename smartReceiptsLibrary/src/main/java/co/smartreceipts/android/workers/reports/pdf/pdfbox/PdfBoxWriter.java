package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.workers.reports.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.tables.FixedWidthCell;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTable;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableRow;

import static android.R.attr.rowHeight;
import static android.R.attr.x;
import static android.R.attr.y;


public class PdfBoxWriter {
    private PDDocument doc;
    private PdfBoxContext context;
    private List<PDPage> pages;
    private float currentYPosition;
    private PDPageContentStream contentStream;
    private boolean inTextBlock;


    public PdfBoxWriter(PDDocument doc, PdfBoxContext context) throws IOException {
        this.doc = doc;
        this.context = context;
        pages = new ArrayList<>();
        newPage();
    }


    private void newPage() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }
        PDPage page = new PDPage(PDRectangle.A4);
        currentYPosition = page.getMediaBox().getHeight() - context.getPageMarginVertical();
        pages.add(page);
        contentStream = new PDPageContentStream(doc, page);
    }

    private PDPage getCurrentPage() {
        return pages.get(pages.size() - 1);
    }

    public void openTextBlock() throws IOException {
        inTextBlock = true;
        contentStream.beginText();
        contentStream.newLineAtOffset(
                context.getPageMarginHorizontal(), currentYPosition);
    }

    public void closeTextBlock() throws IOException {
        inTextBlock = false;
        contentStream.endText();
    }


    public void writeNewLine(PdfBoxContext.FontSpec spec,
                             @StringRes int resId,
                             Object... args) throws IOException {
        if (resId != 0) {
            writeNewLine(spec, context.getString(resId, args));
        }
    }


    public void writeNewLine(PdfBoxContext.FontSpec spec,
                             String str) throws IOException {
        // set the font
        contentStream.setFont(spec.getFont(), spec.getSize());
        // calculate dy (font size + line spacing)
        float dy = spec.getSize() + context.getLineSpacing();
        // move the cursor by dy and update the currentYPosition
        contentStream.newLineAtOffset(0, -dy);
        currentYPosition -= dy;
        // write the text
        contentStream.showText(str);
    }

    public void writeAndClose() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }

        for (PDPage page : pages) {
            doc.addPage(page);
        }
    }

    public void writeTable(PdfBoxTable table) throws IOException {

        if (table.getHeaderRow() != null) {
            printRow(table.getHeaderRow());
        }

        for (PdfBoxTableRow pdfBoxTableRow : table.getRows()) {
            printRow(pdfBoxTableRow);
        }

        if (table.getFooterRow() != null) {

        }
    }

    private void printRow(PdfBoxTableRow row) throws IOException {
        float x = context.getPageMarginHorizontal();
        if (row.getBackgroundColor() != null) {
            PDRectangle rect = new PDRectangle(
                    x,
                    currentYPosition - row.getHeight(),
                    row.getWidth(),
                    row.getHeight());
            contentStream.setNonStrokingColor(row.getBackgroundColor());
            contentStream.addRect(rect.getLowerLeftX(), rect.getLowerLeftY(), rect.getWidth(), rect.getHeight());
            contentStream.fill();
            contentStream.setNonStrokingColor(AWTColor.BLACK);
        }

        // draw the cells contents
        printRowContents(row, x, currentYPosition);
        currentYPosition -= row.getHeight();
    }

    void printRowContents(PdfBoxTableRow row, float x, float y) throws IOException {
        float xCell = x;
        float yCell = y;
        for (FixedWidthCell cell : row.getCells()) {
            printCellContent(cell, xCell, yCell, row.getHeight());
            xCell += cell.getWidth();
        }
    }


    /**
     * Prints the cell content centering the contents vertically and horizontally.
     *
     * @param fixedWidthCell
     * @param cell
     * @param xCell
     * @param yCell
     * @param rowHeight      @throws IOException
     */
    private void printCellContent(FixedWidthCell cell,
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

    public void verticalJump(float dy) {
        currentYPosition -= dy;
    }
}
