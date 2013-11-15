package wb.receiptslibrary.workers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import com.actionbarsherlock.internal.widget.ActionBarView.HomeView;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import wb.android.async.ProgressTask;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;
import wb.receiptslibrary.BuildConfig;
import wb.receiptslibrary.SmartReceiptsActivity;
import wb.receiptslibrary.persistence.DatabaseHelper;

public class ExportTask extends ProgressTask<Void, Uri> {

	private static final String TAG = "ExportTask";
	private static final boolean D = true;
	
	private static final String EXPORT_FILENAME = "SmartReceipts.smr";
	static final String DATABASE_EXPORT_NAME = "receipts_backup.db";
	private static final String DATABASE_JOURNAL = "receipts.db-journal";
	
	private SmartReceiptsActivity mActivity;
	private Listener mListener;
	
	public interface Listener {
		public void onExportComplete(SmartReceiptsActivity activity, Uri uri);
	}
	
	public ExportTask(SmartReceiptsActivity activity, Listener listener, String progressMessage) {
		super(activity, progressMessage, true);
		mActivity = activity;
		mListener = listener;
	}

	@Override
	protected Uri doInBackground(Void... voids) {
		try {
			StorageManager external = StorageManager.getExternalInstance(mActivity);
			StorageManager internal = StorageManager.getInternalInstance(mActivity);
			external.delete(external.getFile(EXPORT_FILENAME)); //Remove old export
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
				if (BuildConfig.DEBUG) Log.d(TAG, "Copying the prefs file from: " + prefs.getAbsolutePath() + " to " + sdPrefs.getAbsolutePath());
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
				if (BuildConfig.DEBUG) Log.d(TAG, "Copying " + internalFiles.length + " files/directories to the SD Card.");
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
					return !file.getName().equalsIgnoreCase(DatabaseHelper.DATABASE_NAME) && !file.getName().equalsIgnoreCase(DATABASE_JOURNAL);
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
		mListener.onExportComplete(mActivity, uri);
	}

}