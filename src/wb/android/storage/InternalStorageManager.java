package wb.android.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.util.Log;

public class InternalStorageManager extends StorageManager {
	
	private static final String TAG = "InternalStorageManager";
	private static final boolean D = false;
	
	private final Activity _activity;
	
	protected InternalStorageManager(final Activity activity) {
		super(activity.getFilesDir());
		if (D) Log.d(TAG, "Creating Internal SD Card"); 
		_activity = activity;
	}
	
	public FileOutputStream getFOS(String filename, int mode) throws FileNotFoundException {
		return getFOSHelper(_root, filename, mode);
	}
	
	public FileOutputStream getFOS(File dir, String filename, int mode) throws FileNotFoundException {
		return getFOSHelper(dir, filename, mode);
	}
	
	private final FileOutputStream getFOSHelper(File dir, String filename, int mode) throws FileNotFoundException {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		return _activity.openFileOutput(filename, mode);
	}
	
	Activity getActivity() {
		return _activity;
	}
	
}