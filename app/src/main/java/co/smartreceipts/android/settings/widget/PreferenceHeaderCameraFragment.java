package co.smartreceipts.android.settings.widget;

import co.smartreceipts.android.R;

public class PreferenceHeaderCameraFragment extends
        AbstractPreferenceHeaderFragment {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences_camera;
    }

    @Override
    public void configurePreferences() {
        mSettingsActivity.configurePreferencesCamera(this);
    }
}
