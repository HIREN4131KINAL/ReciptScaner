package wb.receipts.workers;

import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.SubscriptionWallet;
import co.smartreceipts.android.workers.AdManager;
import co.smartreceipts.android.workers.WorkerManager;
import wb.receipts.R;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.lang.ref.WeakReference;

public class SRFreeAdManager extends AdManager {

    //Preference Identifiers - SubClasses Only
    private static final String AD_PREFERENECES = SharedPreferenceDefinitions.Subclass_Preferences.toString();
    private static final String HIDE_AD = "pref1";

    private WeakReference<AdView> mAdViewReference;

    public SRFreeAdManager(@NonNull WorkerManager manager, @NonNull SubscriptionWallet subscriptionWallet) {
        super(manager, subscriptionWallet);
    }

    public void onViewCreated(@NonNull View rootView) {
        final AdView adView = (AdView) rootView.findViewById(R.id.adView);
        mAdViewReference = new WeakReference<>(adView);
        if (adView != null) {
            if (!rootView.getContext().getSharedPreferences(AD_PREFERENECES, 0).getBoolean(HIDE_AD, true) || getSubscriptionWallet().hasSubscription(Subscription.SmartReceiptsPro)) {
                adView.setVisibility(View.GONE);
            } else {
                adView.loadAd(getAdRequest());
            }
        }
    }

    public void onResume() {
        final AdView adView = mAdViewReference.get();
        if (adView != null) {
            adView.resume();
        }
    }

    public void onPause() {
        final AdView adView = mAdViewReference.get();
        if (adView != null) {
            adView.pause();
        }
    }

    public void onDestroy() {
        final AdView adView = mAdViewReference.get();
        if (adView != null) {
            adView.destroy();
        }
    }

    private static AdRequest getAdRequest() {
        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("18A4E00B14321F0C9A3EA60E38A32932")
                .build();
    }

}
