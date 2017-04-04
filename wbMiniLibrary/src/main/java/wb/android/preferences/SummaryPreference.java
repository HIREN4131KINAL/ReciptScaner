package wb.android.preferences;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class SummaryPreference extends Preference implements Preference.OnPreferenceChangeListener {

    private OnPreferenceChangeListener mOnPreferenceChangeListener;

    public SummaryPreference(Context context) {
        super(context);
        super.setOnPreferenceChangeListener(this); //Must use the super's method here (since we overwrite)
    }

    public SummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnPreferenceChangeListener(this); //Must use the super's method here (since we overwrite)
    }

    public SummaryPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.setOnPreferenceChangeListener(this); //Must use the super's method here (since we overwrite)
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary(getSummary());
        if (mOnPreferenceChangeListener != null) {
            return mOnPreferenceChangeListener.onPreferenceChange(preference, newValue);
        }
        return true;
    }

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        mOnPreferenceChangeListener = onPreferenceChangeListener;
    }

    @Override
    public CharSequence getSummary() {
        return getPersistedString("");
    }
}