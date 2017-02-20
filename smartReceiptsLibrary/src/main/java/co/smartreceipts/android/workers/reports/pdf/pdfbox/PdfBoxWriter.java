package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.reports.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontSpec;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontStyle;
import co.smartreceipts.android.workers.reports.tables.FixedSizeImageCell;
import co.smartreceipts.android.workers.reports.tables.FixedWidthCell;
import co.smartreceipts.android.workers.reports.tables.FixedWidthTextCell;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTable;
import co.smartreceipts.android.workers.reports.tables.PdfBoxTableRow;
import wb.android.storage.StorageManager;


/**
 * Responsible for printing out content (text, tables, images) to a pdf.
 * Keeps track of the y position on the page and handles pagination if required.
 */
public class PdfBoxWriter {
    private final PDDocument mDocument;
    private final PdfBoxContext mContext;
    private final PdfBoxPageDecorations mPageDecorations;
    private final List<PDPage> mPages;

    private float currentYPosition;
    private PDPageContentStream contentStream;
    private boolean inTextBlock;


    public PdfBoxWriter(@NonNull PDDocument doc,
                        @NonNull PdfBoxContext context,
                        @NonNull PdfBoxPageDecorations pageDecorations) throws IOException {
        mDocument = doc;
        mContext = context;
        mPageDecorations = pageDecorations;
        mPages = new ArrayList<>();
        newPage();
    }


    private void newPage() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }

        PDPage page = new PDPage(mContext.getPageSize());
        mPages.add(page);
        contentStream = new PDPageContentStream(mDocument, page);
        mPageDecorations.writeHeader(contentStream);
        currentYPosition = page.getMediaBox().getHeight()
                - mContext.getPageMarginVertical()
                - mPageDecorations.getHeaderHeight();

        mPageDecorations.writeFooter(contentStream);
    }


    void openTextBlock() throws IOException {
        inTextBlock = true;
        contentStream.beginText();
        contentStream.newLineAtOffset(
                mContext.getPageMarginHorizontal(), currentYPosition);
    }

    void closeTextBlock() throws IOException {
        inTextBlock = false;
        contentStream.endText();
    }


    void writeNewLine(@NonNull PdfFontSpec spec,
                      @StringRes int resId,
                      Object... args) throws IOException {
        if (resId != 0) {
            writeNewLine(spec, mContext.getString(resId, args));
        }
    }


    void writeNewLine(PdfFontSpec spec, String str) throws IOException {
        if (!inTextBlock) {
            throw new IllegalStateException("Tried to write out text, without opening a text block first");
        }
        // set the font
        contentStream.setFont(spec.getFont(), spec.getSize());
        // calculate dy (font size + line spacing)
        float dy = spec.getSize() + mContext.getLineSpacing();
        // move the cursor by dy and update the currentYPosition
        contentStream.newLineAtOffset(0, -dy);
        currentYPosition -= dy;
        // write the text
        contentStream.showText(str);
    }

    void writeAndClose() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }

        for (PDPage page : mPages) {
            mDocument.addPage(page);
        }
    }

    void writeTable(@NonNull PdfBoxTable table) throws IOException {

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
            printRow(table.getFooterRow());
        }
    }

    void verticalJump(float dy) {
        currentYPosition -= dy;
    }

    /**
     * @param row
     * @return Whether the row was printed or not. (It might not be printed if
     * the page has no more vertical space available).
     * @throws IOException
     */
    private boolean printRow(@NonNull PdfBoxTableRow row) throws IOException {
        // Line break if required
        if (currentYPosition - row.getHeight() <
                mContext.getPageMarginVertical() + mPageDecorations.getFooterHeight()) {
            return false;
        }

        float x = mContext.getPageMarginHorizontal();
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

    private void printRowContents(@NonNull PdfBoxTableRow row, float x, float y) throws IOException {
        float xCell = x;
        for (FixedWidthCell cell : row.getCells()) {
            if (cell != null) {
                if (cell instanceof FixedWidthTextCell) {
                    printTextCellContent((FixedWidthTextCell) cell, xCell, y, row.getHeight());

                } else if (cell instanceof FixedSizeImageCell) {
                    printImageCellContent((FixedSizeImageCell) cell, xCell, y, row.getHeight());
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
                ximage = JPEGFactory.createFromStream(mDocument, in);
            } else if (fileExtension.toLowerCase().equals("png")) {
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                ximage = LosslessFactory.createFromImage(mDocument, bitmap);
            } else if (fileExtension.toLowerCase().equals("pdf")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ximage = getXImageNative(image);
                } else {
                    ximage = getXImagePdfBox(image);
                }
            } else {
                // TODO UNRECOGNIZED IMAGE
                return;
            }

            if (ximage == null) {
                FixedWidthTextCell textCell = new FixedWidthTextCell(cell.getWidth(), cell.getCellPadding(),
                        mContext.getAndroidContext().getResources().getString(R.string.report_file_could_not_be_rendered),
                        mContext.getFontManager().getFont(PdfFontStyle.Default),
                        AWTColor.BLACK);
                printTextCellContent(textCell, xCell, yCell, height);
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

    private PDImageXObject getXImagePdfBox(File image) {
        PDDocument document = null;
        try {
            document = PDDocument.load(image);
            PDFRenderer renderer = new PDFRenderer(document);
            Bitmap bitmap = renderer.renderImage(0, 1, Bitmap.Config.ARGB_8888);
            return JPEGFactory.createFromImage(mDocument, bitmap);
        } catch (IOException e) {
            Logger.error(this, "Error while rendering PDF using PDFBox renderer", e);
            ((SmartReceiptsApplication) (mContext.getAndroidContext().getApplicationContext()))
                    .getAnalyticsManager().record(Events.Generate.ReportPdfRenderingError);

            return null;
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    // we can ignore this
                }
            }
        }
    }


    @Nullable
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private PDImageXObject getXImageNative(File image) {
        PdfRenderer renderer = null;
        try {
            renderer = new PdfRenderer(ParcelFileDescriptor.open(image, ParcelFileDescriptor.MODE_READ_ONLY));
            PdfRenderer.Page page = renderer.openPage(0);
            int h = 300 / 72 * page.getHeight();
            int w = 300 / 72 * page.getWidth();

            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Rect rect = new Rect(0, 0, w, h);
            page.render(bitmap, rect, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();

            return JPEGFactory.createFromImage(mDocument, bitmap);
        } catch (SecurityException e) {
            Logger.error(this, "PDF file is password-protected", e);
            return null;
        } catch (IOException e) {
            Logger.error(this, "Error while rendering PDF using native renderer", e);
            return null;
        } finally {
            if (renderer != null) {
                renderer.close();
            }
        }

    }


    /**
     * Prints the cell content centering the contents vertically and horizontally.
     *
     * @param cell
     * @param xCell     x-coordinate of the upper-left corner
     * @param yCell     y-coordinate of the upper-left corner
     * @param rowHeight @throws IOException
     */
    private void printTextCellContent(FixedWidthTextCell cell,
                                      float xCell,
                                      float yCell,
                                      float rowHeight) throws IOException {

        List<String> lines = cell.getLines();

        PdfFontSpec fontSpec = cell.getFontSpec();

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
