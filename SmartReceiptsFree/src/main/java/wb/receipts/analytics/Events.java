package wb.receipts.analytics;

import wb.receipts.R;
import wb.receipts.SmartReceiptsFreeApplication;
import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class Events {

	public static final void sendEvent(Activity activity, String category, String action) {
		if (activity != null) {
			Application application = activity.getApplication();
			if (application instanceof SmartReceiptsFreeApplication) {
				Tracker tracker = ((SmartReceiptsFreeApplication) application).getTracker(R.xml.analytics);
				if (tracker != null) {
					tracker.send(new HitBuilders.EventBuilder(category, action).build());
				}
			}
		}
	}

	public static final void sendEvent(Fragment fragment, String category, String action) {
		if (fragment != null) {
			Events.sendEvent(fragment.getActivity(), action, category);
		}
	}
}
