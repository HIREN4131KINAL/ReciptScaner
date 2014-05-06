package co.smartreceipts.android.workers;

import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * Currently, just a stub class - This may be used at a later date to track
 * usage information
 * @author WRB
 *
 */
public class Logger extends WorkerChild {

	protected Logger(WorkerManager manager) {
		super(manager);
	}

	public void logScreen(Activity activity) {

	}

	public void logScreen(Fragment fragment) {

	}

	public void logError(String log) {

	}

}
