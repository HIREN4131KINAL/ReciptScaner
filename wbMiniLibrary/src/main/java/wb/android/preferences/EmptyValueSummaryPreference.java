package wb.android.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

public class EmptyValueSummaryPreference extends SummaryPreference {

	private String mEmptyValue;

	public EmptyValueSummaryPreference(Context context) {
		super(context);
		mEmptyValue = "";
	}

	public EmptyValueSummaryPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mEmptyValue = "";
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, wb.android.R.styleable.SummaryPreference);
			if (a.hasValue(wb.android.R.styleable.SummaryPreference_emptyValue)) {
				mEmptyValue = a.getString(wb.android.R.styleable.SummaryPreference_emptyValue);
			}
			a.recycle();
		}
	}

	public EmptyValueSummaryPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mEmptyValue = "";
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, wb.android.R.styleable.SummaryPreference, defStyle, 0);
			if (a.hasValue(wb.android.R.styleable.SummaryPreference_emptyValue)) {
				mEmptyValue = a.getString(wb.android.R.styleable.SummaryPreference_emptyValue);
			}
			a.recycle();
		}
	}
	
	@Override
	public CharSequence getSummary() {
		if (TextUtils.isEmpty(super.getSummary())) {
			return mEmptyValue;
		}
		else {
			return super.getSummary();
		}
	}
	
	public CharSequence getEmptyValue() {
		return mEmptyValue;
	}
	
	public void setEmptyValue(String emptyValue) {
		mEmptyValue = emptyValue;
	}

}