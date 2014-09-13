package co.smartreceipts.android.workers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import wb.android.async.BooleanProgressTask;
import wb.android.async.BooleanTaskCompleteDelegate;
import wb.android.storage.SDCardFileManager;
import wb.android.storage.StorageManager;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;

public class ImportTask extends BooleanProgressTask<Uri> {

	private static final String TAG = "ImportTask";

	private PersistenceManager mPersistenceManager;
	private Context mContext;
	private final boolean mOverwrite;

	public ImportTask(Context context, BooleanTaskCompleteDelegate delegate, String progressMessage, int taskID, boolean overwrite, PersistenceManager persistenceManager) {
		super(context, delegate, progressMessage, taskID);
		mContext = context;
		mPersistenceManager = persistenceManager;
		mOverwrite = overwrite;
	}

	@Override
	protected Boolean doInBackground(Uri... uris) {
		if (uris == null || uris.length == 0)
			return false;
		Uri uri = uris[0];
		if (uri != null) {
			try {
				SDCardFileManager external = mPersistenceManager.getExternalStorageManager();
				String scheme = uri.getScheme();
				if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
					InputStream is = null;
					try {
						ContentResolver cr = mContext.getContentResolver();
						is = cr.openInputStream(uri);
						File dest = external.getFile("smart.zip");
						external.delete(dest);
						if (!external.copy(is, dest, true)) {
							return false;
						}
						final boolean importResult = importAll(external, dest, mOverwrite);
						external.delete(dest);
						return importResult;
					}
					catch (Exception e) {
						Log.e(TAG, e.toString());
						return false;
					}
					finally {
						try {
							if (is != null)
								is.close();
						}
						catch (IOException e) {
							Log.e(TAG, e.toString());
						}
					}
				}
				else {
					File src = null;
					File dest = external.getFile("smart.zip");
					external.delete(dest);
					if (uri.getPath() != null)
						src = new File(uri.getPath());
					else if (uri.getEncodedPath() != null)
						src = new File(uri.getEncodedPath());
					if (src == null || !src.exists()) {
						return false;
					}
					if (!external.copy(src, dest, true)) {
						return false;
					}
					final boolean importResult = importAll(external, dest, mOverwrite);
					external.delete(dest);
					return importResult;
				}
			}
			catch (Exception e) {
				Log.e(TAG, e.toString());
				return false;
			}
		}
		else {
			return false;
		}
	}

	private boolean importAll(SDCardFileManager external, File file, final boolean overwrite) {
		if (!external.unzip(file, overwrite)) {
			return false;
		}
		StorageManager internal = mPersistenceManager.getInternalStorageManager();
		File sdPrefs = external.getFile("shared_prefs");
		File prefs = internal.getFile(internal.getRoot().getParentFile(), "shared_prefs");
		try {
			if (!internal.copy(sdPrefs, prefs, overwrite)) {
				Log.e(TAG, "Failed to import settings");
			}
		}
		catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		try {
			File internalDir = external.getFile("Internal");
			internal.copy(internalDir, internal.getRoot(), overwrite);
		}
		catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		DatabaseHelper db = mPersistenceManager.getDatabase();
		return db.merge(external.getFile(ExportTask.DATABASE_EXPORT_NAME).getAbsolutePath(), mContext.getPackageName(), overwrite);
	}

}
