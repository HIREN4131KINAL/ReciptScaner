package wb.android.dialog;

import android.app.Activity;

public abstract class DirectLongLivedOnClickListener<T extends Activity> extends LongLivedOnClickListener {
	
	public final T activity;
	
	public DirectLongLivedOnClickListener(T activity) {
		this.activity = activity;
	}
}
