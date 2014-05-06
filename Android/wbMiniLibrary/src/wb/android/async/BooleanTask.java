package wb.android.async;

import android.os.AsyncTask;

public abstract class BooleanTask<T> extends AsyncTask<T, Void, Boolean> {

	private final BooleanTaskCompleteDelegate delegate;
	protected final int taskID;
	
	public BooleanTask(BooleanTaskCompleteDelegate delegate, int taskID) {
		this.delegate = delegate;
		this.taskID = taskID;
	}
	
	@Override
	protected final void onPostExecute(Boolean success) {
		delegate.onBooleanTaskComplete(taskID, success);
	}

}