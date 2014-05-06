package co.smartreceipts.android.fragments.preferences;

import wb.android.preferences.IntegerSummaryEditTextPreference;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import co.smartreceipts.android.R;

public class DefaultDurationEditTextPreference extends IntegerSummaryEditTextPreference {

	public DefaultDurationEditTextPreference(Context context) {
		super(context);
	}

	public DefaultDurationEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DefaultDurationEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public CharSequence getSummary() {
		if (TextUtils.isEmpty(getText())) {
			return new String();
		}
		else {
			try {
				int days = Integer.parseInt(getText());
				if (days == 1) {
					return getContext().getString(R.string.day, Integer.toString(days));
				}
				else {
					return getContext().getString(R.string.days, Integer.toString(days));
				}
			} catch (NumberFormatException e) {
				return new String();
			}
		}
	}

}
