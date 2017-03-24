package co.smartreceipts.android.settings.widget;

import co.smartreceipts.android.R;

public class PreferenceHeaderEmailFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_email;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesEmail(this);
    }
}
