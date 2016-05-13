package wb.receipts.workers;

import co.smartreceipts.android.purchases.PurchaseableSubscriptions;
import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.SubscriptionEventsListener;
import co.smartreceipts.android.purchases.SubscriptionManager;
import co.smartreceipts.android.purchases.SubscriptionWallet;
import co.smartreceipts.android.workers.AdManager;
import co.smartreceipts.android.workers.WorkerManager;
import wb.receipts.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.lang.ref.WeakReference;

public class SRFreeAdManager extends AdManager implements SubscriptionEventsListener {

    private static final String TAG = SRFreeAdManager.class.getSimpleName();

    //Preference Identifiers - SubClasses Only
    private static final String AD_PREFERENECES = SharedPreferenceDefinitions.Subclass_Preferences.toString();
    private static final String SHOW_AD = "pref1";

    private WeakReference<AdView> mAdViewReference;

    public SRFreeAdManager(@NonNull WorkerManager manager) {
        super(manager);
    }

    public synchronized void onActivityCreated(@NonNull Activity activity, @Nullable SubscriptionManager subscriptionManager) {
        super.onActivityCreated(activity, subscriptionManager);
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
        if (getSubscriptionManager() != null) {
            getSubscriptionManager().addEventListener(this);
        }
    }

    public synchronized void onResume() {
        super.onResume();
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
        super.onPause();
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
        if (getSubscriptionManager() != null) {
            getSubscriptionManager().removeEventListener(this);
        }
        super.onDestroy();
    }

    private boolean shouldShowAds(@NonNull AdView adView) {
        final boolean hasProSubscription = getSubscriptionManager() != null && getSubscriptionManager().getSubscriptionCache().getSubscriptionWallet().hasSubscription(Subscription.SmartReceiptsPlus);
        final boolean areAdsEnabledLocally = adView.getContext().getSharedPreferences(AD_PREFERENECES, 0).getBoolean(SHOW_AD, true);
        return areAdsEnabledLocally && !hasProSubscription;
    }

    private static AdRequest getAdRequest() {
        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("BFB48A3556EED9C87CB3AD907780D610")
                .build();
    }

    @Override
    public synchronized void onSubscriptionsAvailable(@NonNull PurchaseableSubscriptions purchaseableSubscriptions, @NonNull SubscriptionWallet subscriptionWallet) {
        // Refresh our subscriptions now
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
        Log.i(TAG, "Received purchase success in our ad manager for: " + subscription);
        if (Subscription.SmartReceiptsPlus == subscription) {
            final AdView adView = mAdViewReference.get();
            if (adView != null) {
                adView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (shouldShowAds(adView)) {
                            Log.w(TAG, "Showing the original ad following a purchase");
                            adView.setVisibility(View.VISIBLE);
                        } else {
                            Log.i(TAG, "Hiding the original ad following a purchase");
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
