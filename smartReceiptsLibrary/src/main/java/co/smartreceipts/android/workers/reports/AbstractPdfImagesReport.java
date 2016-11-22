package co.smartreceipts.android.workers.reports;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.utils.Logger;
import co.smartreceipts.android.workers.reports.formatting.SmartReceiptsFormattableString;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

/**
 * Provides an abstract implementation of the {@link co.smartreceipts.android.workers.reports.Report} contract
 * for Pdf
 */
abstract class AbstractPdfImagesReport extends AbstractReport {

    private static final Rectangle DEFAULT_PAGE_SIZE = PageSize.A4;
    private static final int DEFAULT_MARGIN = 36;
    private static final int DEFAULT_MARGIN_BOTTOM = 50;
    private static final float BIG_COLUMN_DIVIDER = 2.1f;

    private final String mFooterMessage;

    protected AbstractPdfImagesReport(@NonNull Context context, @NonNull PersistenceManager persistenceManager, Flex flex) {
        this(context, persistenceManager, flex, persistenceManager.getPreferences().getPdfFooterText());
    }

    protected AbstractPdfImagesReport(@NonNull Context context, @NonNull PersistenceManager persistenceManager, Flex flex, @NonNull String footerMessage) {
        super(context, persistenceManager, flex);
        mFooterMessage = footerMessage;
    }

    protected AbstractPdfImagesReport(@NonNull Context context, @NonNull DatabaseHelper db, @NonNull Preferences preferences, @NonNull StorageManager storageManager, Flex flex) {
        this(context, db, preferences, storageManager, flex, preferences.getPdfFooterText());
    }

    protected AbstractPdfImagesReport(@NonNull Context context, @NonNull DatabaseHelper db, @NonNull Preferences preferences, @NonNull StorageManager storageManager, Flex flex, @NonNull String footerMessage) {
        super(context, db, preferences, storageManager, flex);
        mFooterMessage = footerMessage;
    }


    @Override
    @NonNull
    public final File generate(@NonNull Trip trip) throws ReportGenerationException {
        final String outputFileName = getFileName(trip);
        FileOutputStream pdfStream = null;
        Document document = null;
        try {
            // Setup work for pdf docs
            getStorageManager().delete(trip.getDirectory(), outputFileName);
            pdfStream = getStorageManager().getFOS(trip.getDirectory(), outputFileName);
            document = new Document(DEFAULT_PAGE_SIZE, DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN_BOTTOM);

            final PdfWriter writer = PdfWriter.getInstance(document, pdfStream);
            writer.setPageEvent(new Footer(trip));

            // Open the document
            document.open();

            final List<Receipt> receipts = new ArrayList<>(getDatabase().getReceiptsTable().getBlocking(trip, false));

            // Allow subclasses to add their own behavior to the header pages
            generateInitialPages(document, receipts, trip);

            // Add images to the base of the PDF
            this.addImageRows(document, receipts, writer);

            // Add a signature page (if needed)
            final File signature = getPreferences().getSignaturePhoto();
            if (signature != null) {
                document.newPage();
                document.add(new Paragraph(getContext().getString(R.string.signature) + "\n\n"));
                document.add(Image.getInstance(signature.getAbsolutePath()));
            }


            return getStorageManager().getFile(trip.getDirectory(), outputFileName);
        } catch (IOException e) {
            Logger.error(this, e);
            throw new ReportGenerationException(e);
        } catch (DocumentException e) {
            Logger.error(this, e);
            throw new ReportGenerationException(e);
        } finally {
            if (document != null) {
                try {
                    document.close(); //Close me first
                } catch (Exception e) {
                    // We might see this if it was an empty document
                    Logger.error(this, e);
                }
            }
            if (pdfStream != null) {
                StorageManager.closeQuietly(pdfStream);
            }
        }
    }

    /**
     * Gets the name of the file for a given trip
     *
     * @param trip the {@link co.smartreceipts.android.model.Trip} for which this report belongs
     * @return the name of the report
     */
    protected abstract String getFileName(@NonNull Trip trip);

