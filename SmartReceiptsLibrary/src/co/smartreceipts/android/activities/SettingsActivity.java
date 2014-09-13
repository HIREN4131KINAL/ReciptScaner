package co.smartreceipts.android.activities;

import java.util.ArrayList;
import java.util.List;

import wb.android.util.AppRating;
import wb.android.util.Utils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.fragments.preferences.DefaultTaxPercentagePreference;
import co.smartreceipts.android.fragments.preferences.MinimumPriceEditTextPreference;
import co.smartreceipts.android.fragments.preferences.PreferenceHeaderFragment;
import co.smartreceipts.android.fragments.preferences.UniversalPreferences;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.workers.EmailAssistant;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, UniversalPreferences {

	public static final String TAG = "SettingsActivity";

	private boolean mIsUsingHeaders;

	/**
	 * Ugly hack to determine if a fragment header is currently showing or not. See if I can replace by counting the
	 * fragment manager entries
	 */
	private boolean mIsFragmentHeaderShowing = false;

	@Override
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mIsUsingHeaders = getResources().getBoolean(R.bool.isTablet);

		if (!mIsUsingHeaders) {
			// Load the legacy preferences headers
			getPreferenceManager().setSharedPreferencesName(Preferences.SMART_PREFS);
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(((SmartReceiptsApplication) getApplication()).getPersistenceManager().getPreferences());
			addPreferencesFromResource(R.xml.preference_legacy);
			configurePreferencesGeneral(this);
			configurePreferencesReceipts(this);
			configurePreferencesOutput(this);
			configurePreferencesEmail(this);
			configurePreferencesCamera(this);
			configurePreferencesLayoutCustomizations(this);
			configurePreferencesHelp(this);
			configurePreferencesAbout(this);
		}
	}

	@Override
	protected void onStart() {
		((SmartReceiptsApplication) getApplication()).getWorkerManager().getLogger().logScreen(this);
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setTitle(R.string.menu_main_settings);
		setUpActionBar11();
		setUpActionBar14();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setUpActionBar11() {
		if (Utils.ApiHelper.hasHoneycomb()) {
			getActionBar().setTitle(R.string.menu_main_settings);
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setUpActionBar14() {
		if (Utils.ApiHelper.hasICS()) {
			getActionBar().setHomeButtonEnabled(true);
		}
	}

	// Called only on Honeycomb and later
	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		// Called before onCreate it seems
		mIsUsingHeaders = getResources().getBoolean(R.bool.isTablet);
		if (mIsUsingHeaders) {
			loadHeadersFromResource(R.xml.preference_headers, target);
		}
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		if (PreferenceHeaderFragment.class.getName().equals(fragmentName)) {
			return true;
		}
		else {
			return false;
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mIsFragmentHeaderShowing) { // If we're actively showing a fragment, let it handle the call
			return super.onOptionsItemSelected(item);
		}
		if (item.getItemId() == android.R.id.home) {
			final Intent upIntent = new Intent(this, ((SmartReceiptsApplication) getApplication()).getTopLevelActivity());
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// This activity is NOT part of this app's task, so create a new task
				// when navigating up, with a synthesized back stack.
				TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent) // Add all of this activity's
																						// parents to the back stack
				.startActivities(); // Navigate up to the closest parent
			}
			else {
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

	public boolean isUsingHeaders() {
		return mIsUsingHeaders;
	}

	public void setFragmentHeaderIsShowing(boolean isShowing) {
		mIsFragmentHeaderShowing = isShowing;
	}

	@Override
	@SuppressWarnings("deprecation")
	public Preference findPreference(int stringId) {
		return findPreference(getString(stringId));
	}

	public void configurePreferencesGeneral(UniversalPreferences universal) {
		// Get the currency list
		PersistenceManager persistenceManager = ((SmartReceiptsApplication) getApplication()).getPersistenceManager();
		ArrayList<CharSequence> currencyList = persistenceManager.getDatabase().getCurrenciesList();
		CharSequence[] currencyArray = new CharSequence[currencyList.size()];
		currencyList.toArray(currencyArray);

		// Get the date separator list
		CharSequence[] dateSeparators;
		final String defaultSepartor = persistenceManager.getPreferences().getDateSeparator();
		if (!defaultSepartor.equals("-") && !defaultSepartor.equals("/")) {
			dateSeparators = new CharSequence[3];
			dateSeparators[2] = defaultSepartor;
		}
		else {
			dateSeparators = new CharSequence[2];
		}
		dateSeparators[0] = "/";
		dateSeparators[1] = "-";

		// Set up the ListPreference data
		ListPreference currencyPreference = (ListPreference) universal.findPreference(R.string.pref_general_default_currency_key);
		currencyPreference.setEntries(currencyArray);
		currencyPreference.setEntryValues(currencyArray);
		currencyPreference.setValue(persistenceManager.getPreferences().getDefaultCurreny());
		ListPreference dateSeparatorPreference = (ListPreference) universal.findPreference(R.string.pref_general_default_date_separator_key);
		dateSeparatorPreference.setEntries(dateSeparators);
		dateSeparatorPreference.setEntryValues(dateSeparators);
		dateSeparatorPreference.setValue(defaultSepartor);
	}

	public void configurePreferencesReceipts(UniversalPreferences universal) {
		// Set on Preference Click Listeners for all that require it
		universal.findPreference(R.string.pref_receipt_customize_categories_key).setOnPreferenceClickListener(this);
		universal.findPreference(R.string.pref_receipt_payment_methods_key).setOnPreferenceClickListener(this);

		// Here we restore our current values (easier than getting the FloatEditText stuff to work)
		Preferences preferences = ((SmartReceiptsApplication) getApplication()).getPersistenceManager().getPreferences();
		DefaultTaxPercentagePreference taxPercentagePreference = (DefaultTaxPercentagePreference) universal.findPreference(R.string.pref_receipt_tax_percent_key);
		taxPercentagePreference.setText(Float.toString(preferences.getDefaultTaxPercentage()));
		MinimumPriceEditTextPreference minimumPriceEditTextPreference = (MinimumPriceEditTextPreference) universal.findPreference(R.string.pref_receipt_minimum_receipts_price_key);
		minimumPriceEditTextPreference.setText(Float.toString(preferences.getMinimumReceiptPriceToIncludeInReports()));

	}

	public void configurePreferencesOutput(UniversalPreferences universal) {
		// Set on Preference Click Listeners for all that require it
		universal.findPreference(R.string.pref_output_custom_csv_key).setOnPreferenceClickListener(this);
		universal.findPreference(R.string.pref_output_custom_pdf_key).setOnPreferenceClickListener(this);
	}

	public void configurePreferencesEmail(UniversalPreferences universal) {
		Preference subjectPreference = universal.findPreference(R.string.pref_email_default_email_subject_key);
		subjectPreference.setDefaultValue(((SmartReceiptsApplication) getApplication()).getFlex().getString(this, R.string.EMAIL_DATA_SUBJECT));
	}

	public void configurePreferencesCamera(UniversalPreferences universal) {

	}

	public void configurePreferencesLayoutCustomizations(UniversalPreferences universal) {

	}

	public void configurePreferencesHelp(UniversalPreferences universal) {
		// Set on Preference Click Listeners for all that require it
		universal.findPreference(R.string.pref_help_send_feedback_key).setOnPreferenceClickListener(this);
		universal.findPreference(R.string.pref_help_send_love_key).setOnPreferenceClickListener(this);
		universal.findPreference(R.string.pref_help_support_email_key).setOnPreferenceClickListener(this);
	}

	public void configurePreferencesAbout(UniversalPreferences universal) {
		// Set up Version Summary
		Preference versionPreference = universal.findPreference(R.string.pref_about_version_key);
		versionPreference.setSummary(getAppVersion());
	}

	private String getAppVersion() {
		try {
			return getPackageManager().getPackageInfo(getString(R.string.package_name), 0).versionName;
		}
		catch (NameNotFoundException e) {
			return null;
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if (key.equals(getString(R.string.pref_receipt_customize_categories_key)) || key.equals(getString(R.string.pref_output_custom_csv_key)) || key.equals(getString(R.string.pref_output_custom_pdf_key)) || key.equals(getString(R.string.pref_receipt_payment_methods_key))) {

			final Intent intent = new Intent(this, SettingsViewerActivity.class);
			intent.putExtra(SettingsViewerActivity.KEY_FLAG, key);
			startActivity(intent);
			return true;
		}
		else if (key.equals(getString(R.string.pref_help_send_love_key))) { // Dark Pattern... Send Love => AppStore.
																			// Others for email
			startActivity(AppRating.getRatingIntent(this, getString(R.string.package_name)));
			return true;
		}
		else if (key.equals(getString(R.string.pref_help_send_feedback_key))) {
			final Intent intent = EmailAssistant.getEmailDeveloperIntent(getString(R.string.feedback, getString(R.string.sr_app_name)));
			startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
			return true;
		}
		else if (key.equals(getString(R.string.pref_help_support_email_key))) {
			final Intent intent = EmailAssistant.getEmailDeveloperIntent(getString(R.string.support, getString(R.string.sr_app_name)), getDebugScreen());
			startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
			return true;
		}
		else {
			return false;
		}
	}

	private String getDebugScreen() {
		String debug = "Debug-information: \n" + "Smart Receipts Version: " + getAppVersion() + "\n" + "Brand: " + android.os.Build.BRAND + "\n" + "CPU: " + android.os.Build.CPU_ABI + "\n" + "OS API Level: " + android.os.Build.VERSION.SDK_INT + "\n" + "Device: " + android.os.Build.DEVICE + "\n" + "Model (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")\n" + "Two-Paned: " + mIsUsingHeaders;
		return debug;
	}

}