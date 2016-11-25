package co.smartreceipts.android.workers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.filters.LegacyReceiptFilter;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.impl.columns.distance.DistanceColumnDefinitions;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.utils.IntentUtils;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.reports.FullPdfReport;
import co.smartreceipts.android.workers.reports.ImagesOnlyPdfReport;
import co.smartreceipts.android.workers.reports.Report;
import co.smartreceipts.android.workers.reports.ReportGenerationException;
import co.smartreceipts.android.workers.reports.formatting.SmartReceiptsFormattableString;
import co.smartreceipts.android.workers.reports.tables.CsvTableGenerator;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

//TODO: Redo this class... Really sloppy
public class EmailAssistant {

    private static final String DEVELOPER_EMAIL = "will.r.b" + "aumann" + "@" + "gm" + "ail" + "." + "com";

    public enum EmailOptions {
        PDF_FULL(0),
        PDF_IMAGES_ONLY(1),
        CSV(2),
        ZIP_IMAGES_STAMPED(3);

        private final int index;

        EmailOptions(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }
    }

    private static final Rectangle DEFAULT_PAGE_SIZE = PageSize.A4;

    private final Context mContext;
    private final Flex mFlex;
    private final PersistenceManager mPersistenceManager;
    private final Trip mTrip;

    public static final Intent getEmailDeveloperIntent() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        setEmailDeveloperRecipient(intent);
        intent.setData(Uri.parse("mailto:" + DEVELOPER_EMAIL));
        return intent;
    }

    private static void setEmailDeveloperRecipient(Intent intent) {
        intent.setData(Uri.parse("mailto:" + "will.r.b" + "aumann" + "@" + "gm" + "ail" + "." + "com"));
    }

    public static final Intent getEmailDeveloperIntent(String subject) {
        Intent intent = getEmailDeveloperIntent();
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        return intent;
    }


    public static final Intent getEmailDeveloperIntent(String subject, String body) {
        Intent intent = getEmailDeveloperIntent(subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        return intent;
    }

    public static final Intent getEmailDeveloperIntent(Context context, String subject, String body, List<File> files) {
        Intent intent = IntentUtils.getSendIntent(context, files);
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{DEVELOPER_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        return intent;
    }

    public EmailAssistant(Context context, Flex flex, PersistenceManager persistenceManager, Trip trip) {
        mContext = context;
        mFlex = flex;
        mPersistenceManager = persistenceManager;
        mTrip = trip;
    }

    public void emailTrip(@NonNull EnumSet<EmailOptions> options) {
        ProgressDialog progress = ProgressDialog.show(mContext, "", "Building Reports...", true, false);
        EmailAttachmentWriter attachmentWriter = new EmailAttachmentWriter(mPersistenceManager, progress, options);
        attachmentWriter.execute(mTrip);
    }

    public void onAttachmentsCreated(File[] attachments) {
        List<File> files = new ArrayList<File>();
        StringBuilder bodyBuilder = new StringBuilder();
        String path = "";
        if (attachments[EmailOptions.PDF_FULL.getIndex()] != null) {
            path = attachments[EmailOptions.PDF_FULL.getIndex()].getParentFile().getAbsolutePath();
            files.add(attachments[EmailOptions.PDF_FULL.getIndex()]);
            if (attachments[EmailOptions.PDF_FULL.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(mContext.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.PDF_FULL.getIndex()].getAbsolutePath()));
            }
        }
        if (attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()] != null) {
            path = attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()].getParentFile().getAbsolutePath();
            files.add(attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()]);
            if (attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(mContext.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()].getAbsolutePath()));
            }
        }
        if (attachments[EmailOptions.CSV.getIndex()] != null) {
            path = attachments[EmailOptions.CSV.getIndex()].getParentFile().getAbsolutePath();
            files.add(attachments[EmailOptions.CSV.getIndex()]);
            if (attachments[EmailOptions.CSV.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(mContext.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.CSV.getIndex()].getAbsolutePath()));
            }
        }
        if (attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()] != null) {
            path = attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()].getParentFile().getAbsolutePath();
            files.add(attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()]);
            if (attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(mContext.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()].getAbsolutePath()));
            }
        }

        String body = bodyBuilder.toString();
        if (body.length() > 0) {
            body = "\n\n" + body;
        }
        if (files.size() == 1) {
            body = mContext.getString(R.string.report_attached) + body;
        } else if (files.size() > 1) {
            body = mContext.getString(R.string.reports_attached, Integer.toString(files.size())) + body;
        }

        final Intent emailIntent = IntentUtils.getSendIntent(mContext, files);
        final String[] to = mPersistenceManager.getPreferences().getEmailTo().split(";");
        final String[] cc = mPersistenceManager.getPreferences().getEmailCC().split(";");
        final String[] bcc = mPersistenceManager.getPreferences().getEmailBCC().split(";");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(android.content.Intent.EXTRA_CC, cc);
        emailIntent.putExtra(android.content.Intent.EXTRA_BCC, bcc);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, new SmartReceiptsFormattableString(mPersistenceManager.getPreferences().getEmailSubject(), mContext, mTrip, mPersistenceManager.getPreferences()).toString());
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        try {
            mContext.startActivity(Intent.createChooser(emailIntent, mContext.getString(R.string.send_email)));
        } catch (ActivityNotFoundException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(R.string.error_no_send_intent_dialog_title)
                    .setMessage(mContext.getString(R.string.error_no_send_intent_dialog_message, path))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }
    }

    public static final class WriterResults {
        public boolean didPDFFailCompletely = false;
        public boolean didPDFFailParitially = false;
        public boolean didSimplePDFFailCompletely = false;
        public boolean didSimplePDFFailParitially = false;
        public boolean didCSVFailCompletely = false;
        public boolean didCSVFailParitially = false;
        public boolean didZIPFailCompletely = false;
        public boolean didZIPFailParitially = false;

        public static final WriterResults getFullFailureInstance() {
            WriterResults result = new WriterResults();
            result.didPDFFailCompletely = true;
            result.didPDFFailParitially = true;
            result.didSimplePDFFailCompletely = true;
            result.didSimplePDFFailParitially = true;
            result.didCSVFailCompletely = true;
            result.didCSVFailParitially = true;
            result.didZIPFailCompletely = true;
            result.didZIPFailParitially = true;
            return result;
        }
    }

    private class EmailAttachmentWriter extends AsyncTask<Trip, Integer, WriterResults> {

        private final StorageManager mStorageManager;
        private final DatabaseHelper mDB;
        private final Preferences mPreferences;
        private final WeakReference<ProgressDialog> mProgressDialog;
        private final File[] mFiles;
        private final EnumSet<EmailOptions> mOptions;
        private boolean memoryErrorOccured = false;

        public EmailAttachmentWriter(PersistenceManager persistenceManager,
                                     ProgressDialog dialog,
                                     EnumSet<EmailOptions> options) {
            mStorageManager = persistenceManager.getStorageManager();
            mDB = persistenceManager.getDatabase();
            mPreferences = persistenceManager.getPreferences();
            mProgressDialog = new WeakReference<>(dialog);
            mOptions = options;
            mFiles = new File[]{null, null, null, null};
            memoryErrorOccured = false;
        }

        @Override
        // TODO: Add all close(s) in finally statements
        protected WriterResults doInBackground(Trip... trips) {
            if (trips.length == 0) {
                return WriterResults.getFullFailureInstance(); //Should never be reached
            }

            // Set up our initial variables
            final Trip trip = trips[0];
            final List<Receipt> receipts = mDB.getReceiptsTable().getBlocking(trip, false);
            final int len = receipts.size();
            final WriterResults results = new WriterResults();

            // Make our trip output directory exists in a good state
            File dir = trip.getDirectory();
            if (!dir.exists()) {
                dir = mStorageManager.getFile(trip.getName());
                if (!dir.exists()) {
                    dir = mStorageManager.mkdir(trip.getName());
                }
            }

            if (mOptions.contains(EmailOptions.PDF_FULL)) {
                final Report pdfFullReport = new FullPdfReport(mContext, mPersistenceManager, mFlex);
                try {
                    mFiles[EmailOptions.PDF_FULL.getIndex()] = pdfFullReport.generate(trip);
                } catch (ReportGenerationException e) {
                    results.didPDFFailCompletely = true;
                }
            }
            if (mOptions.contains(EmailOptions.PDF_IMAGES_ONLY)) {
                final Report pdfimagesReport = new ImagesOnlyPdfReport(mContext, mPersistenceManager, mFlex);
                try {
                    mFiles[EmailOptions.PDF_IMAGES_ONLY.getIndex()] = pdfimagesReport.generate(trip);
                } catch (ReportGenerationException e) {
                    results.didPDFFailCompletely = true;
                }
            }
            if (mOptions.contains(EmailOptions.CSV)) {
                mStorageManager.delete(dir, dir.getName() + ".csv");

                final List<Column<Receipt>> csvColumns = mDB.getCSVTable().get().toBlocking().first();
                final CsvTableGenerator<Receipt> csvTableGenerator = new CsvTableGenerator<Receipt>(csvColumns, new LegacyReceiptFilter(mPreferences), true, false);
                String data = csvTableGenerator.generate(receipts);
                if (mPreferences.getPrintDistanceTable()) {
                    final List<Distance> distances = new ArrayList<>(mDB.getDistanceTable().getBlocking(trip, false));
                    if (!distances.isEmpty()) {
                        Collections.reverse(distances); // Reverse the list, so we print the most recent one first

                        // CSVs cannot print special characters
                        final ColumnDefinitions<Distance> distanceColumnDefinitions = new DistanceColumnDefinitions(mContext, mDB, mPreferences, mFlex, true);
                        final List<Column<Distance>> distanceColumns = distanceColumnDefinitions.getAllColumns();
                        data += "\n\n";
                        data += new CsvTableGenerator<>(distanceColumns, true, true).generate(distances);
                    }
                }
                String filename = dir.getName() + ".csv";
                if (!mStorageManager.write(dir, filename, data)) {
                    if (BuildConfig.DEBUG) {
                        Logger.error(this, "Failed to write the csv file");
                    }
                    results.didCSVFailCompletely = true;
                } else {
                    mFiles[EmailOptions.CSV.getIndex()] = mStorageManager.getFile(dir, filename);
                }
            }
            if (mOptions.contains(EmailOptions.ZIP_IMAGES_STAMPED)) {
                mStorageManager.delete(dir, dir.getName() + ".zip");
                dir = mStorageManager.mkdir(trip.getDirectory(), trip.getName());
                for (int i = 0; i < len; i++) {
                    if (!filterOutReceipt(mPreferences, receipts.get(i)) && receipts.get(i).hasImage()) {
                        try {
                            Bitmap b = stampImage(trip, receipts.get(i), Bitmap.Config.ARGB_8888);
                            if (b != null) {
                                mStorageManager.writeBitmap(dir, b, receipts.get(i).getImage().getName(), CompressFormat.JPEG, 85);
                                b.recycle();
                                b = null;
                            }
                        } catch (OutOfMemoryError e) {
                            System.gc();
                            try {
                                Bitmap b = stampImage(trip, receipts.get(i), Bitmap.Config.RGB_565);
                                if (b != null) {
                                    mStorageManager.writeBitmap(dir, b, receipts.get(i).getImage().getName(), CompressFormat.JPEG, 85);
                                    b.recycle();
                                }
                            } catch (OutOfMemoryError e2) {
                                results.didZIPFailCompletely = true;
                                memoryErrorOccured = true;
                                break;
                            }
                        }
                    }
                }
                File zip = mStorageManager.zipBuffered(dir, 2048);
                mStorageManager.deleteRecursively(dir);
                mFiles[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()] = zip;
            }
            return results;
        }

        /**
         * Applies a particular filter to determine whether or not this receipt should be
         * generated for this report
         *
         * @param preferences - User preferences
         * @param receipt     - The particular receipt
         * @return true if if should be filtered out, false otherwise
         */
        private boolean filterOutReceipt(Preferences preferences, Receipt receipt) {
            if (preferences.onlyIncludeReimbursableReceiptsInReports() && !receipt.isReimbursable()) {
                return true;
            } else if (receipt.getPrice().getPriceAsFloat() < preferences.getMinimumReceiptPriceToIncludeInReports()) {
                return true;
            } else {
                return false;
            }
        }

        private static final float IMG_SCALE_FACTOR = 2.1f;
        private static final float HW_RATIO = 0.75f;

        private Bitmap stampImage(final Trip trip, final Receipt receipt, Bitmap.Config config) {
            if (!receipt.hasImage()) {
                return null;
            }
            Bitmap foreground = mStorageManager.getMutableMemoryEfficientBitmap(receipt.getImage());
            if (foreground != null) { // It can be null if file not found
                // Size the image
                int foreWidth = foreground.getWidth();
                int foreHeight = foreground.getHeight();
                if (foreHeight > foreWidth) {
                    foreWidth = (int) (foreHeight * HW_RATIO);
                } else {
                    foreHeight = (int) (foreWidth / HW_RATIO);
                }

                // Set up the paddings
                int xPad = (int) (foreWidth / IMG_SCALE_FACTOR);
                int yPad = (int) (foreHeight / IMG_SCALE_FACTOR);

                // Set up an all white background for our canvas
                Bitmap background = Bitmap.createBitmap(foreWidth + xPad, foreHeight + yPad, config);
                Canvas canvas = new Canvas(background);
                canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF); //This represents White color

                // Set up the paint
                Paint dither = new Paint();
                dither.setDither(true);
                dither.setFilterBitmap(false);
                canvas.drawBitmap(foreground, (background.getWidth() - foreground.getWidth()) / 2, (background.getHeight() - foreground.getHeight()) / 2, dither);
                Paint brush = new Paint();
                brush.setAntiAlias(true);
                brush.setTypeface(Typeface.SANS_SERIF);
                brush.setColor(Color.BLACK);
                brush.setStyle(Paint.Style.FILL);
                brush.setTextAlign(Align.LEFT);

                // Set up the number of items to draw
                int num = 5;
                if (mPreferences.includeTaxField()) {
                    num++;
                }
                if (receipt.hasExtraEditText1()) {
                    num++;
                }
                if (receipt.hasExtraEditText2()) {
                    num++;
                }
                if (receipt.hasExtraEditText3()) {
                    num++;
                }
                float spacing = getOptimalSpacing(num, yPad / 2, brush);
                float y = spacing * 4;
                canvas.drawText(trip.getName(), xPad / 2, y, brush);
                y += spacing;
                canvas.drawText(trip.getFormattedStartDate(mContext, mPersistenceManager.getPreferences().getDateSeparator()) + " -- " + trip.getFormattedEndDate(mContext, mPersistenceManager.getPreferences().getDateSeparator()), xPad / 2, y, brush);
                y += spacing;
                y = background.getHeight() - yPad / 2 + spacing * 2;
                canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_NAME) + ": " + receipt.getName(), xPad / 2, y, brush);
                y += spacing;
                canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_PRICE) + ": " + receipt.getPrice().getDecimalFormattedPrice() + " " + receipt.getPrice().getCurrencyCode(), xPad / 2, y, brush);
                y += spacing;
                if (mPreferences.includeTaxField()) {
                    canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_TAX) + ": " + receipt.getTax().getDecimalFormattedPrice() + " " + receipt.getPrice().getCurrencyCode(), xPad / 2, y, brush);
                    y += spacing;
                }
                canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_DATE) + ": " + receipt.getFormattedDate(mContext, mPersistenceManager.getPreferences().getDateSeparator()), xPad / 2, y, brush);
                y += spacing;
                canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_CATEGORY) + ": " + receipt.getCategory().getName(), xPad / 2, y, brush);
                y += spacing;
                canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_COMMENT) + ": " + receipt.getComment(), xPad / 2, y, brush);
                y += spacing;
                if (receipt.hasExtraEditText1()) {
                    canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1) + ": " + receipt.getExtraEditText1(), xPad / 2, y, brush);
                    y += spacing;
                }
                if (receipt.hasExtraEditText2()) {
                    canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2) + ": " + receipt.getExtraEditText2(), xPad / 2, y, brush);
                    y += spacing;
                }
                if (receipt.hasExtraEditText3()) {
                    canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3) + ": " + receipt.getExtraEditText3(), xPad / 2, y, brush);
                    y += spacing;
                }

                // Clear out the dead data here
                foreground.recycle();
                foreground = null;

                // And return
                return background;
            } else {
                return null;
            }
        }

        private float getOptimalSpacing(int count, int space, Paint brush) {
            float fontSize = 8f; //Seed
            brush.setTextSize(fontSize);
            while (space > (count + 2) * brush.getFontSpacing()) {
                brush.setTextSize(++fontSize);
            }
            brush.setTextSize(--fontSize);
            return brush.getFontSpacing();
        }

        private static final float BIG_COLUMN_DIVIDER = 2.1f;

        private Document addImageRows(Document document, List<Receipt> receipts, PdfWriter writer) {
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
                if (filterOutReceipt(mPreferences, receipt)) { // Don't include receipts that have been explicitly filtered out
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
            final int num = (mPreferences.includeReceiptIdInsteadOfIndexByPhoto()) ? receipt.getId() : receipt.getIndex();
            final String extra = (mPreferences.getIncludeCommentByReceiptPhoto() && !TextUtils.isEmpty(receipt.getComment())) ? "  \u2022  " + receipt.getComment() : "";
            table.addCell(num + "  \u2022  " + receipt.getName() + "  \u2022  " + receipt.getFormattedDate(mContext, mPreferences.getDateSeparator()) + extra);
        }

        private void addFullPageImage(Document document, Receipt receipt, PdfWriter writer) {
            if (mPreferences.onlyIncludeReimbursableReceiptsInReports() && !receipt.isReimbursable()) {
                return;
            }
            if (receipt.getPrice().getPriceAsFloat() < mPreferences.getMinimumReceiptPriceToIncludeInReports()) {
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
                            if (mContext != null) {
                                // TODO: Move this check up a level (i.e. to our model/attachment), so we can alert the user in advance
                                if (mContext instanceof Activity) {
                                    // TODO: Figure out a real reporting mechanism, so we're not just randomly throwing out toast messages
                                    Activity activity = (Activity) mContext;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(mContext, mContext.getString(R.string.toast_error_password_protected_pdf), Toast.LENGTH_SHORT).show();
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
                return;
            } catch (BadPdfFormatException e) {
                Logger.error(this, e);
                return;
            } catch (IOException e) {
                Logger.error(this, e);
                return;
            } catch (BadElementException e) {
                Logger.error(this, e);
                return;
            } catch (DocumentException e) {
                Logger.error(this, e);
                return;
            }
        }

        private PdfPTable getSingleElementTable() {
            PdfPTable table = new PdfPTable(1);
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
            PdfPTable table = new PdfPTable(3);
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

        private final PdfPCell getCell(Image img) {
            PdfPCell cell = new PdfPCell(img, true);
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

        private final PdfPCell getFullCell(Image img) {
            PdfPCell cell = new PdfPCell(img, true);
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

        @Override
        protected void onPostExecute(WriterResults result) {
            //TODO: Use result!
            EmailAssistant.this.onAttachmentsCreated(mFiles);
            ProgressDialog dialog = mProgressDialog.get();
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (memoryErrorOccured) {
                memoryErrorOccured = false;
                Toast.makeText(mContext, "Error: Not enough memory to stamp the images. Try stopping some other apps and try again.", Toast.LENGTH_LONG).show();
            }
        }

        private class Footer extends PdfPageEventHelper {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                Rectangle rect = writer.getPageSize();
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT,
                        new Phrase(mPreferences.getPdfFooterText(), FontFactory.getFont("Times-Roman", 9, Font.ITALIC)), rect.getLeft() + 36, rect.getBottom() + 36, 0);
            }
        }

    }

}