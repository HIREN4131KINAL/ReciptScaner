package co.smartreceipts.android.fragments.preferences;

import co.smartreceipts.android.R;

public class PreferenceHeaderProFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_pro;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configureProPreferences(this);
    }
}
