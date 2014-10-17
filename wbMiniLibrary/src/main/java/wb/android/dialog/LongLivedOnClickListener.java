package wb.android.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;

public abstract class LongLivedOnClickListener implements View.OnClickListener {
	
	private DialogInterface _dialog;
	private int _whichButton;
	
	void set(Dialog dialog, int whichButton) {
		_dialog = dialog;
		_whichButton = whichButton;
	}
	
	@Override
	public final void onClick(View v) {
		this.onClick(_dialog, _whichButton);
	}
	
	public abstract void onClick(DialogInterface dialog, int whichButton);

}
