package wb.android.loaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.AsyncTaskLoader;

public class SharedPreferencesLoader extends AsyncTaskLoader<SharedPreferences> {

	private SharedPreferences mPreferences;
	private String mName;
	private int mMode;
	
	/**
	 * Creates an implementation of the {@link AsyncTaskLoader} for Android {@link SharedPreferences}.
	 * This implementation uses the {@link PreferenceManager#getDefaultSharedPreferences(Context)} method
	 * to generate it's SharedPreferences.
	 * 
	 * @param context - the Context to generate these preferences from
	 */
	public SharedPreferencesLoader(Context context) {
		super(context);
	}
	
	/**
	 * Creates an implementation of the {@link AsyncTaskLoader} for Android {@link SharedPreferences}.
	 * This implementation takes a name as input in order to generate an named set of {@link SharedPreferences}
	 * via the {@link Context#getSharedPreferences(String, int)} method. By default, this is opened in 
	 * {@link Context#MODE_PRIVATE}.
	 * 
	 * @param context - the Context to generate these preferences
	 * @param name - the named instance of the preferences
	 */
	public SharedPreferencesLoader(Context context, String name) {
		super(context);
		mName = name;
		mMode = 0;
	}
	
	/**
	 * Creates an implementation of the {@link AsyncTaskLoader} for Android {@link SharedPreferences}.
	 * This implementation takes a name as input in order to generate an named set of {@link SharedPreferences}
	 * via the {@link Context#getSharedPreferences(String, int)} method. 
	 * 
	 * @param context - the Context to generate these preferences
	 * @param name - the named instance of the preferences
	 * @param mode - the mode in which to open these preferences
	 */
	public SharedPreferencesLoader(Context context, String name, int mode) {
		super(context);
		mName = name;
		mMode = mode;
	}

	@Override
	public SharedPreferences loadInBackground() {
		if (mName == null) {
			mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		}
		else {
			mPreferences = getContext().getSharedPreferences(mName, mMode);
		} 
		return mPreferences;
	}
	
	@Override
	protected void onStartLoading() {
		// If we currently have a result available, deliver it immediately.
		if (mPreferences != null) {
			deliverResult(mPreferences);
		}

		// If the data has changed since the last time it was loaded
        // or is not currently available, start a load.
		if (takeContentChanged() || mPreferences == null) {
			forceLoad(); 
		}
	}
	
	@Override
	protected void onReset() {
		super.onReset();
        onStopLoading(); // Ensure the loader is stopped
        mPreferences = null; // Clear out old data
        mName = null;
        mMode = 0;
	}

}
