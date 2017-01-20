package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.StringRes;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.workers.reports.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.tables.FixedSizeImageCell;
import co.smartreceipts.android.workers.reports.tables.FixedWidthCell;
import co.smartreceipts.android.workers.reports.tables.FixedWidthTextCell;
import co.smartreceipts.android.workers.reports.tables.ImagesWithLegendGrid;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTable;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableRow;
import wb.android.storage.StorageManager;


/**
 * Responsible for printing out content (text, tables, images) to a pdf.
 * Keeps track of the y position on the page and handles pagination if required.
 */
public class PdfBoxWriter {
    private PDDocument doc;
    private PdfBoxContext context;
    private List<PDPage> pages;
    private float currentYPosition;
    private PDPageContentStream contentStream;
    private boolean inTextBlock;
    private PdfBoxPageDecorations pageDecorations;


    public PdfBoxWriter(PDDocument doc,
                        PdfBoxContext context,
                        PdfBoxPageDecorations pageDecorations) throws IOException {
        this.doc = doc;
        this.context = context;
        this.pageDecorations = pageDecorations;
        pages = new ArrayList<>();
        newPage();
    }


    private void newPage() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }
        PDPage page = new PDPage(context.getPageSize());
        pages.add(page);
        contentStream = new PDPageContentStream(doc, page);
        pageDecorations.writeHeader(contentStream);
        currentYPosition = page.getMediaBox().getHeight()
                - context.getPageMarginVertical()
                - pageDecorations.getHeaderHeight();

        pageDecorations.writeFooter(contentStream);
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
            // If we must add a page break
            if (!printRow(pdfBoxTableRow)) {
                // add new page
                newPage();
                // repeat header (if available)
                if (table.getHeaderRow() != null) {
                    printRow(table.getHeaderRow());
                }
                // print out the row again
                boolean successInNewPage = printRow(pdfBoxTableRow);
                if (!successInNewPage) {
                    throw new IOException("Row does not fit in a single page...");
                }
            }
        }

        if (table.getFooterRow() != null) {

        }
    }

    public void verticalJump(float dy) {
        currentYPosition -= dy;
    }

    /**
     * @param row
     * @return Whether the row was printed or not. (It might not be printed if
     * the page has no more vertical space available).
     * @throws IOException
     */
    private boolean printRow(PdfBoxTableRow row) throws IOException {
        // Line break if required
        if (currentYPosition - row.getHeight() < context.getPageMarginVertical() + pageDecorations.getFooterHeight()) {
            return false;
        }

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
        return true;
    }


    void printRowContents(ImagesWithLegendGrid row, float x, float y) {
        float currentY = y;
        for (PdfBoxTableRow r : row.getRows()) {
            float dy = r.getHeight();
            printRowContents(row, x, currentY);
            currentY -= dy;
        }
    }

    void printRowContents(PdfBoxTableRow row, float x, float y) throws IOException {
        float xCell = x;
        float yCell = y;
        for (FixedWidthCell cell : row.getCells()) {
            if (cell != null) {
                if (cell instanceof FixedWidthTextCell) {
                    printTextCellContent((FixedWidthTextCell) cell, xCell, yCell, row.getHeight());

                } else if (cell instanceof FixedSizeImageCell) {
                    printImageCellContent((FixedSizeImageCell) cell, xCell, yCell, row.getHeight());
                }
                xCell += cell.getWidth();
            }

        }
    }

    private void printImageCellContent(FixedSizeImageCell cell, float xCell, float yCell, float height) throws IOException {

        File image = cell.getImage();
        if (image != null) {
            InputStream in = new FileInputStream(image);

            String fileExtension = StorageManager.getExtension(image);

            PDImageXObject ximage;
            if (!fileExtension.isEmpty() && fileExtension.toLowerCase().equals("jpg")
                    || fileExtension.toLowerCase().equals("jpeg")) {
                ximage = JPEGFactory.createFromStream(doc, in);
            } else if (fileExtension.toLowerCase().equals("png")) {
                // TODO, doesn't work - maybe it works on Android (not in unit text, because of the bitmap,
                // same as with pdfs below
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                ximage = LosslessFactory.createFromImage(doc, bitmap);
            } else if (fileExtension.toLowerCase().equals("pdf")) {
                PDDocument document = PDDocument.load(image);
                PDFRenderer renderer = new PDFRenderer(document);
                Bitmap bitmap = renderer.renderImage(0, 1, Bitmap.Config.RGB_565);

                // TODO need to test this on android runtime, wont't work on test
                File renderFile = new File("a.jpg");
                FileOutputStream fileOut = new FileOutputStream(renderFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
                fileOut.close();
//                OutputStream out = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//                I
                ximage = JPEGFactory.createFromImage(doc, bitmap);
            } else {
                // TODO UNRECOGNIZED IMAGE
                return;
            }


            float availableHeight = cell.getHeight() - 2 * cell.getCellPadding();
            float availableWidth = cell.getWidth() - 2 * cell.getCellPadding();

            PDRectangle rectangle = new PDRectangle(xCell + cell.getCellPadding(), yCell - cell.getHeight() + cell.getCellPadding(),
                    availableWidth, availableHeight);

            PDRectangle resizedRec = PdfBoxImageUtils.scaleImageInsideRectangle(ximage, rectangle);

            contentStream.drawImage(ximage, resizedRec.getLowerLeftX(), resizedRec.getLowerLeftY(),
                    resizedRec.getWidth(), resizedRec.getHeight());
        }
    }



    /**
     * Prints the cell content centering the contents vertically and horizontally.
     *
     * @param cell
     * @param xCell
     * @param yCell
     * @param rowHeight      @throws IOException
     */
    private void printTextCellContent(FixedWidthTextCell cell,
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
}