    /**
     * Allows subclasses of this class to (optionally) add a series of initial pages to the PDF printer before
     * it prints the grid of photos
     *
     * @param document the pdf {@link com.itextpdf.text.Document}
     * @param receipts the current list of receipts
     * @param trip     the current trip
     * @throws co.smartreceipts.android.workers.reports.ReportGenerationException if something failed to generate
     */
    protected abstract void generateInitialPages(@NonNull Document document, @NonNull List<Receipt> receipts, @NonNull Trip trip) throws ReportGenerationException;

    private Document addImageRows(@NonNull Document document, @NonNull List<Receipt> receipts, @NonNull PdfWriter writer) {
        // Set up
        PdfPTable table = getPanedPdfPTable();
        final int size = receipts.size();
        Receipt receipt;
        Image img1 = null, img2 = null;
        Receipt receipt1 = null; // Tracks the receipt in the left column (if any)
        int flag = 0;
        boolean hitFirstNonFullPage = false;
        final ArrayList<Receipt> fullpageReceipts = new ArrayList<Receipt>(); //Includes Full Page Images && PDFs

        for (int i = 0; i < size; i++) {
            receipt = receipts.get(i);
            if (filterOutReceipt(receipt)) { // Don't include receipts that have been explicitly filtered out
                continue;
            }
            if (receipt.isFullPage() || receipt.hasPDF()) { // Don't include full page or PDFs yet (add at the end)
                if (!hitFirstNonFullPage) {
                    addFullPageImage(document, receipt, writer); // If it's fullpage first, add immediately
                } else {
                    fullpageReceipts.add(receipt); // Else, add to the end
                }
                continue;
            }
            if (!receipt.hasImage()) { // Don't include receipts without images (called after the PDF line, b/c PDFs shouldn't be removed)
                continue;
            }
            hitFirstNonFullPage = true;
            if (receipt.hasImage() && img1 == null) {
                try {
                    img1 = Image.getInstance(receipt.getFilePath());
                    receipt1 = receipt;
                } catch (Exception e) {
                    Logger.error(this, e);
                    continue;
                }
            } else if (receipt.hasImage() && img2 == null) {
                try {
                    img2 = Image.getInstance(receipt.getFilePath());
                } catch (Exception e) {
                    Logger.error(this, e);
                    continue;
                }
                addHeaderCell(table, receipt1);
                table.addCell("");
                addHeaderCell(table, receipt);
                table.addCell(getCell(img1));
                table.addCell("");
                table.addCell(getCell(img2));
                table.setSpacingAfter(40);
                img1 = null;
                img2 = null;
                if (++flag == 2) {//ugly hack to fix how page breaks are separated
                    table.completeRow();
                    try {
                        document.add(table);
                    } catch (DocumentException e) {
                        Logger.error(this, e);
                    }
                    table = getPanedPdfPTable();
                    flag = 0;
                }
            }
        }
        if (img1 != null) {
            addHeaderCell(table, receipt1);
            table.addCell(" ");
            table.addCell(" ");
            table.addCell(getCell(img1));
        }
        table.completeRow();
        try {
            document.add(table);
        } catch (DocumentException e) {
            Logger.error(this, e);
        }
        document.newPage(); //TODO: See if this code can be made more linear (i.e. add columns dyanmically instead of doing it all at once)

        //Full Page Stuff Below
        for (int i = 0; i < fullpageReceipts.size(); i++) {
            receipt = fullpageReceipts.get(i);
            addFullPageImage(document, receipt, writer);
        }
        return document;
    }

    private void addHeaderCell(PdfPTable table, Receipt receipt) {
        final int num = (getPreferences().includeReceiptIdInsteadOfIndexByPhoto()) ? receipt.getId() : receipt.getIndex();
        final String extra = (getPreferences().getIncludeCommentByReceiptPhoto() && !TextUtils.isEmpty(receipt.getComment())) ? "  \u2022  " + receipt.getComment() : "";
        table.addCell(num + "  \u2022  " + receipt.getName() + "  \u2022  " + receipt.getFormattedDate(getContext(), getPreferences().getDateSeparator()) + extra);
    }

