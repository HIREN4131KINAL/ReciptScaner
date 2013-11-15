package wb.receiptslibrary.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;

import wb.android.dialog.BetterDialogBuilder;
import wb.receiptslibrary.R;
import wb.receiptslibrary.SmartReceiptsActivity;
import wb.receiptslibrary.model.TripRow;
import wb.receiptslibrary.persistence.PersistenceManager;
import wb.receiptslibrary.workers.EmailAttachmentWriter.EmailOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

public class EmailAssistant implements EmailAttachmentWriter.Listener {

	private SmartReceiptsActivity mSmartReceiptsActivity;
	private TripRow mTrip;
	
	public EmailAssistant(SmartReceiptsActivity activity, TripRow trip) {
		mSmartReceiptsActivity = activity;
		mTrip = trip;
	}
	
	
	public final void emailTrip() {
    	final PersistenceManager persistenceManager = mSmartReceiptsActivity.getPersistenceManager();
		if (!persistenceManager.getStorageManager().isExternal()) {
    		Toast.makeText(mSmartReceiptsActivity, mSmartReceiptsActivity.getFlex().getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
    		return;
    	}
    	View scrollView = mSmartReceiptsActivity.getFlex().getView(R.layout.dialog_email);
    	final CheckBox pdfFull = (CheckBox) mSmartReceiptsActivity.getFlex().getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_PDF_FULL);
    	final CheckBox pdfImages = (CheckBox) mSmartReceiptsActivity.getFlex().getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_PDF_IMAGES);
    	final CheckBox csv = (CheckBox) mSmartReceiptsActivity.getFlex().getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_CSV);
    	final CheckBox zipStampedImages = (CheckBox) mSmartReceiptsActivity.getFlex().getSubView(scrollView, R.id.DIALOG_EMAIL_CHECKBOX_ZIP_IMAGES_STAMPED);
    	final BetterDialogBuilder builder = new BetterDialogBuilder(mSmartReceiptsActivity);
    	String msg = mSmartReceiptsActivity.getFlex().getString(R.string.DIALOG_EMAIL_MESSAGE);
    	if (msg.length() > 0) 
    		builder.setMessage(msg);
			builder.setTitle(mSmartReceiptsActivity.getFlex().getString(R.string.DIALOG_EMAIL_TITLE))
			   .setCancelable(true)
			   .setView(scrollView)
			   .setPositiveButton(mSmartReceiptsActivity.getFlex().getString(R.string.DIALOG_EMAIL_POSITIVE_BUTTON), new DialogInterface.OnClickListener() {
				   @Override
		           public void onClick(DialogInterface dialog, int id) {
					   if (!pdfFull.isChecked() && !pdfImages.isChecked() && !csv.isChecked() && !zipStampedImages.isChecked()) {
						   Toast.makeText(mSmartReceiptsActivity, mSmartReceiptsActivity.getFlex().getString(R.string.DIALOG_EMAIL_TOAST_NO_SELECTION), Toast.LENGTH_SHORT).show();
						   dialog.cancel();
						   return;
					   }
					   if (persistenceManager.getDatabase().getReceiptsSerial(mTrip).length == 0) {
						   Toast.makeText(mSmartReceiptsActivity, mSmartReceiptsActivity.getFlex().getString(R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS), Toast.LENGTH_SHORT).show();
						   dialog.cancel();
						   return;
					   }
		        	   ProgressDialog progress = ProgressDialog.show(mSmartReceiptsActivity, "", "Building Reports...", true, false);
		        	   EnumSet<EmailOptions> options = EnumSet.noneOf(EmailOptions.class);
		        	   if (pdfFull.isChecked()) options.add(EmailOptions.PDF_FULL);
		        	   if (pdfImages.isChecked()) options.add(EmailOptions.PDF_IMAGES_ONLY);
		        	   if (csv.isChecked()) options.add(EmailOptions.CSV);
		        	   if (zipStampedImages.isChecked()) options.add(EmailOptions.ZIP_IMAGES_STAMPED);
		        	   EmailAttachmentWriter attachmentWriter = new EmailAttachmentWriter(mSmartReceiptsActivity, persistenceManager.getStorageManager(), persistenceManager.getDatabase(), progress, options);
		        	   attachmentWriter.registerListener(EmailAssistant.this);
		        	   attachmentWriter.execute(mTrip);
		           }
		       })
		       .setNegativeButton(mSmartReceiptsActivity.getFlex().getString(R.string.DIALOG_EMAIL_NEGATIVE_BUTTON), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
		       .show();
	}


	@Override
	public void onAttachmentsCreated(File[] attachments) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);  
		emailIntent.setType("application/octet-stream");
		ArrayList<Uri> uris = new ArrayList<Uri>();
		if (attachments[EmailOptions.PDF_FULL.getIndex()] != null) uris.add(Uri.fromFile(attachments[EmailOptions.PDF_FULL.getIndex()]));
		if (attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()] != null) uris.add(Uri.fromFile(attachments[EmailOptions.PDF_IMAGES_ONLY.getIndex()]));
		if (attachments[EmailOptions.CSV.getIndex()] != null) uris.add(Uri.fromFile(attachments[EmailOptions.CSV.getIndex()]));
		if (attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()] != null) uris.add(Uri.fromFile(attachments[EmailOptions.ZIP_IMAGES_STAMPED.getIndex()]));
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{mSmartReceiptsActivity.getPersistenceManager().getPreferences().getDefaultEmailReceipient()});
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, mSmartReceiptsActivity.getFlex().getString(R.string.EMAIL_DATA_SUBJECT).replace("%REPORT_NAME%", mTrip.getName()).replace("%USER_ID%", mSmartReceiptsActivity.getPersistenceManager().getPreferences().getUserID()));
		/*Works for Google Drive. Breaks the rest
		ArrayList<String> extra_text = new ArrayList<String>(); //Need this part to fix a Bundle casting bug
		if (uris.size() == 1) extra_text.add(uris.size() + " report attached");
		if (uris.size() > 1) extra_text.add(uris.size() + " reports attached");
		emailIntent.putStringArrayListExtra(Intent.EXTRA_TEXT, extra_text);
		*/
		if (uris.size() == 1) emailIntent.putExtra(Intent.EXTRA_TEXT, uris.size() + " report attached");
		if (uris.size() > 1) emailIntent.putExtra(Intent.EXTRA_TEXT, uris.size() + " reports attached");
		emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
    	mSmartReceiptsActivity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}
	
}
