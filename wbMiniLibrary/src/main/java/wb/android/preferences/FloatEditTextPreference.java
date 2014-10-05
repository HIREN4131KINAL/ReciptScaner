package wb.android.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

public class FloatEditTextPreference extends EditTextPreference {

    public FloatEditTextPreference(Context context) {
        super(context);
    }

    public FloatEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
    	if (TextUtils.isEmpty(defaultReturnValue)) {
			return String.valueOf(getPersistedFloat(0));
    	}
    	else {
	    	try {
	    		return String.valueOf(getPersistedFloat(Float.parseFloat(defaultReturnValue)));
			} catch (NumberFormatException e) {
				return String.valueOf(getPersistedFloat(0));
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
	    		return persistFloat(Float.valueOf(value));
			} catch (NumberFormatException e) {
				return false;
			}
		}
    }
}
