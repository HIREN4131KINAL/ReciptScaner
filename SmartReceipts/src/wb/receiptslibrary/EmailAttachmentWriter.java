package wb.receiptslibrary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Set;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
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
import com.itextpdf.text.Element;

import wb.android.storage.StorageManager;
import wb.csv.CSVColumns;

public class EmailAttachmentWriter extends AsyncTask<TripRow, Integer, Long>{

	private static final String TAG = "EmailAttachmentWriter";
	
	private final SmartReceiptsActivity _activity;
	private final StorageManager _sdCard;
	private final DatabaseHelper _db;
	private final ProgressDialog _dialog;
	private final boolean _fullPDF, _imgPDF, _csv;
	private final File[] _files;

	private static final String IMAGES_PDF = "Images.pdf";
	private static final String FOOTER = "Report Generated using Smart Receipts for Android";
	
	static final int FULL_PDF = 0;
	static final int IMG_PDF = 1;
	static final int CSV = 2;
	
	public EmailAttachmentWriter(SmartReceiptsActivity activity, StorageManager sdCard, DatabaseHelper db, ProgressDialog dialog, boolean buildFullPDF, boolean buildImagesPDF, boolean buildCSV) {
		_activity = activity;
		_sdCard = sdCard;
		_db = db;
		_dialog = dialog;
		_fullPDF = buildFullPDF;
		_imgPDF = buildImagesPDF;
		_csv = buildCSV;
		_files = new File[] {null, null, null};
	}
	
