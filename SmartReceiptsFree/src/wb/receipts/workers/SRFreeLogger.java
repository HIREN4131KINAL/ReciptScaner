package wb.receipts.workers;

import wb.receipts.BuildConfig;
import wb.receipts.analytics.Events;
import wb.receipts.analytics.Screens;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
		// wEasyTracker.getTracker().sendException(log, false); // false indicates non-fatal exception.
	}

	@Override
	public void logScreen(Activity activity) {
		Screens.sendScreen(activity);
	}

	@Override
	public void logScreen(Fragment fragment) {
		Screens.sendScreen(fragment);
	}

	@Override
	public void logEvent(Activity activity, String action) {
		if (activity != null && !TextUtils.isEmpty(action)) {
			Events.sendEvent(activity, activity.getClass().getSimpleName(), action);
		}
	}

	@Override
	public void logEvent(Fragment fragment, String action) {
		if (fragment != null && !TextUtils.isEmpty(action)) {
			Events.sendEvent(fragment, fragment.getClass().getSimpleName(), action);
		}
	}

}
