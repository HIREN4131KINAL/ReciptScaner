package wb.receiptslibrary.date;

import java.sql.Date;

import wb.android.dialog.CalendarDialog;
import wb.receiptslibrary.SmartReceiptsActivity;
import android.text.format.DateFormat;

public class MyCalendarDialog extends CalendarDialog {

	private final SmartReceiptsActivity _activity;
	private final DateManager _manager;
	private DateEditText _edit, _end;
	private long _duration;
	
	public MyCalendarDialog(final SmartReceiptsActivity activity, final DateManager manager) {
		super();
		_activity = activity;
		_manager = manager;
	}
	
	@Override
	public final void onDateSet(int day, int month, int year) {
		final Date date = new Date(year - 1900, month, day);  //**This Date constructor is deprecated
		_manager.setCachedDate(date);
		String dateString = DateFormat.getDateFormat(_activity).format(date);
		//This block is for _edit
		if (_edit != null) { 
			_edit.setText(dateString);
			_edit.date = date;
		}
		//This block is for _end
		if (_end != null && _end.date == null) { //ugly hack (order dependent set methods below)
			final Date endDate = new Date(date.getTime() + _duration*86400000L+3600000L); //+3600000 for DST hack
			String endString = DateFormat.getDateFormat(_activity).format(endDate);
			_end.setText(endString);
			_end.date = endDate;
		}
	}
	
	public final void setEditText(final DateEditText edit) {
		_edit = edit;
		_end = null;
	}
	
	public final void setEnd(final DateEditText end, final long duration) {
		_end = end;
		_duration = duration;
	}

}
