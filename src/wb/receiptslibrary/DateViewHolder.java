package wb.receiptslibrary;

import java.sql.Date;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wb.navigation.ViewHolder;

//This will be overriden by the receipt holder and trip holder to take advantage of these methods
public class DateViewHolder extends ViewHolder<SmartReceiptsActivity>{
	
	private static final boolean D = SmartReceiptsActivity.D;
	private static final String TAG = "DateViewHolder";
	
	private MyCalendarDialog _calendar;
	private Date _cachedDate;
	private DateEditTextListener _dateEditTextListener;

	public DateViewHolder(SmartReceiptsActivity activity) {
		super(activity);
	}
	
	public final void setCachedDate(Date date) {
		_cachedDate = date;
	}
	
	public final Date getCachedDate(Date date) {
		return _cachedDate;
	}
	
    private final void initCalendar(DateEditText edit, Dialog dialog) {
    	if (_calendar == null)
			_calendar = new MyCalendarDialog(activity, this);
		_calendar.set(edit.date);
		_calendar.setEditText(edit);
		_calendar.buildDialog(activity).show();
		if (dialog != null) { dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); }
    }
    
    private final void initDurationCalendar(DateEditText start, DateEditText end) {
		if (_calendar == null)
			_calendar = new MyCalendarDialog(activity, this);
		_calendar.set(start.date);
		_calendar.setEditText(start);
		_calendar.setEnd(end, activity.getPreferences().getDefaultTripDuration());
		_calendar.buildDialog(activity).show();
    }
    
    public OnClickListener getDateEditTextListener() {
    	_dateEditTextListener = new DateEditTextListener(this);
    	return _dateEditTextListener;
    }
    
    public void setDateEditTextListenerDialogHolder(Dialog dialogHolder) {
    	if (_dateEditTextListener != null)
    		_dateEditTextListener.setDialogHolder(dialogHolder);
    }
    
    public OnClickListener getDurationDateEditTextListener(DateEditText end) {
    	DurationDateEditTextListener durationDateListener = new DurationDateEditTextListener(this);
    	durationDateListener.setEnd(end);
    	return durationDateListener;
    }
    
    public OnItemSelectedListener getSpinnerSelectionListener(TextView nameBox, TextView commentBox, ArrayAdapter<CharSequence> categories) {
    	return new SpinnerSelectionListener(nameBox, commentBox, categories);
    }
	
	//Private Listener Classes
	private final class DateEditTextListener implements OnClickListener {
		private final DateViewHolder mHolder;
		private Dialog mDialogHolder;
		public DateEditTextListener(DateViewHolder holder) {mHolder = holder;}
		public final void setDialogHolder(Dialog dialogHolder) { mDialogHolder = dialogHolder; }
		@Override public final void onClick(final View v) { mHolder.initCalendar((DateEditText)v, mDialogHolder); }
	}
	private final class DurationDateEditTextListener implements OnClickListener {
		private final DateViewHolder mHolder;
		private DateEditText mEnd;
		public DurationDateEditTextListener(DateViewHolder holder) {mHolder = holder;}
		public final void setEnd(DateEditText end) {mEnd = end;}
		@Override public final void onClick(final View v) {mHolder.initDurationCalendar((DateEditText)v, mEnd);if (D) Log.d(TAG, "Here2");}
	}
	private final class SpinnerSelectionListener implements OnItemSelectedListener {
		private final TextView mNameBox, mCommentBox;
		private final ArrayAdapter<CharSequence> mCategories;
		public SpinnerSelectionListener(TextView nameBox, TextView commentBox, ArrayAdapter<CharSequence> categories) {mNameBox = nameBox; mCommentBox = commentBox; mCategories = categories;}
		@Override public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
			if (mNameBox != null) mNameBox.setText(mCategories.getItem(position));
			if (mCommentBox != null) mCommentBox.setText(mCategories.getItem(position)); 
		}
		@Override public void onNothingSelected(AdapterView<?> arg0) {}	
	}

}
