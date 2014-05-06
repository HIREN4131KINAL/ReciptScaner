package wb.android.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

public class IntegerEditTextPreference extends EditTextPreference {

    public IntegerEditTextPreference(Context context) {
        super(context);
    }

    public IntegerEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntegerEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
    	if (TextUtils.isEmpty(defaultReturnValue)) {
    		return String.valueOf(getPersistedInt(0));
    	}
    	else {
	    	try {
	    		return String.valueOf(getPersistedInt(Integer.parseInt(defaultReturnValue)));
			} catch (NumberFormatException e) {
				return String.valueOf(getPersistedInt(0));
			}
    	}
    }

    @Override
    protected boolean persistString(String value) {
    	if (TextUtils.isEmpty(value)) {
			return false; // Don't persist zero here (we'll recurse and always end at zero)
    	}
    	else {
	    	try {
	    		return persistInt(Integer.valueOf(value));
			} catch (NumberFormatException e) {
				return false;
			}
    	}
    }
}
