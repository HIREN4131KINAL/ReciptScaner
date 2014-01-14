package wb.receiptslibrary.workers;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

import wb.android.dialog.BetterDialogBuilder;
import wb.android.flex.Flex;
import wb.android.storage.StorageManager;
import wb.receiptslibrary.BuildConfig;
import wb.receiptslibrary.CSVColumns;
import wb.receiptslibrary.R;
import wb.receiptslibrary.SmartReceiptsApplication;
import wb.receiptslibrary.model.ReceiptRow;
import wb.receiptslibrary.model.TripRow;
import wb.receiptslibrary.persistence.DatabaseHelper;
import wb.receiptslibrary.persistence.PersistenceManager;
import wb.receiptslibrary.persistence.Preferences;
import android.app.ProgressDialog;
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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

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
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

public class EmailAssistant {

	public enum EmailOptions {
		PDF_FULL (0),
		PDF_IMAGES_ONLY (1),
		CSV (2),
		ZIP_IMAGES_STAMPED (3);
		
		private final int index;
		EmailOptions(int index) { this.index = index; }
		public int getIndex() { return this.index; }
	}
	
	private static final String TAG = "EmailAssistant";
	
	private Context mContext;
	private Flex mFlex;
	private PersistenceManager mPersistenceManager;
	private TripRow mTrip;
	
	public static final void email(SmartReceiptsApplication app, Context context, TripRow trip) {
		EmailAssistant assistant = new EmailAssistant(context, app.getFlex(), app.getPersistenceManager(), trip);
		assistant.emailTrip();
	}
	
