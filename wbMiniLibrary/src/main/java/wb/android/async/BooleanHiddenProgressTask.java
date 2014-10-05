package wb.android.async;

import android.app.Activity;

public abstract class BooleanHiddenProgressTask<T> extends ProgressTask<T, Boolean>{

	private final BooleanTaskCompleteDelegate delegate;
	private final int taskID;
	
	public BooleanHiddenProgressTask(Activity activity, BooleanTaskCompleteDelegate delegate, int taskID) {
		super(activity, "", false);
		this.delegate = delegate;
		this.taskID = taskID;
	}
	
	@Override
	protected void onTaskCompleted(Boolean success) {
		delegate.onBooleanTaskComplete(taskID, success);
	}

}