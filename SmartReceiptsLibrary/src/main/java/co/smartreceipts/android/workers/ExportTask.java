package co.smartreceipts.android.workers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import wb.android.async.ProgressTask;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;

public class ExportTask extends ProgressTask<Void, Uri> {

	private static final String TAG = "ExportTask";

	private static final String EXPORT_FILENAME = DateUtils.getCurrentDateAsYYYY_MM_DDString() + "_SmartReceipts.smr";
	static final String DATABASE_EXPORT_NAME = "receipts_backup.db";
	private static final String DATABASE_JOURNAL = "receipts.db-journal";

	private final PersistenceManager mPersistenceManager;
	private final Listener mListener;

	public interface Listener {
		public void onExportComplete(Uri uri);
	}

	public ExportTask(Context context, String progressMessage, PersistenceManager persistenceManager, Listener listener) {
		super(context, progressMessage, true);
		mPersistenceManager = persistenceManager;
		mListener = listener;
	}

	@Override
	protected Uri doInBackground(Void... voids) {
		try {
			StorageManager external = mPersistenceManager.getExternalStorageManager();
			StorageManager internal = mPersistenceManager.getInternalStorageManager();
			external.delete(external.getFile(EXPORT_FILENAME)); //Remove old export
			File file = external.getFile(EXPORT_FILENAME);
			try {
				external.copy(external.getFile(DatabaseHelper.DATABASE_NAME), external.getFile(DATABASE_EXPORT_NAME), true);
			} catch (IOException e1) {
				Log.e(TAG, e1.toString());
				return null;
			}
			File prefs = internal.getFile(internal.getRoot().getParentFile(), "shared_prefs");

			//Preferences File
			if (prefs != null && prefs.exists()) {
				File sdPrefs = external.getFile("shared_prefs");
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "Copying the prefs file from: " + prefs.getAbsolutePath() + " to " + sdPrefs.getAbsolutePath());
				}
				try {
					external.copy(prefs, sdPrefs, true);
				}
				catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}

			//Internal Files
			File[] internalFiles = internal.listFilesAndDirectories();
			if (internalFiles.length > 0) {
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "Copying " + internalFiles.length + " files/directories to the SD Card.");
				}
				File internalOnSD = external.mkdir("Internal");
				try {
					internal.copy(internalOnSD, true);
				}
				catch (IOException e) {
					Log.e(TAG, e.toString());
					return null;
				}
			}

			//Finish
			File zip = external.zipBuffered(8192, new FileFilter() {
				@Override
				public boolean accept(File file) {
					return !file.getName().equalsIgnoreCase(DatabaseHelper.DATABASE_NAME) &&
						   !file.getName().equalsIgnoreCase(DATABASE_JOURNAL) &&
						   !file.getName().endsWith(".smr"); //Ignore previous backups
				}
			});
			// TODO: Catch null pointer here
			zip = external.rename(zip, EXPORT_FILENAME);
			return Uri.fromFile(zip);
		}
		catch (SDCardStateException e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}

	@Override
	protected void onTaskCompleted(Uri uri) {
		mListener.onExportComplete(uri);
	}

}