package wb.receipts.workers;

import co.smartreceipts.android.purchases.PurchaseableSubscriptions;
import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.SubscriptionEventsListener;
import co.smartreceipts.android.purchases.SubscriptionWallet;
import co.smartreceipts.android.workers.AdManager;
import co.smartreceipts.android.workers.WorkerManager;
import wb.receipts.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.lang.ref.WeakReference;
import java.util.logging.Handler;

public class SRFreeAdManager extends AdManager implements SubscriptionEventsListener {

    //Preference Identifiers - SubClasses Only
    private static final String AD_PREFERENECES = SharedPreferenceDefinitions.Subclass_Preferences.toString();
    private static final String HIDE_AD = "pref1";

    private WeakReference<AdView> mAdViewReference;

    public SRFreeAdManager(@NonNull WorkerManager manager, @NonNull SubscriptionWallet subscriptionWallet) {
        super(manager, subscriptionWallet);
    }

    public synchronized void onActivityCreated(@NonNull Activity activity) {
        final AdView adView = (AdView) activity.findViewById(R.id.adView);
        mAdViewReference = new WeakReference<>(adView);
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.loadAd(getAdRequest());
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // If we fail to load the ad, just hide it
                        adView.setVisibility(View.GONE);
                    }
                });
            } else {
                adView.setVisibility(View.GONE);
            }
        }
    }

    public synchronized void onResume() {
        final AdView adView = mAdViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.resume();
            } else {
                adView.setVisibility(View.GONE);
            }
        }
    }

    public synchronized void onPause() {
        final AdView adView = mAdViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.pause();
            } else {
                adView.setVisibility(View.GONE);
            }
        }
    }

    public synchronized void onDestroy() {
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

    @Override
    public synchronized void onSubscriptionsAvailable(@NonNull PurchaseableSubscriptions purchaseableSubscriptions, @NonNull SubscriptionWallet subscriptionWallet) {
        // Intentional Stub. Handled with parent activity
    }

    @Override
    public synchronized void onSubscriptionsUnavailable() {
        // Intentional Stub. Handled with parent activity
    }

    @Override
    public synchronized void onPurchaseIntentAvailable(@NonNull Subscription subscription, @NonNull PendingIntent pendingIntent, @NonNull String key) {
        // Intentional Stub. Handled with parent activity
    }

    @Override
    public synchronized void onPurchaseIntentUnavailable(@NonNull Subscription subscription) {
        // Intentional Stub. Handled with parent activity
    }

    @Override
    public synchronized void onPurchaseSuccess(@NonNull Subscription subscription, @NonNull SubscriptionWallet updatedSubscriptionWallet) {
        if (Subscription.SmartReceiptsPro == subscription) {
            setSubscriptionWallet(updatedSubscriptionWallet);
            final AdView adView = mAdViewReference.get();
            if (adView != null) {
                adView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (shouldShowAds(adView)) {
                            adView.setVisibility(View.VISIBLE);
                        } else {
                            adView.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    }

    @Override
    public synchronized void onPurchaseFailed() {
        // Intentional Stub. Handled with parent activity
    }
}
