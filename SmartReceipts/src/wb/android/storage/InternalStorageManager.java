package wb.android.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;

public class InternalStorageManager extends StorageManager {
	
	//logging variables
    // private static final boolean D = false;
    // private static final String TAG = "InternalStorageManager";
	private final Activity _activity;
	
	public InternalStorageManager(final Activity activity) {
		super();
		_root = activity.getFilesDir();
		_activity = activity;
	}
	
	@Override
	public final boolean isCurrentStateValid() {
		return true;
	}
	
	@Override
	public final FileOutputStream getFOS(String filename, int mode) throws FileNotFoundException {
		return this.getFOS(_root, filename, mode);
	}
	
	@Override
	public final FileOutputStream getFOS(File dir, String filename, int mode) throws FileNotFoundException {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		return _activity.openFileOutput(filename, mode);
	}

	@Override
	public boolean isExternal() {
		return false;
	}
	
}