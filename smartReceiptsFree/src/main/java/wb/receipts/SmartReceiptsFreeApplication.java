package wb.receipts;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.squareup.leakcanary.LeakCanary;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.WorkerManager;
import wb.receipts.workers.SRFreeWorkerManager;

public class SmartReceiptsFreeApplication extends SmartReceiptsApplication {

	@Override
	public void onCreate() {
		super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            Logger.debug(this, "Ignoring this process as it's the LeakCanary analyzer one...");
            return;
        } else {
            LeakCanary.install(this);
        }
		BugSenseHandler.initAndStartSession(this, "01de172a");
        getAnalyticsManager().register(new co.smartreceipts.android.analytics.GoogleAnalytics(GoogleAnalytics.getInstance(getApplicationContext()).newTracker(R.xml.analytics)));
		getAnalyticsManager().register(new co.smartreceipts.android.analytics.FirebaseAnalytics(getApplicationContext()));
	}

	@Override
	protected WorkerManager instantiateWorkerManager() {
		return new SRFreeWorkerManager(this);
	}

}
