package co.smartreceipts.android.date;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import java.sql.Date;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;

@ApplicationScope
public class DateManager {

	@Inject UserPreferenceManager mPreferences;
	private Date mDate;
	private DateEditTextListener mDateEditTextListener;
	private MyCalendarDialog.Listener mListener;

	@Inject
	public DateManager() {
	}

	public final void setCachedDate(Date date) {
		mDate = date;
	}
	
	public final Date getCachedDate(Date date) {
		return mDate;
	}
	
    private final void initCalendar(DateEditText edit, Dialog dialog) {
		Context activityContext = edit.getContext();
    	MyCalendarDialog calendar = new MyCalendarDialog(activityContext, this);
    	calendar.set(edit.date);
    	calendar.setEditText(edit);
    	calendar.buildDialog(activityContext).show();
    	calendar.setListener(mListener);
		if (dialog != null) { dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); }
    }
    
    private final void initDurationCalendar(DateEditText start, DateEditText end) {
		Context activityContext = start.getContext();
		MyCalendarDialog calendar = new MyCalendarDialog(activityContext, this);
		calendar.set(start.date);
		calendar.setEditText(start);
		calendar.setEnd(end, mPreferences.get(UserPreference.General.DefaultReportDuration));
		calendar.buildDialog(activityContext).show();
    }
    
    public OnClickListener getDateEditTextListener() {
    	mDateEditTextListener = new DateEditTextListener(this);
    	mListener = null;
    	return mDateEditTextListener;
    }
    
    public OnClickListener getDateEditTextListener(MyCalendarDialog.Listener listener) {
    	mDateEditTextListener = new DateEditTextListener(this);
    	mListener = listener;
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
