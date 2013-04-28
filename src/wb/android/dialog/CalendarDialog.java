package wb.android.dialog;

import java.sql.Date;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

public abstract class CalendarDialog {

	protected int day, month, year;
	private final int _origDay, _origMonth, _origYear;
	private final Calendar _calendar;

	public CalendarDialog() {
		_calendar = Calendar.getInstance();
		_origDay = day = _calendar.get(Calendar.DAY_OF_MONTH);
		_origMonth = month = _calendar.get(Calendar.MONTH);
		_origYear = year = _calendar.get(Calendar.YEAR);
	}
	
	public CalendarDialog(final int day, final int month, final int year) {
		_calendar = Calendar.getInstance();
		_origDay = this.day = day;
		_origMonth = this.month = month;
		_origYear = this.year = year;
	}
	
	public CalendarDialog(Date date) {
		_calendar = Calendar.getInstance();
		_calendar.setTime(date);
		_origDay = day = _calendar.get(Calendar.DAY_OF_MONTH);
		_origMonth = month = _calendar.get(Calendar.MONTH);
		_origYear = year = _calendar.get(Calendar.YEAR);
	}
	
	private final DatePickerDialog.OnDateSetListener _dateListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int newYear, int newMonth, int newDay) {
			day = newDay;
			month = newMonth;
			year = newYear;
			CalendarDialog.this.onDateSet(newDay, newMonth, newYear);
		}
	};
	
	public abstract void onDateSet(int day, int month, int year);
	
	public void reset() {
		day = _origDay;
		month = _origMonth;
		year = _origYear;
	}
	
	public void set(Date date) {
		if (date == null) {reset(); return;}
		_calendar.setTime(date);
		day = _calendar.get(Calendar.DAY_OF_MONTH);
		month = _calendar.get(Calendar.MONTH);
		year = _calendar.get(Calendar.YEAR);
	}
	
	public DatePickerDialog buildDialog(Context context) {
		return new DatePickerDialog(context, _dateListener, year, month, day);
	}
	
	public final Date getOrigDate() {
		return new Date(_origYear - 1900, _origMonth, _origDay);  //**This date constructor is deprecated
	}

}
