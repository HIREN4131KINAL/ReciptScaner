package wb.android.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public abstract class ProgressTask<T, V> extends AsyncTask<T, Void, V> {

	private WeakReference<ProgressDialog> mDialogReference;
	private final String progressMessage;
	private final boolean showDialog;
	
	public ProgressTask(Context context, String progressMessage, boolean showDialog) {
		this.progressMessage = progressMessage;
		this.showDialog = showDialog;
		if (showDialog) this.mDialogReference = new WeakReference<ProgressDialog>(new ProgressDialog(context));
	}
	
	protected final void onPreExecute() {
		if (showDialog) {
			final ProgressDialog progress = mDialogReference.get();
			if (progress != null) {
				progress.setMessage(progressMessage);
				progress.setIndeterminate(true);
				progress.setCancelable(false);
		        progress.show();
			}
		}
	}
	
	@Override
	protected final void onPostExecute(V v) {
		if (showDialog) {
			final ProgressDialog progress = mDialogReference.get();
			if (progress != null) {
				if (progress.isShowing()) {
					progress.dismiss();
				}
			}
		}
		onTaskCompleted(v);
	}
	
	protected abstract void onTaskCompleted(V v);
	
	
}
