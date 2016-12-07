package co.smartreceipts.android.fragments.preferences;

import co.smartreceipts.android.R;

public class PreferenceHeaderReportOutputFragment
        extends AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_output;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesOutput(this);
    }
}
