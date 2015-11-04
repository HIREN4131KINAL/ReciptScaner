package wb.receipts.workers;

import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.SubscriptionWallet;
import co.smartreceipts.android.workers.AdManager;
import co.smartreceipts.android.workers.WorkerManager;
import wb.receipts.R;

import android.app.Activity;
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

    public void onActivityCreated(@NonNull Activity activity) {
        final AdView adView = (AdView) activity.findViewById(R.id.adView);
        mAdViewReference = new WeakReference<>(adView);
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.loadAd(getAdRequest());
            } else {
                adView.setVisibility(View.GONE);
            }
        }
    }

    public void onResume() {
        final AdView adView = mAdViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.resume();
            } else {
                adView.setVisibility(View.GONE);
            }
        }
    }

    public void onPause() {
        final AdView adView = mAdViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.pause();
            } else {
                adView.setVisibility(View.GONE);
            }
        }
    }

    public void onDestroy() {
        final AdView adView = mAdViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.destroy();
            } else {
                adView.setVisibility(View.GONE);
            }
        }
    }

    private boolean shouldShowAds(@NonNull AdView adView) {
        return !adView.getContext().getSharedPreferences(AD_PREFERENECES, 0).getBoolean(HIDE_AD, false) && !getSubscriptionWallet().hasSubscription(Subscription.SmartReceiptsPro);
    }

    private static AdRequest getAdRequest() {
        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("BFB48A3556EED9C87CB3AD907780D610")
                .build();
    }

}
