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
import co.smartreceipts.android.utils.Utils;

public class ImportTask extends BooleanProgressTask<Uri> {

	private static final String TAG = "ImportTask";
	public static final int TASK_ID = 1123;
	public static final String LOG_FILE = "import_log.txt";

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
		mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Starting log task at " + System.currentTimeMillis());
		if (uris == null || uris.length == 0) {
			mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Exiting task early as uris are empty");
			return false;
		}
		Uri uri = uris[0];
		if (uri != null) {
			mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Uri: " + uri);
			try {
				SDCardFileManager external = mPersistenceManager.getExternalStorageManager();
				String scheme = uri.getScheme();
				if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
					mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Processing URI with accepted scheme.");
					InputStream is = null;
					try {
						ContentResolver cr = mContext.getContentResolver();
						is = cr.openInputStream(uri);
						File dest = external.getFile("smart.zip");
						external.delete(dest);
						if (!external.copy(is, dest, true)) {
							mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Copy failed.");
							return false;
						}
						final boolean importResult = importAll(external, dest, mOverwrite);
						external.delete(dest);
						return importResult;
					}
					catch (Exception e) {
						Log.e(TAG, e.toString());
						mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Caught exception during import at [1]: " + Utils.getStackTrace(e));
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
					mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Processing URI with unknown scheme.");
					File src = null;
					File dest = external.getFile("smart.zip");
					external.delete(dest);
					if (uri.getPath() != null) {
						src = new File(uri.getPath());
					}
					else if (uri.getEncodedPath() != null) {
						src = new File(uri.getEncodedPath());
					}
					if (src == null || !src.exists()) {
						mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Unknown source.");
						return false;
					}
					if (!external.copy(src, dest, true)) {
						mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Copy failed.");
						return false;
					}
					final boolean importResult = importAll(external, dest, mOverwrite);
					external.delete(dest);
					return importResult;
				}
			}
			catch (Exception e) {
				Log.e(TAG, e.toString());
				mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Caught exception during import at [2]: " + Utils.getStackTrace(e));
				return false;
			}
		}
		else {
			mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Exiting task early as the desired URI is null.");
			return false;
		}
	}

	private boolean importAll(SDCardFileManager external, File file, final boolean overwrite) {
		mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "import all : " + file + " " + overwrite);
		if (!external.unzip(file, overwrite)) {
			mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Unzip failed");
			return false;
		}
		StorageManager internal = mPersistenceManager.getInternalStorageManager();
		File sdPrefs = external.getFile("shared_prefs");
		File prefs = internal.getFile(internal.getRoot().getParentFile(), "shared_prefs");
		try {
			if (!internal.copy(sdPrefs, prefs, overwrite)) {
				Log.e(TAG, "Failed to import settings");
				mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Failed to import settings");
			}
		}
		catch (IOException e) {
			Log.e(TAG, e.toString());
			mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Caught IOexception during import at [3]: " + Utils.getStackTrace(e));
		}
		try {
			File internalDir = external.getFile("Internal");
			internal.copy(internalDir, internal.getRoot(), overwrite);
		}
		catch (IOException e) {
			Log.e(TAG, e.toString());
			mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Caught IOexception during import at [4]: " + Utils.getStackTrace(e));
		}
		DatabaseHelper db = mPersistenceManager.getDatabase();
		mPersistenceManager.getStorageManager().appendTo(LOG_FILE, "Merging database");
		return db.merge(external.getFile(ExportTask.DATABASE_EXPORT_NAME).getAbsolutePath(), mContext.getPackageName(), overwrite);
	}

}
