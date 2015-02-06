package wb.receipts.ads;

import wb.receipts.R;
import android.view.View;
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Ads {

	//Preference Identifiers - SubClasses Only
    private static final String AD_PREFERENECES = SharedPreferenceDefinitions.Subclass_Preferences.toString();
    private static final String HIDE_AD = "pref1";

	private Ads() { }

	public static AdRequest     getAdRequest() {
		return new AdRequest.Builder()
					        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
					        .addTestDevice("18A4E00B14321F0C9A3EA60E38A32932")
					        .build();
	}

	public static AdView onCreateView(View rootView) {
		AdView adView = (AdView) rootView.findViewById(R.id.adView);
		if (!rootView.getContext().getSharedPreferences(AD_PREFERENECES, 0).getBoolean(HIDE_AD, true)) {
			adView.setVisibility(View.GONE);
			adView = null;
        }
        else if (adView != null) {
        	adView.loadAd(Ads.getAdRequest());
        }
		return adView;
	}

	public static void onResume(AdView adView) {
		if (adView != null) {
			adView.resume();
		}
	}

	public static void onPause(AdView adView) {
		if (adView != null) {
			adView.pause();
		}
	}

	public static void onDestroy(AdView adView) {
		if (adView != null) {
			adView.destroy();
		}
	}

}
