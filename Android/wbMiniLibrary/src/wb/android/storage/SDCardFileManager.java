package wb.android.storage;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public final class SDCardFileManager extends StorageManager {
	
	//logging variables
    private static final boolean D = false;
    private static final String TAG = "SDCardFileManager";
	
    //instance vars
	private String[] mAllowedStates;
	private Context mContext;
	
	protected SDCardFileManager(Context context) throws SDCardStateException {
		super(context.getExternalFilesDir(null));
		if (D) Log.d(TAG, "Creating External SD Card"); 
		final String state = Environment.getExternalStorageState();
		if (D) Log.d(TAG, "External Storage State: " + state);
		mAllowedStates = null;
		mContext = context;
		if (_root == null)
		    throw new SDCardStateException(state);
	}
	
	protected SDCardFileManager(Context context, String[] allowedStates) throws SDCardStateException {
		super(context.getExternalFilesDir(null));
		if (D) Log.d(TAG, "Creating External SD Card");
		final String state = Environment.getExternalStorageState();
		if (D) Log.d(TAG, "External Storage State: " + state);
		mAllowedStates = allowedStates;
		mContext = context;
		final int size = allowedStates.length;
		for (int i = 0; i < size; i++) {
			if (!allowedStates[i].equals(state))
				throw new SDCardStateException(state);
		}
	}
		
	public boolean isCurrentStateValid() {
		final String state = Environment.getExternalStorageState();
		if (mAllowedStates == null)
			return (Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
		else {
			final int size = mAllowedStates.length;
			for (int i = 0; i < size; i++) {
				if (!mAllowedStates[i].equals(state))
					return false;
			}
			return true;
		}
	}
	
	Context getContext() {
		return mContext;
	}
	
}