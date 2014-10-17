package wb.android.ui;

import java.sql.Date;

import wb.android.dialog.CalendarDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class DateEditText extends EditText {

	private Date mDate;
	private Listener mListener;
	
	public interface Listener {
		public void onDateSet(Date date);
	}
	
	public DateEditText(Context context) {
		super(context);
		init();
	}
	
	public DateEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public DateEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		mDate = null;
		this.setFocusableInTouchMode(false);
		this.setOnClickListener(new DateEditTextOnClickListener());
	}
	
	public Date getDate() {
		return mDate;
	}
	
	private final class DateEditTextOnClickListener implements OnClickListener {
		@Override 
		public final void onClick(final View v) {
			new MyCalendarDialog().buildDialog(getContext()).show();
		}
	}
	
	private class MyCalendarDialog extends CalendarDialog {

		@Override
		@SuppressWarnings("deprecation")
		public void onDateSet(int day, int month, int year) {
			mDate = new Date(year - 1900, month, day);
			DateEditText.this.setText(DateFormat.getDateFormat(getContext()).format(mDate));
			if (mListener != null) {
				mListener.onDateSet(getDate());
			}
		}
		
	}

}
