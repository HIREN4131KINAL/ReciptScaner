package wb.receipts;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.analytics.GoogleAnalytics;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.workers.WorkerManager;
import wb.receipts.workers.SRFreeWorkerManager;

public class SmartReceiptsFreeApplication extends SmartReceiptsApplication {

	@Override
	public void onCreate() {
		super.onCreate();
		BugSenseHandler.initAndStartSession(this, "01de172a");
        getAnalyticsManager().register(new co.smartreceipts.android.analytics.GoogleAnalytics(GoogleAnalytics.getInstance(getApplicationContext()).newTracker(R.xml.analytics)));
	}

	@Override
	protected WorkerManager instantiateWorkerManager() {
		return new SRFreeWorkerManager(this);
	}

}
