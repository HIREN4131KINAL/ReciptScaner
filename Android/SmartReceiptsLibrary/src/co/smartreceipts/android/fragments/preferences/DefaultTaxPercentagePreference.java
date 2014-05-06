package co.smartreceipts.android.fragments.preferences;

import java.text.DecimalFormat;

import wb.android.preferences.FloatSummaryEditTextPreference;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

public class DefaultTaxPercentagePreference extends FloatSummaryEditTextPreference {

	public DefaultTaxPercentagePreference(Context context) {
		super(context);
	}

	public DefaultTaxPercentagePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DefaultTaxPercentagePreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public CharSequence getSummary() {
		if (TextUtils.isEmpty(getText())) { //Add a zero check
			return new String();
		}
		else {
			try {
				float value = Float.parseFloat(getText());
				if (value <= 0) {
					return new String();
				}
				DecimalFormat decimalFormat = new DecimalFormat();
				decimalFormat.setMaximumFractionDigits(2);
				decimalFormat.setMinimumFractionDigits(2);
				decimalFormat.setGroupingUsed(false);
				return decimalFormat.format(value) + "%";
			} catch (NumberFormatException e) {
				return new String();
			}
		}
	}
}
