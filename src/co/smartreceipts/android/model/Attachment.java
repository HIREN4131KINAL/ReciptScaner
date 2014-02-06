package co.smartreceipts.android.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;

import co.smartreceipts.android.BuildConfig;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class Attachment {
	
	private static final String TAG = "Attachment";
	
	private static final String PDF_EXTENSION = "pdf";
	private static final String SMR_EXTENSION = "smr";
	private static final String PNG_EXTENSION = "png";
	private static final String JPG_EXTENSION = "jpg";
	private static final String JPEG_EXTENSION = "jpeg";
	
	private final Uri mUri;
	private final String mAction;
	private final String mExtension;
	private boolean mIsValid;
	
	public Attachment(Intent intent, ContentResolver resolver) {
		if (intent != null && intent.getAction() != null) {
			mAction = intent.getAction();
			if (Intent.ACTION_VIEW.equals(mAction) && intent.getData() != null) {
				mUri = intent.getData();
				mExtension = getExtension(resolver, mUri);
				mIsValid = true;
			}
			else if (Intent.ACTION_SEND.equals(mAction) && intent.getExtras() != null) {
				mUri = resolveUri((Uri) intent.getExtras().get(Intent.EXTRA_STREAM), resolver, MediaStore.Images.ImageColumns.DATA);
				mExtension = (mUri != null) ? mUri.toString().substring(mUri.toString().lastIndexOf(".") + 1) : new String();
				mIsValid = true;
			}
			else {
				mUri = null; mExtension = null;
				mIsValid = false;
			}
		}
		else {
			mUri = null; mAction = null; mExtension = null;
			mIsValid = false;
		}
		if (BuildConfig.DEBUG) Log.d(TAG, "Action: " + mAction);
		if (BuildConfig.DEBUG) Log.d(TAG, "Extension: " + mExtension);
		if (BuildConfig.DEBUG) Log.d(TAG, "Uri: " + mUri);
	}
	
	private String getExtension(ContentResolver resolver, Uri uri) {
		String extension = mUri.toString().substring(mUri.toString().lastIndexOf(".") + 1);
		extension = (!TextUtils.isEmpty(extension) && extension.length() < 5) ? extension : MimeTypeMap.getSingleton().getExtensionFromMimeType(resolver.getType(uri));
		if (!TextUtils.isEmpty(extension)) {
			return extension.toLowerCase(Locale.US);
		}
		else {
			return SMR_EXTENSION; //q&d hack
		}
	}
	
	private Uri resolveUri(Uri uri, ContentResolver resolver, String column) {
		Cursor cursor = null;
		try {
	        cursor = resolver.query(uri, null, null, null, null); 
	        if (cursor == null) { // local file path (i.e. Dropbox)
	        	return Uri.parse(uri.toString());
	        } 
	        else { 
	            cursor.moveToFirst();
	            int idx = cursor.getColumnIndex(column); 
	            return Uri.fromFile(new File(cursor.getString(idx)));
	        }
		}
		finally {
			if (cursor != null) cursor.close();
		}
	}
	
	public boolean isValid() {
		return mIsValid;
	}
	
	public Uri getUri() {
		return mUri;
	}
	
	public InputStream openUri(ContentResolver resolver) throws FileNotFoundException {
		return resolver.openInputStream(mUri);
	}
	
	public String getAction() {
		return mAction;
	}
	
	public boolean isActionView() {
		return Intent.ACTION_VIEW.equals(mAction);
	}
	
	public boolean isActionSend() {
		return Intent.ACTION_SEND.equals(mAction);
	}
	
	public String getExtension() {
		return mExtension;
	}
	
	public boolean isPDF() {
		return PDF_EXTENSION.equals(mExtension);
	}
	
	public boolean isImage() {
		return JPG_EXTENSION.equals(mExtension) || JPEG_EXTENSION.equals(mExtension) || PNG_EXTENSION.equals(mExtension);
	}
	
	public boolean isSMR() {
		return SMR_EXTENSION.equals(mExtension);
	}
	
	/**
	 * Checks if this is a file that can be attached to a receipt (i.e. PDF or Image)
	 * @return true if so. false otherwise
	 */
	public boolean isDirectlyAttachable() {
		return (isActionSend() && isImage()) || (isActionView() && isPDF());
	}

}