    private void addFullPageImage(Document document, Receipt receipt, PdfWriter writer) {
        if (getPreferences().onlyIncludeReimbursableReceiptsInReports() && !receipt.isReimbursable()) {
            return;
        }
        if (receipt.getPrice().getPriceAsFloat() < getPreferences().getMinimumReceiptPriceToIncludeInReports()) {
            return;
        }
        PdfPTable table;
        try {
            if (receipt.hasPDF()) {
                PdfReader reader = new PdfReader(new FileInputStream(receipt.getFile()));
                PdfReader.unethicalreading = true; // Reduce errors due to "owner password not found"
                int numPages = reader.getNumberOfPages();
                for (int page = 0; page < numPages; ) {
                    table = getSingleElementTable();
                    addHeaderCell(table, receipt);
                    try {
                        table.addCell(Image.getInstance(writer.getImportedPage(reader, ++page)));
                    } catch (RuntimeException e) {
                        if (getContext() != null) {
                            // TODO: Move this check up a level (i.e. to our model/attachment), so we can alert the user in advance
                            if (getContext() instanceof Activity) {
                                // TODO: Figure out a real reporting mechanism, so we're not just randomly throwing out toast messages
                                Activity activity = (Activity) getContext();
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), getContext().getString(R.string.toast_error_password_protected_pdf), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        break;
                    }
                    table.completeRow();
                    document.add(table);
                    document.newPage();
                }
                writer.freeReader(reader);
                reader = null; //
            } else if (receipt.isFullPage() && receipt.hasFile()) {
                table = getSingleElementTable();
                addHeaderCell(table, receipt);
                table.addCell(getFullCell(Image.getInstance(receipt.getFilePath())));
                table.completeRow();
                document.add(table);
                document.newPage();
            }
        } catch (FileNotFoundException e) {
            Logger.error(this, e);
        } catch (BadPdfFormatException e) {
            Logger.error(this, e);
        } catch (IOException e) {
            Logger.error(this, e);
        } catch (BadElementException e) {
            Logger.error(this, e);
        } catch (DocumentException e) {
            Logger.error(this, e);
        }
    }

    private PdfPTable getSingleElementTable() {
        final PdfPTable table = new PdfPTable(1);
        table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
        table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
        table.getDefaultCell().disableBorderSide(PdfPCell.TOP);
        table.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
        table.setWidthPercentage(100);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
        table.setSplitLate(false);
        return table;
    }

    private PdfPTable getPanedPdfPTable() {
        // Here we create a table with 3 columns - col 1 holds img 1, col 2 is a divider, col 3 is img 2
        final PdfPTable table = new PdfPTable(3);
        table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
        table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
        table.getDefaultCell().disableBorderSide(PdfPCell.TOP);
        table.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
        table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        table.setWidthPercentage(100);
        float big = DEFAULT_PAGE_SIZE.getWidth() / BIG_COLUMN_DIVIDER;
        float small = DEFAULT_PAGE_SIZE.getWidth() - 2 * big;
        try {
            table.setWidths(new float[]{big, small, big});
        } catch (DocumentException e1) {
            Logger.error(this, e1);
        }
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
        table.setSplitLate(false);
        return table;
    }

    private PdfPCell getCell(Image img) {
        final PdfPCell cell = new PdfPCell(img, true);
        cell.disableBorderSide(PdfPCell.LEFT);
        cell.disableBorderSide(PdfPCell.RIGHT);
        cell.disableBorderSide(PdfPCell.TOP);
        cell.disableBorderSide(PdfPCell.BOTTOM);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setFixedHeight(DEFAULT_PAGE_SIZE.getHeight() / 2.4f);
        return cell;
    }

    private PdfPCell getFullCell(Image img) {
        final PdfPCell cell = new PdfPCell(img, true);
        cell.disableBorderSide(PdfPCell.LEFT);
        cell.disableBorderSide(PdfPCell.RIGHT);
        cell.disableBorderSide(PdfPCell.TOP);
        cell.disableBorderSide(PdfPCell.BOTTOM);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setFixedHeight(DEFAULT_PAGE_SIZE.getHeight() / 1.15f);
        return cell;
    }

    private class Footer extends PdfPageEventHelper {
        private final Trip mTrip;

        public Footer(@NonNull Trip trip) {
            mTrip = trip;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            final String formattedFooterMessage = new SmartReceiptsFormattableString(mFooterMessage, getContext(), mTrip, getPreferences()).toString();
            Rectangle rect = writer.getPageSize();
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT,
                    new Phrase(formattedFooterMessage, FontFactory.getFont("Times-Roman", 9, Font.ITALIC)), rect.getLeft() + 36, rect.getBottom() + 36, 0);
        }
    }

}
