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
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
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
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.CSVColumns;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.PDFColumns;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.comparators.ReceiptDateComparator;
import co.smartreceipts.android.model.converters.DistanceToReceiptsConverter;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.workers.reports.DistanceTableGenerator;
import co.smartreceipts.android.workers.reports.columns.DistanceTableColumns;
import co.smartreceipts.android.workers.reports.columns.TableColumns;
import co.smartreceipts.android.workers.reports.writers.CsvTableGenerator;
import co.smartreceipts.android.workers.reports.writers.PdfTableGenerator;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;

//TODO: Redo this class... Really sloppy
public class EmailAssistant {

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

    private static final String TAG = "EmailAssistant";

    private static final Rectangle DEFAULT_PAGE_SIZE = PageSize.A4;
    private static final float EPSILON = 0.0001f;

    private final Context mContext;
    private final Flex mFlex;
    private final PersistenceManager mPersistenceManager;
    private final Trip mTrip;

    public static final void email(SmartReceiptsApplication app, Context context, Trip trip) {
        EmailAssistant assistant = new EmailAssistant(context, app.getFlex(), app.getPersistenceManager(), trip);
        assistant.emailTrip();
    }

    public static final Intent getEmailDeveloperIntent() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + "will.r.b" + "aumann" + "@" + "gm" + "ail" + "." + "com"));
        return intent;
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

    public EmailAssistant(Context context, Flex flex, PersistenceManager persistenceManager, Trip trip) {
        mContext = context;
        mFlex = flex;
        mPersistenceManager = persistenceManager;
        mTrip = trip;
    }

    public final void emailTrip() {
        if (!mPersistenceManager.getStorageManager().isExternal()) {
            Toast.makeText(mContext, mFlex.getString(mContext, R.string.SD_ERROR), Toast.LENGTH_LONG).show();
            return;
        }
        View scrollView = mFlex.getView(mContext, R.layout.dialog_email);
        final CheckBox pdfFull = (CheckBox) mFlex.getSubView(mContext, scrollView, R.id.DIALOG_EMAIL_CHECKBOX_PDF_FULL);
        final CheckBox pdfImages = (CheckBox) mFlex.getSubView(mContext, scrollView, R.id.DIALOG_EMAIL_CHECKBOX_PDF_IMAGES);
        final CheckBox csv = (CheckBox) mFlex.getSubView(mContext, scrollView, R.id.DIALOG_EMAIL_CHECKBOX_CSV);
        final CheckBox zipStampedImages = (CheckBox) mFlex.getSubView(mContext, scrollView, R.id.DIALOG_EMAIL_CHECKBOX_ZIP_IMAGES_STAMPED);
        final BetterDialogBuilder builder = new BetterDialogBuilder(mContext);
        String msg = mFlex.getString(mContext, R.string.DIALOG_EMAIL_MESSAGE);
        if (msg.length() > 0) {
            builder.setMessage(msg);
        }
        builder.setTitle(mFlex.getString(mContext, R.string.DIALOG_EMAIL_TITLE))
                .setCancelable(true)
                .setView(scrollView)
                .setPositiveButton(mFlex.getString(mContext, R.string.DIALOG_EMAIL_POSITIVE_BUTTON), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (!pdfFull.isChecked() && !pdfImages.isChecked() && !csv.isChecked() && !zipStampedImages.isChecked()) {
                            Toast.makeText(mContext, mFlex.getString(mContext, R.string.DIALOG_EMAIL_TOAST_NO_SELECTION), Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            return;
                        }
                        if (mPersistenceManager.getDatabase().getReceiptsSerial(mTrip).isEmpty()) {
                            if (mPersistenceManager.getDatabase().getDistanceSerial(mTrip).isEmpty() || !pdfFull.isChecked()) {
                                // Only allow report processing to continue with no reciepts if we're doing a full pdf report with distances
                                Toast.makeText(mContext, mFlex.getString(mContext, R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS), Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                                return;
                            } else {
                                // Uncheck "Illegal" Items
                                pdfImages.setChecked(false);
                                csv.setChecked(false);
                                zipStampedImages.setChecked(false);
                            }
                        }
                        ProgressDialog progress = ProgressDialog.show(mContext, "", "Building Reports...", true, false);
                        EnumSet<EmailOptions> options = EnumSet.noneOf(EmailOptions.class);
                        if (pdfFull.isChecked()) {
                            options.add(EmailOptions.PDF_FULL);
                        }
                        if (pdfImages.isChecked()) {
                            options.add(EmailOptions.PDF_IMAGES_ONLY);
                        }
                        if (csv.isChecked()) {
                            options.add(EmailOptions.CSV);
                        }
                        if (zipStampedImages.isChecked()) {
                            options.add(EmailOptions.ZIP_IMAGES_STAMPED);
                        }
                        EmailAttachmentWriter attachmentWriter = new EmailAttachmentWriter(mPersistenceManager, progress, options);
                        attachmentWriter.execute(mTrip);
                    }
                })
                .setNegativeButton(mFlex.getString(mContext, R.string.DIALOG_EMAIL_NEGATIVE_BUTTON), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    public void onAttachmentsCreated(File[] attachments) {
        ArrayList<Uri> uris = new ArrayList<Uri>();
        StringBuilder bodyBuilder = new StringBuilder();
        String path = "";
        if (attachments[EmailOptions.PDF_FULL.getIndex()] != null) {
            path = attachments[EmailOptions.PDF_FULL.getIndex()].getParentFile().getAbsolutePath();
            uris.add(Uri.fromFile(attachments[EmailOptions.PDF_FULL.getIndex()]));
            if (attachments[EmailOptions.PDF_FULL.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(mContext.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.PDF_FULL.getIndex()].getAbsolutePath()));
            }
        }
        if (attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()] != null) {
            path = attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()].getParentFile().getAbsolutePath();
            uris.add(Uri.fromFile(attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()]));
            if (attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(mContext.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()].getAbsolutePath()));
            }
        }
        if (attachments[EmailOptions.CSV.getIndex()] != null) {
            path = attachments[EmailOptions.CSV.getIndex()].getParentFile().getAbsolutePath();
            uris.add(Uri.fromFile(attachments[EmailOptions.CSV.getIndex()]));
            if (attachments[EmailOptions.CSV.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(mContext.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.CSV.getIndex()].getAbsolutePath()));
            }
        }
        if (attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()] != null) {
            path = attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()].getParentFile().getAbsolutePath();
            uris.add(Uri.fromFile(attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()]));
            if (attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()].length() > 5000000) { //Technically, this should be 5,242,880 but I'd rather give a warning buffer
                bodyBuilder.append("\n");
                bodyBuilder.append(mContext.getString(R.string.email_body_subject_5mb_warning, attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()].getAbsolutePath()));
            }
        }

        //TODO: Check if we've defined the subject in our preferences
        String body = bodyBuilder.toString();
        if (body.length() > 0) {
            body = "\n\n" + body;
        }
        if (uris.size() == 1) {
            body = mContext.getString(R.string.report_attached) + body;
        } else if (uris.size() > 1) {
            body = mContext.getString(R.string.reports_attached, Integer.toString(uris.size())) + body;
        }

		/*Works for Google Drive. Breaks the rest
        ArrayList<String> extra_text = new ArrayList<String>(); //Need this part to fix a Bundle casting bug
		if (uris.size() == 1) extra_text.add(uris.size() + " report attached");
		if (uris.size() > 1) extra_text.add(uris.size() + " reports attached");
		emailIntent.putStringArrayListExtra(Intent.EXTRA_TEXT, extra_text);
		*/

        if (!mPersistenceManager.getPreferences().getUsesFileExporerForOutputIntent()) {
            // Action Send Output
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
            emailIntent.setType("application/octet-stream");
            final String[] to = mPersistenceManager.getPreferences().getEmailTo().split(";");
            final String[] cc = mPersistenceManager.getPreferences().getEmailCC().split(";");
            final String[] bcc = mPersistenceManager.getPreferences().getEmailBCC().split(";");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(android.content.Intent.EXTRA_CC, cc);
            emailIntent.putExtra(android.content.Intent.EXTRA_BCC, bcc);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, mPersistenceManager.getPreferences().getEmailSubject().replace("%REPORT_NAME%", mTrip.getName()).replace("%USER_ID%", mPersistenceManager.getPreferences().getUserID()).replace("%REPORT_START%", mTrip.getFormattedStartDate(mContext, mPersistenceManager.getPreferences().getDateSeparator())).replace("%REPORT_END%", mTrip.getFormattedEndDate(mContext, mPersistenceManager.getPreferences().getDateSeparator())));
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
            // emailIntent.putCharSequenceArrayListExtra(Intent.EXTRA_TEXT, new ArrayList<CharSequence>(Arrays.asList(body)));
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
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
        } else {
            final Intent fileIntent = new Intent(android.content.Intent.ACTION_GET_CONTENT);
            fileIntent.setType("file/*");
            if (path != null) {
                File parentDirectory = new File(path);
                fileIntent.setData(Uri.fromFile(parentDirectory));
            }
            try {
                mContext.startActivity(Intent.createChooser(fileIntent, mContext.getString(R.string.send_file)));
            } catch (ActivityNotFoundException e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.error_no_file_intent_dialog_title)
                        .setMessage(mContext.getString(R.string.error_no_file_intent_dialog_message, path))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
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

        private static final String IMAGES_PDF = "Images.pdf";
        private static final String FOOTER = "Report Generated using Smart Receipts for Android";

        public EmailAttachmentWriter(PersistenceManager persistenceManager,
                                     ProgressDialog dialog,
                                     EnumSet<EmailOptions> options) {
            mStorageManager = persistenceManager.getStorageManager();
            mDB = persistenceManager.getDatabase();
            mPreferences = persistenceManager.getPreferences();
            mProgressDialog = new WeakReference<ProgressDialog>(dialog);
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
            final List<Receipt> receipts = mDB.getReceiptsSerial(trip, false);
            if (mPreferences.getPrintDistanceAsDailyReceipt()) {
                receipts.addAll(new DistanceToReceiptsConverter(mContext, mPreferences).convert(mDB.getDistanceSerial(trip)));
                Collections.sort(receipts, new ReceiptDateComparator());
            }
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
                FileOutputStream pdfStream = null;
                Document document = null;
                PdfWriter writer = null;
                try {
                    //Setup work
                    mStorageManager.delete(dir, dir.getName() + ".pdf");
                    pdfStream = mStorageManager.getFOS(dir, dir.getName() + ".pdf");
                    document = new Document();
                    writer = PdfWriter.getInstance(document, pdfStream);
                    writer.setPageEvent(new Footer());
                    document.open();

                    // Pre-tax => receipt total does not include price
                    final boolean usePrexTaxPrice = mPreferences.getUsesPreTaxPrice();
                    final boolean onlyUseExpensable = mPreferences.onlyIncludeExpensableReceiptsInReports();
                    final ArrayList<Price> netTotal = new ArrayList<Price>(receipts.size());
                    final ArrayList<Price> receiptTotal = new ArrayList<Price>(receipts.size());
                    final ArrayList<Price> expensableTotal = new ArrayList<Price>(receipts.size());
                    final ArrayList<Price> taxTotal = new ArrayList<Price>(receipts.size());
                    final ArrayList<Price> distanceTotal = new ArrayList<Price>(distances.size());

                    for (int i = 0; i < len; i++) {
                        final Receipt receipt = receipts.get(i);
                        if (!onlyUseExpensable || receipt.isExpensable()) {
                            netTotal.add(receipt.getPrice());
                            receiptTotal.add(receipt.getPrice());
                            taxTotal.add(receipt.getTax());
                            if (usePrexTaxPrice) {
                                netTotal.add(receipt.getTax());
                            }
                            if (receipt.isExpensable()) {
                                expensableTotal.add(receipt.getPrice());
                            }
                        }
                    }

                    final List<Distance> distances = new ArrayList<Distance>(mDB.getDistanceSerial(trip));
                    Collections.reverse(distances); // Reverse the list, so we start with the earliest one
                    for (int i = 0; i < distances.size(); i++) {
                        final Distance distance = distances.get(i);
                        netTotal.add(distance.getPrice());
                        distanceTotal.add(distance.getPrice());
                    }

                    final Price netPrice = new PriceBuilderFactory().setPrices(netTotal).build();
                    final Price receiptsPrice = new PriceBuilderFactory().setPrices(receiptTotal).build();
                    final Price expensablePrice = new PriceBuilderFactory().setPrices(expensableTotal).build();
                    final Price taxPrice = new PriceBuilderFactory().setPrices(taxTotal).build();
                    final Price distancePrice = new PriceBuilderFactory().setPrices(distanceTotal).build();


                    // Add the table (TODO: Use formatting at some point so it doesn't look like crap)
                    document.add(new Paragraph(dir.getName() + "\n"));
                    if (!receiptsPrice.equals(netPrice)) {
                        document.add(new Paragraph(mContext.getString(R.string.report_header_receipts_total, receiptsPrice.getCurrencyFormattedPrice()) + "\n"));
                    }
                    if (mPreferences.includeTaxField() && taxPrice.getPriceAsFloat() > EPSILON) {
                        document.add(new Paragraph(mContext.getString(R.string.report_header_price_no_tax, taxPrice.getCurrencyFormattedPrice()) + "\n"));
                    }
                    if (!mPreferences.onlyIncludeExpensableReceiptsInReports() && !expensablePrice.equals(receiptsPrice)) {
                        document.add(new Paragraph(mContext.getString(R.string.report_header_receipts_total_expensable, expensablePrice.getCurrencyFormattedPrice()) + "\n"));
                    }
                    if (distances.size() > 0) {
                        document.add(new Paragraph(mContext.getString(R.string.report_header_distance_total, distancePrice.getCurrencyFormattedPrice()) + "\n"));
                    }
                    document.add(new Paragraph(mContext.getString(R.string.report_header_net_total, trip.getPrice().getCurrencyFormattedPrice()) + "\n"));
                    document.add(new Paragraph(mContext.getString(R.string.report_header_from, trip.getFormattedStartDate(mContext, mPreferences.getDateSeparator())) + " "
                            + mContext.getString(R.string.report_header_to, trip.getFormattedEndDate(mContext, mPreferences.getDateSeparator())) + "\n\n\n"));
                    PDFColumns columns = mDB.getPDFColumns();
                    PdfPTable table = columns.getTableWithHeaders();
                    Receipt receipt;
                    for (int i = 0; i < len; i++) {
                        receipt = receipts.get(i);
                        if (!filterOutReceipt(mPreferences, receipt)) {
                            columns.print(table, receipt, trip);
                        }
                    }
                    document.add(table);

                    if (mPreferences.getPrintDistanceTable()) {
                        final TableColumns distanceTableColumns = new DistanceTableColumns(mContext, mPreferences, distances);
                        document.add(new Paragraph("\n\n"));
                        document.add(new PdfTableGenerator().write(distanceTableColumns));
                    }
                    document.newPage();

                    // Add image Rows
                    this.addImageRows(document, receipts, writer);
                    final File signature = mPreferences.getSignaturePhoto();
                    if (signature != null) {
                        document.newPage();
                        document.add(new Paragraph(mContext.getString(R.string.signature) + "\n\n"));
                        document.add(Image.getInstance(signature.getAbsolutePath()));
                    }
                    mFiles[EmailOptions.PDF_FULL.getIndex()] = mStorageManager.getFile(dir, dir.getName() + ".pdf");
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.toString(), e);
                    }
                    results.didPDFFailCompletely = true; //TODO: Add error messages to each of these
                } catch (DocumentException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.toString(), e);
                    }
                    results.didPDFFailCompletely = true; //TODO: Add error messages to each of these
                } finally {
                    if (document != null) {
                        document.close(); //Close me first
                    }
                    if (pdfStream != null) {
                        StorageManager.closeQuietly(pdfStream);
                    }
                }
            }
            if (mOptions.contains(EmailOptions.PDF_IMAGES_ONLY)) {
                FileOutputStream pdfStream = null;
                Document document = null;
                PdfWriter writer = null;
                try {
                    //Setup work
                    mStorageManager.delete(dir, dir.getName() + IMAGES_PDF);
                    pdfStream = mStorageManager.getFOS(dir, dir.getName() + IMAGES_PDF);
                    document = new Document();
                    writer = PdfWriter.getInstance(document, pdfStream);
                    writer.setPageEvent(new Footer());
                    document.open();

                    // Add image Rows
                    this.addImageRows(document, receipts, writer);

                    mFiles[EmailOptions.PDF_IMAGES_ONLY.getIndex()] = mStorageManager.getFile(dir, dir.getName() + IMAGES_PDF);
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.toString(), e);
                    }
                    results.didSimplePDFFailCompletely = true; //TODO: Add error messages to each of these
                } catch (DocumentException e) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, e.toString(), e);
                    }
                    results.didSimplePDFFailCompletely = true; //TODO: Add error messages to each of these
                } finally {
                    try {
                        if (document != null) {
                            document.close();
                        }
                    } catch (RuntimeException e) {
                        // Document has no pages exception
                    }
                    if (pdfStream != null) {
                        StorageManager.closeQuietly(pdfStream);
                    }
                }
            }
            if (mOptions.contains(EmailOptions.CSV)) {
                mStorageManager.delete(dir, dir.getName() + ".csv");
                String data = "";
                CSVColumns columns = mDB.getCSVColumns();
                if (mPreferences.includeCSVHeaders()) {
                    data += columns.printHeaders();
                }
                for (int i = 0; i < len; i++) {
                    if (!filterOutReceipt(mPreferences, receipts.get(i))) {
                        data += columns.print(receipts.get(i), trip);
                    }
                }
                if (mPreferences.getPrintDistanceTable()) {
                    final List<Distance> distances = new ArrayList<Distance>(mDB.getDistanceSerial(trip));
                    Collections.reverse(distances); // Reverse the list, so we print the most recent one first
                    final TableColumns distanceTableColumns = new DistanceTableColumns(mContext, mPreferences, distances);
                    data += "\n\n";
                    data += new CsvTableGenerator().write(distanceTableColumns);
                }
                String filename = dir.getName() + ".csv";
                if (!mStorageManager.write(dir, filename, data)) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Failed to write the csv file");
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
                                mStorageManager.writeBitmap(dir, b, (i + 1) + "_" + receipts.get(i).getName() + ".jpg", CompressFormat.JPEG, 85);
                                b.recycle();
                                b = null;
                            }
                        } catch (OutOfMemoryError e) {
                            System.gc();
                            try {
                                Bitmap b = stampImage(trip, receipts.get(i), Bitmap.Config.RGB_565);
                                if (b != null) {
                                    mStorageManager.writeBitmap(dir, b, (i + 1) + "_" + receipts.get(i).getName() + ".jpg", CompressFormat.JPEG, 85);
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
            if (preferences.onlyIncludeExpensableReceiptsInReports() && !receipt.isExpensable()) {
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
                canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_CATEGORY) + ": " + receipt.getCategory(), xPad / 2, y, brush);
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
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, e.toString(), e);
                        }
                        continue;
                    }
                } else if (receipt.hasImage() && img2 == null) {
                    try {
                        img2 = Image.getInstance(receipt.getFilePath());
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, e.toString());
                        }
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
                            if (BuildConfig.DEBUG) {
                                Log.e(TAG, e.toString());
                            }
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
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString(), e);
                }
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
            int num = (mPreferences.includeReceiptIdInsteadOfIndexByPhoto()) ? receipt.getId() : receipt.getIndex();
            table.addCell(num + "  \u2022  " + receipt.getName() + "  \u2022  " + receipt.getFormattedDate(mContext, mPreferences.getDateSeparator()));
        }

        private void addFullPageImage(Document document, Receipt receipt, PdfWriter writer) {
            if (mPreferences.onlyIncludeExpensableReceiptsInReports() && !receipt.isExpensable()) {
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
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString(), e);
                }
                return;
            } catch (BadPdfFormatException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString(), e);
                }
                return;
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString(), e);
                }
                return;
            } catch (BadElementException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString(), e);
                }
                return;
            } catch (DocumentException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e.toString());
                }
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
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, e1.toString(), e1);
                }
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
                        new Phrase(FOOTER, FontFactory.getFont("Times-Roman", 9, Font.ITALIC)), rect.getLeft() + 36, rect.getBottom() + 36, 0);
            }
        }

    }

}