package wb.android.dialog;

import android.app.Activity;
import android.content.DialogInterface;

public abstract class DirectDialogOnClickListener<T extends Activity> implements DialogInterface.OnClickListener {
	
	public final T activity;
	
	public DirectDialogOnClickListener(T activity) {
		this.activity = activity;
	}


}
