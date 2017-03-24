package co.smartreceipts.android.settings.widget;

import co.smartreceipts.android.R;

public class PreferenceHeaderAboutFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_about;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesAbout(this);
    }
}
