package wb.receiptspro;

import java.sql.Date;

import android.text.format.DateFormat;
import wb.android.dialog.CalendarDialog;

public class MyCalendarDialog extends CalendarDialog {

	private final SmartReceiptsActivity _activity;
	private DateEditText _edit, _end;
	private int _duration;
	
	public MyCalendarDialog(final SmartReceiptsActivity activity) {
		super();
		_activity = activity;
	}
	
	@Override
	public final void onDateSet(int day, int month, int year) {
		final Date date = new Date(year - 1900, month, day);  //**This date constructor is deprecated
		String dateString = DateFormat.getDateFormat(_activity).format(date);
		if (_edit != null) { 
			_edit.setText(dateString);
			_edit.date = date;
		}
		if (_end != null && _end.date == null) { //ugly hack (order dependent set methods below)
			final Date endDate = new Date(date.getTime() + _duration*86400000);
			String endString = DateFormat.getDateFormat(_activity).format(endDate);
			_end.setText(endString);
			_end.date = endDate;
		}
	}
	
	public final void setEditText(final DateEditText edit) {
		_edit = edit;
		_end = null;
	}
	
	public final void setEnd(final DateEditText end, final int duration) {
		_end = end;
		_duration = duration;
	}

}
