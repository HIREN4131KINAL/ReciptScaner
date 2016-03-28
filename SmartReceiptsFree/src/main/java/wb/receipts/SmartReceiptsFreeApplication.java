package wb.receipts;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wb.receipts.workers.SRFreeWorkerManager;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.workers.WorkerManager;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class SmartReceiptsFreeApplication extends SmartReceiptsApplication {

	private final Map<Integer, Tracker> mTrackers = new ConcurrentHashMap<Integer, Tracker>();

	@Override
	public void onCreate() {
		super.onCreate();
		BugSenseHandler.initAndStartSession(this, "01de172a");
	}

	public Tracker getTracker(int xmlResId) {
		Tracker tracker = mTrackers.get(xmlResId);
		if (tracker != null) {
			return tracker;
		}
		else {
			tracker = GoogleAnalytics.getInstance(getApplicationContext()).newTracker(xmlResId);
			if (tracker != null) {
				mTrackers.put(xmlResId, tracker);
			}
			return tracker;
		}
	}

	@Override
	protected WorkerManager instantiateWorkerManager() {
		return new SRFreeWorkerManager(this);
	}

}
