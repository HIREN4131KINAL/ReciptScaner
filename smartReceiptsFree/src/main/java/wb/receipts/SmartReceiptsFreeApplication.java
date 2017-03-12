package wb.receipts;

import com.bugsense.trace.BugSenseHandler;
import com.squareup.leakcanary.LeakCanary;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.GoogleAnalytics;
import co.smartreceipts.android.utils.log.Logger;

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
        getAnalyticsManager().register(new GoogleAnalytics(this));
	}
}
