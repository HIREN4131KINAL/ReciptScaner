package wb.android.async;

import android.content.Context;

public abstract class BooleanProgressTask<T> extends ProgressTask<T, Boolean>{

	private final BooleanTaskCompleteDelegate delegate;
	private final int taskID;
	
	public BooleanProgressTask(Context context, BooleanTaskCompleteDelegate delegate, String progressMessage, int taskID) {
		super(context, progressMessage, true);
		this.delegate = delegate;
		this.taskID = taskID;
	}
	
	@Override
	protected void onTaskCompleted(Boolean success) {
		delegate.onBooleanTaskComplete(taskID, success);
	}

}
