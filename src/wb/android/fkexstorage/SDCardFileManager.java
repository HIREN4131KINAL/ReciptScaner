package wb.android.fkexstorage;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

public final class SDCardFileManager extends StorageManager {
	
	//logging variables
    private static final boolean D = false;
    private static final String TAG = "SDCardFileManager";
	
    //instance vars
	private String[] _allowedStates;
	private Activity _activity;
	
	protected SDCardFileManager(final Activity activity) throws SDCardStateException {
		super(activity.getExternalFilesDir(null));
		if (D) Log.d(TAG, "Creating External SD Card"); 
		final String state = Environment.getExternalStorageState();
		if (D) Log.d(TAG, "External Storage State: " + state);
		_allowedStates = null;
		_activity = activity;
		if (_root == null)
		    throw new SDCardStateException(state);
	}
	
	protected SDCardFileManager(final Activity activity, final String[] allowedStates) throws SDCardStateException {
		super(activity.getExternalFilesDir(null));
		if (D) Log.d(TAG, "Creating External SD Card");
		final String state = Environment.getExternalStorageState();
		if (D) Log.d(TAG, "External Storage State: " + state);
		_allowedStates = allowedStates;
		_activity = activity;
		final int size = allowedStates.length;
		for (int i = 0; i < size; i++) {
			if (!allowedStates[i].equals(state))
				throw new SDCardStateException(state);
		}
	}
		
	public boolean isCurrentStateValid() {
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
	
	Activity getActivity() {
		return _activity;
	}
	
}