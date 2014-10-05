package wb.android.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

public class EmptyValueSummaryListPreference extends SummaryListPreference {

	private String mEmptyValue;
	
	public EmptyValueSummaryListPreference(Context context) {
		super(context);
		mEmptyValue = new String();
	}
	
	public EmptyValueSummaryListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mEmptyValue = new String();
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, wb.android.R.styleable.SummaryPreference);
			if (a.hasValue(wb.android.R.styleable.SummaryPreference_emptyValue)) {
				mEmptyValue = a.getString(wb.android.R.styleable.SummaryPreference_emptyValue);
			}
			a.recycle();
		}
	}
	
	@Override
	public CharSequence getSummary() {
		if (TextUtils.isEmpty(getEntry())) {
			return mEmptyValue;
		}
		else {
			return getEntry();
		}
	}
	
	public CharSequence getEmptyValue() {
		return mEmptyValue;
	}
	
	public void setEmptyValue(String emptyValue) {
		mEmptyValue = emptyValue;
	}

}