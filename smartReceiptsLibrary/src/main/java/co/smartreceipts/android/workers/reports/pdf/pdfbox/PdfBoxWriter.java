package co.smartreceipts.android.workers.reports.pdf.pdfbox;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.google.common.base.Preconditions;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.util.awt.AWTColor;

import org.apache.commons.io.IOUtils;

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
import co.smartreceipts.android.workers.reports.pdf.PdfBoxUtils;
import co.smartreceipts.android.workers.reports.pdf.colors.PdfColorStyle;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontSpec;
import co.smartreceipts.android.workers.reports.pdf.fonts.PdfFontStyle;
import co.smartreceipts.android.workers.reports.pdf.tables.FixedSizeImageCell;
import co.smartreceipts.android.workers.reports.pdf.tables.FixedWidthCell;
import co.smartreceipts.android.workers.reports.pdf.tables.FixedWidthTextCell;
import co.smartreceipts.android.workers.reports.pdf.tables.PdfBoxTable;
import co.smartreceipts.android.workers.reports.pdf.tables.PdfBoxTableRow;
import co.smartreceipts.android.workers.reports.pdf.utils.HeavyHandedReplaceIllegalCharacters;
import wb.android.storage.StorageManager;


/**
 * This writer class directly interacts with the underlying PdfBox layer in order to abstract that
 * away and provide simple writing functionality. Please note that by default, PdfBox treats the
 * coordinate (0, 0) as the bottom-left of the page. Since I find it more meaningful to treat (0, 0)
 * as the top-left of the page (ie the same as Android), all public methods in this class will
 * also implicitly handle this translation.
 */
public class PdfBoxWriter {
    private final PDDocument mDocument;
    private final PdfBoxContext mContext;
    private final PdfBoxPageDecorations mPageDecorations;
    private final List<PDPage> mPages;

    private float currentYPosition;
    private float topOfPageYPosition = -1;
    private float leftOfPageXPosition = 0;
    private PDPageContentStream contentStream;
    private boolean inTextBlock;

    public PdfBoxWriter(@NonNull PDDocument doc,
                        @NonNull PdfBoxContext context,
                        @NonNull PdfBoxPageDecorations pageDecorations) throws IOException {
        mDocument = doc;
        mContext = context;
        mPageDecorations = pageDecorations;
        mPages = new ArrayList<>();
    }

    /**
     * Creates a new PDF page with the decorated header and footer
     *
     * @throws IOException if this operation fails
     */
    public void newPage() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }

        PDPage page = new PDPage(mContext.getPageSize());
        mPages.add(page);
        contentStream = new PDPageContentStream(mDocument, page);
        mPageDecorations.writeHeader(contentStream);
        currentYPosition = page.getMediaBox().getHeight() - mContext.getPageMarginVertical() - mPageDecorations.getHeaderHeight();
        leftOfPageXPosition = mContext.getPageMarginHorizontal();
        topOfPageYPosition = currentYPosition;

        mPageDecorations.writeFooter(contentStream);
    }

    /**
     * Prints a {@link PDImageXObject} (usually an image or another PDF) inside our PDF
     *
     * @param image the {@link PDImageXObject} to print
     * @param x the x-coordinate location, where 0 is the left of the page
     * @param y the y-coordinate location, where 0 is the top of the page
     * @param width the width that this image requires
     * @param height the height that this image requires
     *
     * @throws IOException if something fails during the printing process
     */
    public void printPDImageXObject(@NonNull PDImageXObject image, float x, float y, float width, float height) throws IOException {
        contentStream.drawImage(image, leftOfPageXPosition + x, swapYCoordinate(y) - height, width, height);
    }

    /**
     * Prints a given string in the desired font/color combination at the specified x/y coordinates
     *
     * @param string the {@link String} to print
     * @param font the desired {@link PdfFontSpec} to write the text in
     * @param color the preferred {@link AWTColor} to use
     * @param x the x-coordinate location, where 0 is the left of the page
     * @param y the y-coordinate location, where 0 is the top of the page
     *
     * @throws IOException if something fails during the printing process
     */
    public void printText(@NonNull String string, @NonNull PdfFontSpec font, @NonNull AWTColor color, float x, float y) throws IOException {
        contentStream.setFont(font.getFont(), font.getSize());
        contentStream.setNonStrokingColor(color);
        contentStream.beginText();
        contentStream.newLineAtOffset(leftOfPageXPosition + x, swapYCoordinate(y));
        contentStream.showText(HeavyHandedReplaceIllegalCharacters.getSafeString(string));
        contentStream.endText();
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
        contentStream.showText(HeavyHandedReplaceIllegalCharacters.getSafeString(str));
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
            contentStream.setNonStrokingColor(mContext.getColorManager().getColor(PdfColorStyle.Default));
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

        final File file = cell.getFile();
        if (file != null) {
            InputStream in = new FileInputStream(file);

            final String fileExtension = StorageManager.getExtension(file);

            PDImageXObject ximage;
            if (!TextUtils.isEmpty(fileExtension)) {
                if (fileExtension.toLowerCase().equals("jpg") || fileExtension.toLowerCase().equals("jpeg")) {
                    ximage = JPEGFactory.createFromStream(mDocument, in);
                } else if (fileExtension.toLowerCase().equals("png")) {
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    ximage = LosslessFactory.createFromImage(mDocument, bitmap);
                } else if (fileExtension.toLowerCase().equals("pdf")) {
                    ximage = getXImageNative(file);
                } else {
                    Logger.warn(this, "Unrecognized image extension: {}.", fileExtension);
                    return;
                }
            } else {
                Logger.warn(this, "Unrecognized image extension: {}.", fileExtension);
                return;
            }

            if (ximage == null) {
                FixedWidthTextCell textCell = new FixedWidthTextCell(cell.getWidth(), cell.getCellPadding(),
                        mContext.getAndroidContext().getResources().getString(R.string.report_file_could_not_be_rendered),
                        mContext.getFontManager().getFont(PdfFontStyle.Default),
                        mContext.getColorManager().getColor(PdfColorStyle.Default));
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
            return LosslessFactory.createFromImage(mDocument, bitmap);
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
    private PDImageXObject getXImageNative(File file) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        PdfRenderer renderer = null;
        try {
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(parcelFileDescriptor);

            PdfRenderer.Page page = null;
            try {
                page = renderer.openPage(0);
                final int height = page.getHeight();
                final int width = page.getWidth();
                final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                return LosslessFactory.createFromImage(mDocument, bitmap);
            } finally {
                if (page != null) {
                    page.close();
                }
            }
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
            IOUtils.closeQuietly(parcelFileDescriptor);
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
            contentStream.showText(HeavyHandedReplaceIllegalCharacters.getSafeString(lines.get(i)));
            contentStream.endText();

            y -= PdfBoxUtils.getFontHeight(fontSpec);
        }
    }

    /**
     * Please note that by default, PdfBox treats the coordinate (0, 0) as the bottom-left of the
     * page. Since I find it more meaningful to treat (0, 0) as the top-left of the page (ie the
     * same as Android), we can use this method to perform the internal swap that PDF box requires.
     *
     * @param y the y-coordinate location, where 0 is the TOP of the page
     * @return y the y-coordinate location, where 0 is the BOTTOM of the page
     */
    private float swapYCoordinate(float y) {
        Preconditions.checkArgument(topOfPageYPosition > 0, "You cannot write any values until creating a new page");
        return topOfPageYPosition - y;
    }

}
