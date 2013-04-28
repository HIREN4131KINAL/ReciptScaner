package wb.receiptslibrary;

import java.sql.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class DateEditText extends EditText {

	public Date date;	
	
	public DateEditText(Context context) {
		super(context);
		date = null;
	}
	
	public DateEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		date = null;
	}
	
	public DateEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		date = null;
	}

}
