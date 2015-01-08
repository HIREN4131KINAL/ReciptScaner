package co.smartreceipts.android.fragments.preferences;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.view.MenuItem;
import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.activities.SettingsActivity;
import co.smartreceipts.android.persistence.Preferences;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PreferenceHeaderFragment extends android.preference.PreferenceFragment implements UniversalPreferences {
	
	private SettingsActivity mSettingsActivity;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof SettingsActivity) {
			mSettingsActivity = (SettingsActivity) activity;
		}
		else {
			throw new RuntimeException("PreferenceHeaderFragment requires a SettingsActivity");
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettingsActivity.setFragmentHeaderIsShowing(true);
		setHasOptionsMenu(true); // Required to simulate up navigation
        getPreferenceManager().setSharedPreferencesName(Preferences.SMART_PREFS);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(((SmartReceiptsApplication)getActivity().getApplication()).getPersistenceManager().getPreferences());

		final String key = getArguments().getString(getString(R.string.pref_header_key));
		if (getString(R.string.pref_general_header).equals(key)) {
			addPreferencesFromResource(R.xml.preferences_general);
			mSettingsActivity.configurePreferencesGeneral(this);
		}
		else if (getString(R.string.pref_receipt_header).equals(key)) {
			addPreferencesFromResource(R.xml.preferences_receipts);
			mSettingsActivity.configurePreferencesReceipts(this);
		}
		else if (getString(R.string.pref_output_header).equals(key)) {
			addPreferencesFromResource(R.xml.preferences_output);
			mSettingsActivity.configurePreferencesOutput(this);
		}
		else if (getString(R.string.pref_email_header).equals(key)) {
			addPreferencesFromResource(R.xml.preferences_email);
			mSettingsActivity.configurePreferencesEmail(this);
		}
		else if (getString(R.string.pref_camera_header).equals(key)) {
			addPreferencesFromResource(R.xml.preferences_camera);
			mSettingsActivity.configurePreferencesCamera(this);
		}
		else if (getString(R.string.pref_layout_header).equals(key)) {
			addPreferencesFromResource(R.xml.preferences_layout);
			mSettingsActivity.configurePreferencesLayoutCustomizations(this);
		}
        else if (getString(R.string.pref_distance_header).equals(key)) {
            addPreferencesFromResource(R.xml.preferences_distance);
            mSettingsActivity.configurePreferencesDistance(this);
        }
		else if (getString(R.string.pref_help_header).equals(key)) {
			addPreferencesFromResource(R.xml.preferences_help);
			mSettingsActivity.configurePreferencesHelp(this);
		}
		else if (getString(R.string.pref_about_header).equals(key)) {
			addPreferencesFromResource(R.xml.preferences_about);
			mSettingsActivity.configurePreferencesAbout(this);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getActivity().getActionBar().setTitle(getArguments().getString(getString(R.string.pref_header_key)));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getActivity().onBackPressed();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mSettingsActivity.setFragmentHeaderIsShowing(false);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mSettingsActivity = null; //Garbage Collection
	}
	
	@Override
	public Preference findPreference(int stringId) {
		return findPreference(getString(stringId));
	}

}
