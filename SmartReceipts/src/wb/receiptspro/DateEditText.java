package wb.receiptspro;

import java.sql.Date;

import android.content.Context;
import android.widget.EditText;

public class DateEditText extends EditText {

	public Date date;
	
	public DateEditText(Context context) {
		super(context);
		date = null;
	}

}
