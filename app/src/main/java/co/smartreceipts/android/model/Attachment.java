package co.smartreceipts.android.model;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;

import co.smartreceipts.android.utils.UriUtils;
import co.smartreceipts.android.utils.log.Logger;

public class Attachment {

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
                Logger.debug(this, "Managing send action for {}.", intent);
				final Uri extraStreamUri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
				final Uri resolvedUri = resolveUri(extraStreamUri, resolver, MediaStore.Images.ImageColumns.DATA);
                Logger.debug(this, "Found the following Uris: extra => {}, stream => {}.", extraStreamUri, resolvedUri);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || resolvedUri == null) {
					// For M, just use the "content://" one instead of the "file://"
					mUri = extraStreamUri;
                    mExtension = extraStreamUri != null ? UriUtils.getExtension(extraStreamUri, resolver) : "";
				} else {
                    mUri = resolvedUri;
                    mExtension = resolvedUri.toString().substring(resolvedUri.toString().lastIndexOf(".") + 1);
				}
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
		if (mUri == null) {
			mIsValid = false;
		}
		Logger.debug(this, "Action: " + mAction);
		Logger.debug(this, "Extension: " + mExtension);
		Logger.debug(this, "Uri: " + mUri);
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
        if (uri == null) {
            return null;
        }

		Cursor cursor = null;
		try {
	        cursor = resolver.query(uri, null, null, null, null); 
	        if (cursor == null) { // local file path (i.e. Dropbox)
	        	return Uri.parse(uri.toString());
	        } 
	        else { 
	            if (cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(column);
                    if (idx >= 0 && cursor.getColumnCount() > 0) {
                        final String path = cursor.getString(idx);
                        if (path != null) {
                            return Uri.fromFile(new File(cursor.getString(idx)));
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
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
     * @return {@code true} if we require storage permissions before continuing (ie we're on Android M+ and our Uri doesn't start with content://)
     */
    public boolean requiresStoragePermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ((mUri != null && !mUri.toString().startsWith(ContentResolver.SCHEME_CONTENT)) || isActionView());
    }
	
	/**
	 * Checks if this is a file that can be attached to a receipt (i.e. PDF or Image)
	 * @return true if so. false otherwise
	 */
	public boolean isDirectlyAttachable() {
		return isActionSend() && (isImage() || isPDF());
	}

}