	@Override
	protected Long doInBackground(TripRow... trips) {
		if (trips.length == 0)
			return new Long(100L); //Should never be reached
		final TripRow trip = trips[0];
		final ReceiptRow[] receipts = _db.getReceipts(trip, false);
		final int len = receipts.length;
		if (_fullPDF) {
			try {
				File dir = trip.dir;
				if (!dir.exists()) {
					dir = _sdCard.getFile(trip.dir.getName());
					if (!dir.exists())
						dir = _sdCard.mkdir(trip.dir.getName());
				}
				final FileOutputStream pdf = _sdCard.getFOS(dir, dir.getName() + ".pdf");
	            Document document = new Document();
				PdfWriter writer = PdfWriter.getInstance(document, pdf);
				writer.setPageEvent(new Footer());
				document.open();
				document.add(new Paragraph(SmartReceiptsActivity.CurrencyValue(trip.price, trip.currency) + "  \u2022  " + dir.getName() + "\n"
							+ "From: " + DateFormat.getDateFormat(_activity).format(trip.from) + " To: " + DateFormat.getDateFormat(_activity).format(trip.to) + "\n\n\n"));
				PdfPTable table = new PdfPTable(7);
				table.setWidthPercentage(100);
				table.addCell("Name");
				table.addCell("Price");
				table.addCell("Date");
				table.addCell("Category");
				table.addCell("Comment");
				table.addCell("Expensable");
				table.addCell("Pictured");
				ReceiptRow receipt;
				for (int i=0; i < len; i++) {
					receipt = receipts[i];
					table.addCell(receipt.name);
					table.addCell(SmartReceiptsActivity.CurrencyValue(receipt.price, receipt.currency));
					table.addCell(DateFormat.getDateFormat(_activity).format(receipt.date));
					table.addCell(receipt.category);
					table.addCell(receipt.comment);
					table.addCell(((receipt.expensable)?"":"Not ") + "Expensable");
					table.addCell(((receipt.img != null)?"":"Not ") + "Pictured");
				}
				document.add(table);
				document.newPage();
				this.addImageRows(document, receipts);
				document.close();
				pdf.close();
				_files[FULL_PDF] = _sdCard.getFile(dir, dir.getName() + ".pdf");
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.toString());
			} catch (DocumentException e) {
				Log.e(TAG, e.toString());
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
		if (_imgPDF) {
			try {
				File dir = trip.dir;
				if (!dir.exists()) {
					dir = _sdCard.getFile(trip.dir.getName());
					if (!dir.exists())
						dir = _sdCard.mkdir(trip.dir.getName());
				}
				final FileOutputStream pdf = _sdCard.getFOS(dir, dir.getName() + IMAGES_PDF);
	            Document document = new Document();
				PdfWriter writer = PdfWriter.getInstance(document, pdf);
				writer.setPageEvent(new Footer());
				document.open();
				this.addImageRows(document, receipts);
				document.close();
				pdf.close();
				_files[IMG_PDF] = _sdCard.getFile(dir, dir.getName() + IMAGES_PDF);
				
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.toString());
			} catch (DocumentException e) {
				Log.e(TAG, e.toString());
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
		if (_csv) {
			File dir = trip.dir;
			if (!dir.exists()) {
				dir = _sdCard.getFile(trip.dir.getName());
				if (!dir.exists())
					dir = _sdCard.mkdir(trip.dir.getName());
			}
			String data = "";
			CSVColumns columns = _db.getCSVColumns();
			for (int i=0; i < len; i++)
				data += columns.print(receipts[i]);
			String filename = dir.getName() + ".csv";
			if (!_sdCard.write(dir, filename, data))
				Log.e(TAG, "Failed to write the csv file");
			_files[CSV] = _sdCard.getFile(dir, filename);
		}
		return new Long(100L);
	}
	
	private Document addImageRows(Document document, ReceiptRow[] receipts) {
			PdfPTable table = new PdfPTable(2);
			table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
			table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
			table.getDefaultCell().disableBorderSide(PdfPCell.TOP);
			table.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			table.setWidthPercentage(100);
			table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
			table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
			table.setSplitLate(false);
			int size = receipts.length;
			ReceiptRow receipt;
			Image img1 = null, img2 = null;
			ReceiptRow receipt1 = null;
			int num1 = 0, flag = 0;
			final HashMap<ReceiptRow, Integer> fullpageReceipts = new HashMap<ReceiptRow, Integer>();
			for (int i=0; i < size; i++) {
				receipt = receipts[i];
				if (receipt.img == null)
					continue;
				if (receipt.fullpage) {
					fullpageReceipts.put(receipt, i+1);
					continue;
				}
				if (receipt.img != null && img1 == null) {
					try {
						img1 = Image.getInstance(receipt.img.getCanonicalPath());
					} catch (BadElementException e) {
						Log.e(TAG, e.toString());
						continue;
					} catch (MalformedURLException e) {
						Log.e(TAG, e.toString());
						continue;
					} catch (IOException e) {
						Log.e(TAG, e.toString());
						continue;
					}
					receipt1 = receipt;
					num1 = i;
				}
				else if (receipt.img != null && img2 == null) {
					try {
						img2 = Image.getInstance(receipt.img.getCanonicalPath());
					} catch (BadElementException e) {
						Log.e(TAG, e.toString());
						continue;
					} catch (MalformedURLException e) {
						Log.e(TAG, e.toString());
						continue;
					} catch (IOException e) {
						Log.e(TAG, e.toString());
						continue;
					}
					table.addCell((num1+1) + "  \u2022  " + receipt1.name + "  \u2022  " + DateFormat.getDateFormat(_activity).format(receipt1.date));
					table.addCell((i+1) + "  \u2022  " + receipt.name + "  \u2022  " + DateFormat.getDateFormat(_activity).format(receipt.date));
					table.addCell(getCell(img1));
					table.addCell(getCell(img2));
					table.setSpacingAfter(40);
					img1 = null; img2 = null;
					if (++flag==2) {//ugly hack to fix how page breaks are separated
						table.completeRow();
						try {
							document.add(table);
						} catch (DocumentException e) {
							Log.e(TAG, e.toString());
						}
						table = new PdfPTable(2);
						table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
						table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
						table.getDefaultCell().disableBorderSide(PdfPCell.TOP);
						table.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
						table.setWidthPercentage(100);
						table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
						table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
						table.setSplitLate(false);
						flag = 0;
					}
				}
			}
			if (img1 != null) {
				table.addCell((num1+1) + "  \u2022  " + receipt1.name + "  \u2022  " + DateFormat.getDateFormat(_activity).format(receipt1.date));
				table.addCell(" ");
				table.addCell(getCell(img1));
			}
			table.completeRow();
			try {
				document.add(table);
			} catch (DocumentException e) {
				Log.e(TAG, e.toString());
			}
			document.newPage();
			//Full Page Stuff Below
			Set<ReceiptRow> set = fullpageReceipts.keySet();
			size = fullpageReceipts.size();
			for (ReceiptRow rcpt:set) { //Redo to get rid of iterator
				try {
					img1 = Image.getInstance(rcpt.img.getCanonicalPath());
					table = new PdfPTable(1);
					table.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
					table.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
					table.getDefaultCell().disableBorderSide(PdfPCell.TOP);
					table.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
					table.setWidthPercentage(100);
					table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
					table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
					table.setSplitLate(false);
					table.addCell(fullpageReceipts.get(rcpt).intValue() + "  \u2022  " + rcpt.name + "  \u2022  " + DateFormat.getDateFormat(_activity).format(rcpt.date));
					table.addCell(getFullCell(img1));
					table.completeRow();
					document.add(table);
					document.newPage();
				} catch (BadElementException e) {
					Log.e(TAG, e.toString());
					continue;
				} catch (MalformedURLException e) {
					Log.e(TAG, e.toString());
					continue;
				} catch (IOException e) {
					Log.e(TAG, e.toString());
					continue;
				}
				catch (DocumentException e) {
					Log.e(TAG, e.toString());
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
		_activity.postCreateAttachments(_files);
		_dialog.cancel();
	}
	
	private class Footer extends PdfPageEventHelper {
		public void onEndPage(PdfWriter writer, Document document) {
			Rectangle rect = writer.getPageSize();
			ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_LEFT, 
					new Phrase(FOOTER, FontFactory.getFont("Times-Roman", 9, Font.ITALIC)), rect.getLeft()+36, rect.getBottom()+36, 0);
		}
	}

}
