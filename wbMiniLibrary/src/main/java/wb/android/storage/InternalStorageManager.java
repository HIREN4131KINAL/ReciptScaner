package wb.android.storage;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class InternalStorageManager extends StorageManager {
	
	private static final String TAG = "InternalStorageManager";
	private static final boolean D = false;
	
	private final Context context;

	protected InternalStorageManager(Context context) {
		super(context.getFilesDir());
		if (D) Log.d(TAG, "Creating Internal SD Card"); 
		this.context = context;
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
		return context.openFileOutput(filename, mode);
	}

}