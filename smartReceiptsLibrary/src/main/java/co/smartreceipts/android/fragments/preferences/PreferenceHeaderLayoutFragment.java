package co.smartreceipts.android.fragments.preferences;

import co.smartreceipts.android.R;

public class PreferenceHeaderLayoutFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_layout;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesLayoutCustomizations(this);
    }
}
