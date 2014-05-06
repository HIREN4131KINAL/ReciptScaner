package wb.receipts.workers;

import wb.receipts.BuildConfig;
import wb.receipts.analytics.Screens;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;
import co.smartreceipts.android.workers.Logger;
import co.smartreceipts.android.workers.WorkerManager;

public class SRFreeLogger extends Logger {

	protected SRFreeLogger(WorkerManager manager) {
		super(manager);
	}

	@Override
	public void logError(String log) {
		if (BuildConfig.DEBUG) {
			Log.e("SRErrorLog", log);
		}
		//wEasyTracker.getTracker().sendException(log, false); // false indicates non-fatal exception.
	}

	@Override
	public void logScreen(Activity activity) {
		Screens.sendScreen(activity);
	}

	@Override
	public void logScreen(Fragment fragment) {
		Screens.sendScreen(fragment);
	}

}
