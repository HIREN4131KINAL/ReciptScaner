package wb.receiptslibrary;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import com.actionbarsherlock.internal.widget.ActionBarView.HomeView;

import android.net.Uri;
import android.util.Log;
import wb.android.async.ProgressTask;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;

public class ExportTask extends ProgressTask<Void, Uri> {

	private static final String TAG = "ExportTask";
	private static final boolean D = true;
	
	private static final String EXPORT_FILENAME = "SmartReceipts.smr";
	static final String DATABASE_EXPORT_NAME = "receipts_backup.db";
	private static final String DATABASE_JOURNAL = "receipts.db-journal";
	
	private final HomeHolder holder;
	
	public ExportTask(HomeHolder holder, String progressMessage) {
		super(holder.getActivity(), progressMessage, true);
		this.holder = holder;
	}

	@Override
	protected Uri doInBackground(Void... voids) {
		try {
			StorageManager external = StorageManager.getExternalInstance(holder.getActivity());
			StorageManager internal = StorageManager.getInternalInstance(holder.getActivity());
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
				if (D) Log.d(TAG, "Copying the prefs file from: " + prefs.getAbsolutePath() + " to " + sdPrefs.getAbsolutePath());
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
				if (D) Log.d(TAG, "Copying " + internalFiles.length + " files/directories to the SD Card.");
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
		holder.onExportComplete(uri);
	}

}