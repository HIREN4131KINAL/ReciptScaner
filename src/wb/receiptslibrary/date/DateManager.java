package wb.receiptslibrary.date;

import java.sql.Date;

import wb.receiptslibrary.SmartReceiptsActivity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

public class DateManager {
	
	private SmartReceiptsActivity mActivity;
	private MyCalendarDialog mCalendar;
	private Date _mDate;
	private DateEditTextListener mDateEditTextListener;
	
	public DateManager(SmartReceiptsActivity activity) {
		mActivity = activity;
	}
	
	public final void setCachedDate(Date date) {
		_mDate = date;
	}
	
	public final Date getCachedDate(Date date) {
		return _mDate;
	}
	
    private final void initCalendar(DateEditText edit, Dialog dialog) {
    	if (mCalendar == null)
			mCalendar = new MyCalendarDialog(mActivity, this);
		mCalendar.set(edit.date);
		mCalendar.setEditText(edit);
		mCalendar.buildDialog(mActivity).show();
		if (dialog != null) { dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); }
    }
    
    private final void initDurationCalendar(DateEditText start, DateEditText end) {
		if (mCalendar == null)
			mCalendar = new MyCalendarDialog(mActivity, this);
		mCalendar.set(start.date);
		mCalendar.setEditText(start);
		mCalendar.setEnd(end, mActivity.getPersistenceManager().getPreferences().getDefaultTripDuration());
		mCalendar.buildDialog(mActivity).show();
    }
    
    public OnClickListener getDateEditTextListener() {
    	mDateEditTextListener = new DateEditTextListener(this);
    	return mDateEditTextListener;
    }
    
    public void setDateEditTextListenerDialogHolder(Dialog dialogHolder) {
    	if (mDateEditTextListener != null)
    		mDateEditTextListener.setDialogHolder(dialogHolder);
    }
    
    public OnClickListener getDurationDateEditTextListener(DateEditText end) {
    	DurationDateEditTextListener durationDateListener = new DurationDateEditTextListener(this);
    	durationDateListener.setEnd(end);
    	return durationDateListener;
    }
    

	
	//Private Listener Classes
	private final class DateEditTextListener implements OnClickListener {
		
		private final DateManager mHolder;
		private Dialog mDialogHolder;
		
		public DateEditTextListener(DateManager holder) {
			mHolder = holder;
		}
		
		public final void setDialogHolder(Dialog dialogHolder) {
			mDialogHolder = dialogHolder;
		}
		
		@Override 
		public final void onClick(final View v) {
			mHolder.initCalendar((DateEditText)v, mDialogHolder); 
		}
	}
	
	private final class DurationDateEditTextListener implements OnClickListener {
		
		private final DateManager mHolder;
		private DateEditText mEnd;
		
		public DurationDateEditTextListener(DateManager holder) {
			mHolder = holder;
		}
		
		public final void setEnd(DateEditText end) {
			mEnd = end;
		}
		
		@Override 
		public final void onClick(final View v) {
			mHolder.initDurationCalendar((DateEditText)v, mEnd);
		}
	}
	
}
