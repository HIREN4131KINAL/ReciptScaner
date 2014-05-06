package co.smartreceipts.tests;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import wb.receiptspro.R;
import android.content.Context;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.activities.SettingsActivity;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.shadows.ShadowPreferenceActivity;

@RunWith(RobolectricTestRunner.class)
@Config(reportSdk = 18, shadows = {ShadowPreferenceActivity.class} )
public class PreferencesTest {

	private SmartReceiptsApplication mApp;
	private SettingsActivity mSettingsActivity;
	private Preferences mPreferences;

	@Before
	public void setup() {
		mApp = (SmartReceiptsApplication) Robolectric.application;
		final Context context = mApp.getApplicationContext();
		mPreferences = Preferences.getRoboElectricInstance(context, mApp.getFlex());
		mApp.getPersistenceManager().setPreferences(mPreferences);
		mSettingsActivity = Robolectric.buildActivity(SettingsActivity.class).create().get();
	}

	@After
	public void tearDown() {
		mSettingsActivity = null;
		mApp = null;
	}

	@Test
	public void generalPreferences() throws Exception {
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_general_trip_duration_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_general_default_currency_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_general_default_date_separator_key));
	}

	@Test
	public void receiptPreferences() throws Exception {
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_receipt_customize_categories_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_receipt_default_to_report_start_date_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_receipt_enable_autocomplete_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_receipt_expensable_only_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_receipt_include_tax_field_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_receipt_tax_percent_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_receipt_match_comment_to_category_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_receipt_match_name_to_category_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_receipt_minimum_receipts_price_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_receipt_predict_categories_key));
		assertNotNull(mSettingsActivity.findPreference(R.string.pref_receipt_show_id_key));
	}

}
