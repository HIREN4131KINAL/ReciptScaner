package co.smartreceipts.android.fragments.preferences;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AccountSettingsPreferenceFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * TODO: Add the include CSV Columns Headers to this menu (and/or maybe the CSV action bar?)
		 */
	}
	
}
