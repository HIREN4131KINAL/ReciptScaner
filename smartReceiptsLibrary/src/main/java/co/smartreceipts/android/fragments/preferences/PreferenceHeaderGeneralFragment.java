package co.smartreceipts.android.fragments.preferences;

import co.smartreceipts.android.R;

public class PreferenceHeaderGeneralFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_general;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesGeneral(this);
    }
}
