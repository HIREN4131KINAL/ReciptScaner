package wb.receiptslibrary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import wb.android.async.BooleanProgressTask;
import wb.android.async.BooleanTaskCompleteDelegate;
import wb.android.storage.SDCardFileManager;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;
import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

public class ImportTask extends BooleanProgressTask<Uri> {

	private static final String TAG = "ImportTask";
	private static final boolean D = true;
	
	private final boolean overwrite;
	private SmartReceiptsActivity activity;
	
	public ImportTask(SmartReceiptsActivity activity, BooleanTaskCompleteDelegate delegate, String progressMessage, int taskID, boolean overwrite) {
		super(activity, delegate, progressMessage, taskID);
		this.activity = activity;
		this.overwrite = overwrite;
	}

	@Override
	protected Boolean doInBackground(Uri... uris) {
		if (uris == null || uris.length == 0)
			return false;
		Uri uri = uris[0];
		if (uri != null) {
			try {
	    		SDCardFileManager external = StorageManager.getExternalInstance(activity);
	    		String scheme = uri.getScheme();
	    		if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
	    			InputStream is = null;
	    			try {
	    				ContentResolver cr = activity.getContentResolver();
	                    is = cr.openInputStream(uri);
	                	File dest = external.getFile("smart.zip");
	                	if (!external.copy(is, dest, true)) {
	                		is.close();
	                		return false;
	                	}
	                	is.close();
	                	return importAll(external, dest, overwrite);
	                } 
	                catch(Exception e) {
	                	Log.e(TAG, e.toString());
	            		return false;
	                }
	                finally {
	                	try {
	                    	if (is != null)
	                    		is.close();
	                	} catch (IOException e) { Log.e(TAG, e.toString()); }
	                }
	    		}
	    		else {
	    			File src = null;
	    			File dest = external.getFile("smart.zip");
	    			if (uri.getPath() != null)
	    				src = new File(uri.getPath());
	    			else if (uri.getEncodedPath() != null)
	    				src = new File(uri.getEncodedPath());
	    			if (src == null || !src.exists()) {
	            		return false;
	    			}
	    			if (!external.move(src, dest)) {
	            		return false;
	    			}
	    			return importAll(external, dest, overwrite);	
	    		}
			}
			catch (SDCardStateException e) {
				return false;
			}
    	}
    	else {
    		return false;
    	}
	}
	
	private boolean importAll(SDCardFileManager external, File file, final boolean overwrite) {
    	if (! external.unzip(file, overwrite))
    		return false;
    	StorageManager internal = StorageManager.getInternalInstance(activity);
    	File sdPrefs = external.getFile("shared_prefs");
    	File prefs = internal.getFile(internal.getRoot().getParentFile(), "shared_prefs");
    	try {
			if (!internal.copy(sdPrefs, prefs, overwrite))
				Log.e(TAG, "Failed to import settings");
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
    	try {
    		File internalDir = external.getFile("Internal");
    		internal.copy(internalDir, internal.getRoot(), overwrite);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
    	DatabaseHelper db = DatabaseHelper.getInstance(activity);
    	return db.merge(external.getFile(ExportTask.DATABASE_EXPORT_NAME).getAbsolutePath(), activity.getPackageName(), overwrite);
    }

}
