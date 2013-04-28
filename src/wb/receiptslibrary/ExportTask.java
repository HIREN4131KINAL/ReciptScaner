package wb.receiptslibrary;

import java.io.File;
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
			File db = holder.getActivity().getDatabasePath(DatabaseHelper.DATABASE_NAME);
			File prefs = internal.getFile(internal.getRoot().getParentFile(), "shared_prefs");
			
			//Database file
			if (db != null && db.exists()) {
				File sdDB = external.getFile("receipts.db"); 
				if (D) Log.d(TAG, "Copying the database file from: " + db.getAbsolutePath() + " to " + sdDB.getAbsolutePath());
				try {
					external.copy(db, sdDB, true);
				}
				catch (IOException e) {
					Log.e(TAG, e.toString());
					return null;
				}
			}
			else { 
				return null;
			}
			
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
			File zip = external.zipBuffered(8192);
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