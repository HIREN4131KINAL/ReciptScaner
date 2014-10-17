package wb.receipts.analytics;

import wb.receipts.R;
import wb.receipts.SmartReceiptsFreeApplication;
import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class Screens {

	private Screens() {

	}

	public static final void sendScreen(Activity activity) {
		if (activity != null) {
			Application application = activity.getApplication();
			if (application instanceof SmartReceiptsFreeApplication) {
				Tracker tracker = ((SmartReceiptsFreeApplication) application).getTracker(R.xml.analytics);
				if (tracker != null) {
					tracker.setScreenName("/" + activity.getClass().getSimpleName());
					tracker.send(new HitBuilders.AppViewBuilder().build());
				}
			}
		}
	}

	public static final void sendScreen(Fragment fragment) {
		if (fragment != null && fragment.getActivity() != null) {
			Application application = fragment.getActivity().getApplication();
			if (application instanceof SmartReceiptsFreeApplication) {
				Tracker tracker = ((SmartReceiptsFreeApplication) application).getTracker(R.xml.analytics);
				if (tracker != null) {
					tracker.setScreenName("/" + fragment.getActivity().getClass().getSimpleName() + "/" + fragment.getClass().getSimpleName());
					tracker.send(new HitBuilders.AppViewBuilder().build());
				}
			}
		}
	}

}
