package wb.android.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

public final class SDCardFileManager extends StorageManager {
	
	//logging variables
    private static final boolean D = false;
    private static final String TAG = "SDCardFileManager";
	
    //instance vars
	private String[] _allowedStates;
	
	public SDCardFileManager(final Activity activity) throws SDCardStateException {
		super();
		final String state = Environment.getExternalStorageState();
		_allowedStates = null;
		if (D) Log.d(TAG, "External Storage State: " + state);
		_root = activity.getExternalFilesDir(null); 
		if (_root == null)
		    throw new SDCardStateException(state);
	}
	
	public SDCardFileManager(final Activity activity, final String[] allowedStates) throws SDCardStateException {
		final String state = Environment.getExternalStorageState();
		if (D) Log.e(TAG, state);
		_allowedStates = allowedStates;
		final int size = allowedStates.length;
		for (int i = 0; i < size; i++) {
			if (!allowedStates[i].equals(state))
				throw new SDCardStateException(state);
		}
		_root = activity.getExternalFilesDir(null);
	}
	
	@Override	
	public final boolean isCurrentStateValid() {
		final String state = Environment.getExternalStorageState();
		if (_allowedStates == null)
			return (Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
		else {
			final int size = _allowedStates.length;
			for (int i = 0; i < size; i++) {
				if (!_allowedStates[i].equals(state))
					return false;
			}
			return true;
		}
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
		return new FileOutputStream(path + filename);
	}

	@Override
	public boolean isExternal() {
		return true;
	}
	
}