	public EmailAssistant(Context context, Flex flex, PersistenceManager persistenceManager, TripRow trip) {
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
    	if (msg.length() > 0) 
    		builder.setMessage(msg);
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
					   if (mPersistenceManager.getDatabase().getReceiptsSerial(mTrip).length == 0) {
						   Toast.makeText(mContext, mFlex.getString(mContext, R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS), Toast.LENGTH_SHORT).show();
						   dialog.cancel();
						   return;
					   }
		        	   ProgressDialog progress = ProgressDialog.show(mContext, "", "Building Reports...", true, false);
		        	   EnumSet<EmailOptions> options = EnumSet.noneOf(EmailOptions.class);
		        	   if (pdfFull.isChecked()) options.add(EmailOptions.PDF_FULL);
		        	   if (pdfImages.isChecked()) options.add(EmailOptions.PDF_IMAGES_ONLY);
		        	   if (csv.isChecked()) options.add(EmailOptions.CSV);
		        	   if (zipStampedImages.isChecked()) options.add(EmailOptions.ZIP_IMAGES_STAMPED);
		        	   EmailAttachmentWriter attachmentWriter = new EmailAttachmentWriter(mPersistenceManager, progress, options);
		        	   attachmentWriter.execute(mTrip);
		           }
		       })
		       .setNegativeButton(mFlex.getString(mContext, R.string.DIALOG_EMAIL_NEGATIVE_BUTTON), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
		       .show();
	}

	public void onAttachmentsCreated(File[] attachments) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);  
		emailIntent.setType("application/octet-stream");
		ArrayList<Uri> uris = new ArrayList<Uri>();
		if (attachments[EmailOptions.PDF_FULL.getIndex()] != null) uris.add(Uri.fromFile(attachments[EmailOptions.PDF_FULL.getIndex()]));
		if (attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()] != null) uris.add(Uri.fromFile(attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()]));
		if (attachments[EmailOptions.CSV.getIndex()] != null) uris.add(Uri.fromFile(attachments[EmailOptions.CSV.getIndex()]));
		if (attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()] != null) uris.add(Uri.fromFile(attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()]));
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{mPersistenceManager.getPreferences().getDefaultEmailReceipient()});
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, mFlex.getString(mContext, R.string.EMAIL_DATA_SUBJECT).replace("%REPORT_NAME%", mTrip.getName()).replace("%USER_ID%", mPersistenceManager.getPreferences().getUserID()));
		/*Works for Google Drive. Breaks the rest
		ArrayList<String> extra_text = new ArrayList<String>(); //Need this part to fix a Bundle casting bug
		if (uris.size() == 1) extra_text.add(uris.size() + " report attached");
		if (uris.size() > 1) extra_text.add(uris.size() + " reports attached");
		emailIntent.putStringArrayListExtra(Intent.EXTRA_TEXT, extra_text);
		*/
		if (uris.size() == 1) emailIntent.putExtra(Intent.EXTRA_TEXT, uris.size() + " report attached");
		if (uris.size() > 1) emailIntent.putExtra(Intent.EXTRA_TEXT, uris.size() + " reports attached");
		emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
    	mContext.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}
	
	private static class FullPageReceiptHolder {
		public FullPageReceiptHolder(ReceiptRow receipt, int number) { this.receipt = receipt; this.number = number; } 
		public ReceiptRow receipt;
		public int number;
	}
	
	private class EmailAttachmentWriter extends AsyncTask<TripRow, Integer, Long> {
		
		private final StorageManager mStorageManager;
		private final DatabaseHelper mDB;
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
			mProgressDialog = new WeakReference<ProgressDialog>(dialog);
			mOptions = options;
			mFiles = new File[] {null, null, null, null};
			memoryErrorOccured = false;
			//TODO: Instead of calling preferences.onlyIncludeExpensesable && prefs.minReceiptPrice Each Time, precache in the constructuor
		}
		
		@Override
		protected Long doInBackground(TripRow... trips) {
			if (trips.length == 0)
				return Long.valueOf(100L); //Should never be reached
			final TripRow trip = trips[0];
			final ReceiptRow[] receipts = mDB.getReceiptsSerial(trip, false);
			final int len = receipts.length;
			final Preferences preferences = mPersistenceManager.getPreferences();
			if (mOptions.contains(EmailOptions.PDF_FULL)) {
				try {
					File dir = trip.getDirectory();
					if (!dir.exists()) {
						dir = mStorageManager.getFile(trip.getName());
						if (!dir.exists())
							dir = mStorageManager.mkdir(trip.getName());
					}
					final FileOutputStream pdf = mStorageManager.getFOS(dir, dir.getName() + ".pdf");
		            Document document = new Document();
					PdfWriter writer = PdfWriter.getInstance(document, pdf);
					writer.setPageEvent(new Footer());
					document.open();
					
					document.add(new Paragraph(trip.getCurrencyFormattedPrice() + "  \u2022  " + dir.getName() + "\n"
								+ "From: " + trip.getFormattedStartDate(mContext, preferences.getDateSeparator())
								+ " To: " + trip.getFormattedEndDate(mContext, preferences.getDateSeparator()) + "\n"
								+ "Distance Traveled: " + trip.getMilesAsString() + "\n\n\n"));
					int columns = preferences.includeTaxField() ? 8 : 7;
					PdfPTable table = new PdfPTable(columns);
					table.setWidthPercentage(100);
					table.addCell("Name");
					table.addCell("Price");
					table.addCell("Date");
					table.addCell("Category");
					table.addCell("Comment");
					table.addCell("Expensable");
					table.addCell("Pictured");
					if (preferences.includeTaxField()) table.addCell("Tax");
					ReceiptRow receipt;
					for (int i=0; i < len; i++) {
						receipt = receipts[i];
						if (preferences.onlyIncludeExpensableReceiptsInReports() && !receipt.isExpensable()) 
							continue;
						if (Float.parseFloat(receipt.getPrice()) < preferences.getMinimumReceiptPriceToIncludeInReports())
							continue;
						table.addCell(receipt.getName());
						table.addCell(receipt.getCurrencyFormattedPrice());
						table.addCell(DateFormat.getDateFormat(mContext).format(receipt.getDate()));
						table.addCell(receipt.getCategory());
						table.addCell(receipt.getComment());
						table.addCell(((receipt.isExpensable())?"":"Not ") + "Expensable");
						table.addCell(((receipt.hasImage())?"":"Not ") + "Pictured");
						if (preferences.includeTaxField()) table.addCell(receipt.getCurrencyFormattedTax());
					}
					document.add(table);
					document.newPage();
					this.addImageRows(document, receipts);
					document.close();
					pdf.close();
					mFiles[EmailOptions.PDF_FULL.getIndex()] = mStorageManager.getFile(dir, dir.getName() + ".pdf");
				} catch (Exception e) {
					if(BuildConfig.DEBUG) Log.e(TAG, e.toString());
				}
			}
			if (mOptions.contains(EmailOptions.PDF_IMAGES_ONLY)) {
				Log.e(TAG, "PDF Called");
				try {
					File dir = trip.getDirectory();
					if (!dir.exists()) {
						dir = mStorageManager.getFile(trip.getName());
						if (!dir.exists())
							dir = mStorageManager.mkdir(trip.getName());
					}
					final FileOutputStream pdf = mStorageManager.getFOS(dir, dir.getName() + IMAGES_PDF);
		            Document document = new Document();
					PdfWriter writer = PdfWriter.getInstance(document, pdf);
					writer.setPageEvent(new Footer());
					document.open();
					this.addImageRows(document, receipts);
					document.close();
					pdf.close();
					mFiles[EmailOptions.PDF_IMAGES_ONLY.getIndex()] = mStorageManager.getFile(dir, dir.getName() + IMAGES_PDF);
				} catch (Exception e) {
					if(BuildConfig.DEBUG) Log.e(TAG, e.toString(), e);
					//RuntimeException => Document has no pages (likely selected an image pdf with no images)
				}
			}
			if (mOptions.contains(EmailOptions.CSV)) {
				File dir = trip.getDirectory();
				if (!dir.exists()) {
					dir = mStorageManager.getFile(trip.getName());
					if (!dir.exists())
						dir = mStorageManager.mkdir(trip.getName());
				}
				String data = "";
				CSVColumns columns = mDB.getCSVColumns();
				if (preferences.includeCSVHeaders())
					data += columns.printHeaders();
				for (int i=0; i < len; i++) {
					if (preferences.onlyIncludeExpensableReceiptsInReports() && !receipts[i].isExpensable()) 
						continue;
					String price = (receipts[i].getPrice().length() == 0) ? "0" : receipts[i].getPrice();
					if (Float.parseFloat(price) < preferences.getMinimumReceiptPriceToIncludeInReports())
						continue;
					data += columns.print(receipts[i], trip);
				}
				String filename = dir.getName() + ".csv";
				if (!mStorageManager.write(dir, filename, data)) {
					if (BuildConfig.DEBUG) Log.e(TAG, "Failed to write the csv file");
				}
				mFiles[EmailOptions.CSV.getIndex()] = mStorageManager.getFile(dir, filename);
			}
			if (mOptions.contains(EmailOptions.ZIP_IMAGES_STAMPED)) { //Get rid of Debug line here
				File dir = mStorageManager.mkdir(trip.getDirectory(), trip.getName());
				for (int i=0; i < len; i++) {
					if (preferences.onlyIncludeExpensableReceiptsInReports() && !receipts[i].isExpensable()) 
						continue;
					if (receipts[i].hasImage())
						continue;
					try {
						Bitmap b = stampImage(trip, receipts[i], Bitmap.Config.ARGB_8888);
						if (b != null) {
							mStorageManager.writeBitmap(dir, b, (i+1) + "_" + receipts[i].getName() + ".jpg", CompressFormat.JPEG, 85);
							b.recycle();
						}
					}
					catch (OutOfMemoryError e) {
						System.gc();
						try {
							Bitmap b = stampImage(trip, receipts[i], Bitmap.Config.RGB_565);
							if (b != null) {
								mStorageManager.writeBitmap(dir, b, (i+1) + "_" + receipts[i].getName() + ".jpg", CompressFormat.JPEG, 85);
								b.recycle();
							}
						}
						catch (OutOfMemoryError e2) {
							memoryErrorOccured = true;
							break;
						}
					}
				}
				File zip = mStorageManager.zipBuffered(dir, 2048);
				mStorageManager.deleteRecursively(dir);
				mFiles[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()] = zip;
			}
			return Long.valueOf(100L);
		}
		
		private static final float IMG_SCALE_FACTOR = 2.1f;
		private static final float HW_RATIO = 0.75f;
	    private Bitmap stampImage(final TripRow trip, final ReceiptRow receipt, Bitmap.Config config) {
	    	if (!receipt.hasImage()) {
	    		return null;
	    	}
	        Bitmap foreground = mStorageManager.getMutableMemoryEfficientBitmap(receipt.getImage());
	        if (foreground != null) { // It can be null if file not found
		        int foreWidth = foreground.getWidth(), foreHeight = foreground.getHeight();
		        if (foreHeight > foreWidth) { foreWidth = (int) (foreHeight*HW_RATIO); }
		        else { foreHeight = (int) (foreWidth/HW_RATIO); }
		        int xPad = (int) (foreWidth/IMG_SCALE_FACTOR), yPad = (int) (foreHeight/IMG_SCALE_FACTOR);
		        Bitmap background = Bitmap.createBitmap(foreWidth + xPad, foreHeight + yPad, config);
		        Canvas canvas = new Canvas(background);
		        canvas.drawARGB(0xFF,0xFF,0xFF,0xFF); //This represents White color
		        Paint dither = new Paint(); 
		        dither.setDither(true); dither.setFilterBitmap(false);
		        canvas.drawBitmap(foreground, (background.getWidth() - foreground.getWidth())/2, (background.getHeight() - foreground.getHeight())/2, dither);
		        Paint brush = new Paint();
		        brush.setAntiAlias(true);
		        brush.setTypeface(Typeface.SANS_SERIF);
		        brush.setColor(Color.BLACK);
		        brush.setStyle(Paint.Style.FILL);
		        brush.setTextAlign(Align.LEFT);
		        int num = 5; 
		        if (receipt.hasExtraEditText1()) num++;
		        if (receipt.hasExtraEditText2()) num++;
		        if (receipt.hasExtraEditText3()) num++;
		        float spacing = getOptimalSpacing(num, yPad/2, brush);
		        float y = spacing*4;
		        canvas.drawText(trip.getName(), xPad/2, y, brush); y += spacing;
		        canvas.drawText(trip.getFormattedStartDate(mContext, mPersistenceManager.getPreferences().getDateSeparator()) + " -- " + trip.getFormattedEndDate(mContext, mPersistenceManager.getPreferences().getDateSeparator()), xPad/2, y, brush); y += spacing;
		        y = background.getHeight() - yPad/2 + spacing*2;
		        canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_NAME) + ": " + receipt.getName(), xPad/2, y, brush); y += spacing;
		        canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_PRICE) + ": " + receipt.getPrice() + " " + receipt.getCurrencyCode(), xPad/2, y, brush); y += spacing;
		        canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_DATE) + ": " + receipt.getFormattedDate(mContext, mPersistenceManager.getPreferences().getDateSeparator()), xPad/2, y, brush); y += spacing;
		        canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_CATEGORY) + ": " + receipt.getCategory(), xPad/2, y, brush); y += spacing;
		        canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_COMMENT) + ": " + receipt.getComment(), xPad/2, y, brush); y += spacing;
		        if (receipt.hasExtraEditText1()) { canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_1) + ": " + receipt.getExtraEditText1(), xPad/2, y, brush); y += spacing; }
		        if (receipt.hasExtraEditText2()) { canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_2) + ": " + receipt.getExtraEditText2(), xPad/2, y, brush); y += spacing; }
		        if (receipt.hasExtraEditText3()) { canvas.drawText(mFlex.getString(mContext, R.string.RECEIPTMENU_FIELD_EXTRA_EDITTEXT_3) + ": " + receipt.getExtraEditText3(), xPad/2, y, brush); y += spacing; }
		        return background;
	        }
	        else {
	        	return null;
	        }
	    }
	    
	    private float getOptimalSpacing(int count, int space, Paint brush) {
	    	float fontSize = 8f; //Seed
	    	brush.setTextSize(fontSize);
	    	while (space > (count + 2)*brush.getFontSpacing()) { brush.setTextSize(++fontSize);}
	    	brush.setTextSize(--fontSize);
	    	return brush.getFontSpacing();
	    }
		
		private Document addImageRows(Document document, ReceiptRow[] receipts) {
			final Preferences preferences = mPersistenceManager.getPreferences();
			PdfPTable table = new PdfPTable(3);
			table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
			table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
			table.getDefaultCell().disableBorderSide(PdfPCell.TOP);
			table.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			table.setWidthPercentage(100);
			float big = PageSize.A4.getWidth()/2.1f;
			float small = PageSize.A4.getWidth() - 2*big;
			try {
				table.setWidths(new float[] {big, small, big});
			} catch (DocumentException e1) {
				if (BuildConfig.DEBUG) Log.e(TAG, e1.toString());
			}
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
			table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
			table.setSplitLate(false);
			int size = receipts.length;
			ReceiptRow receipt;
			Image img1 = null, img2 = null;
			ReceiptRow receipt1 = null;
			int num1 = 0, flag = 0;
			final ArrayList<FullPageReceiptHolder> fullpageReceipts = new ArrayList<EmailAssistant.FullPageReceiptHolder>();
			for (int i=0; i < size; i++) {
				receipt = receipts[i];
				if (preferences.onlyIncludeExpensableReceiptsInReports() && !receipt.isExpensable()) 
					continue;
				if (!receipt.hasImage())
					continue;
				if (receipt.isFullPage()) {
					fullpageReceipts.add(new FullPageReceiptHolder(receipt, i+1));
					continue;
				}
				if (receipt.getPriceAsFloat() < preferences.getMinimumReceiptPriceToIncludeInReports())
					continue;
				if (receipt.hasImage() && img1 == null) {
					try {
						img1 = Image.getInstance(receipt.getImage().getCanonicalPath());
					} catch (Exception e) {
						if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
						continue;
					}
					receipt1 = receipt;
					num1 = i;
				}
				else if (receipt.hasImage() && img2 == null) {
					try {
						img2 = Image.getInstance(receipt.getImage().getCanonicalPath());
					} catch (Exception e) {
						if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
						continue;
					}
					table.addCell((num1+1) + "  \u2022  " + receipt1.getName() + "  \u2022  " + receipt1.getFormattedDate(mContext, preferences.getDateSeparator()));
					table.addCell("");
					table.addCell((i+1) + "  \u2022  " + receipt.getName() + "  \u2022  " + receipt.getFormattedDate(mContext, preferences.getDateSeparator()));
					table.addCell(getCell(img1));
					table.addCell("");
					table.addCell(getCell(img2));
					table.setSpacingAfter(40);
					img1 = null; img2 = null;
					if (++flag==2) {//ugly hack to fix how page breaks are separated
						table.completeRow();
						try {
							document.add(table);
						} catch (DocumentException e) {
							if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
						}
						table = new PdfPTable(3);
						table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
						table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
						table.getDefaultCell().disableBorderSide(PdfPCell.TOP);
						table.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
						table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
						table.setWidthPercentage(100);
						try {
							table.setWidths(new float[] {big, small, big});
						} catch (DocumentException e1) {
							if (BuildConfig.DEBUG) Log.e(TAG, e1.toString());
						}
						table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
						table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
						table.setSplitLate(false);
						flag = 0;
					}
				}
			}
			if (img1 != null) {
				table.addCell((num1+1) + "  \u2022  " + receipt1.getName() + "  \u2022  " + receipt1.getFormattedDate(mContext, preferences.getDateSeparator()));
				table.addCell(" ");
				table.addCell(" ");
				table.addCell(getCell(img1));
			}
			table.completeRow();
			try {
				document.add(table);
			} catch (DocumentException e) {
				if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
			}
			document.newPage();
			//Full Page Stuff Below
			for (FullPageReceiptHolder holder : fullpageReceipts) { //TODO: Redo to get rid of iterator
				if (preferences.onlyIncludeExpensableReceiptsInReports() && !holder.receipt.isExpensable()) 
					continue;
				if (holder.receipt.getPriceAsFloat() < preferences.getMinimumReceiptPriceToIncludeInReports())
					continue;
				try {
					img1 = Image.getInstance(holder.receipt.getImage().getCanonicalPath());
					table = new PdfPTable(1);
					table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
					table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
					table.getDefaultCell().disableBorderSide(PdfPCell.TOP);
					table.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
					table.setWidthPercentage(100);
					table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
					table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
					table.setSplitLate(false);
					table.addCell(holder.number + "  \u2022  " + holder.receipt.getName() + "  \u2022  " + holder.receipt.getFormattedDate(mContext, preferences.getDateSeparator()));
					table.addCell(getFullCell(img1));
					table.completeRow();
					document.add(table);
					document.newPage();
				} catch (Exception e) {
					if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
					continue;
				}
			}
			return document;
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
			cell.setFixedHeight(PageSize.A4.getHeight()/2.4f);
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
			cell.setFixedHeight(PageSize.A4.getHeight()/1.15f);
			return cell;
		}
		
		@Override
		protected void onPostExecute(Long result) {
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
			public void onEndPage(PdfWriter writer, Document document) {
				Rectangle rect = writer.getPageSize();
				ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, 
						new Phrase(FOOTER, FontFactory.getFont("Times-Roman", 9, Font.ITALIC)), rect.getLeft()+36, rect.getBottom()+36, 0);
			}
		}

	}
	
}