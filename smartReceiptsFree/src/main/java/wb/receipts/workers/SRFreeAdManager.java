package wb.receipts.workers;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.purchases.PurchaseSource;
import co.smartreceipts.android.purchases.PurchaseableSubscriptions;
import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.SubscriptionEventsListener;
import co.smartreceipts.android.purchases.SubscriptionManager;
import co.smartreceipts.android.purchases.SubscriptionWallet;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.AdManager;
import co.smartreceipts.android.workers.WorkerManager;
import wb.receipts.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import co.smartreceipts.android.persistence.SharedPreferenceDefinitions;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.lang.ref.WeakReference;
import java.util.Random;

public class SRFreeAdManager extends AdManager implements SubscriptionEventsListener {

    private static final int RANDOM_MAX = 100;
    private static final int UPSELL_FREQUENCY = 1; // Out of 100

    //Preference Identifiers - SubClasses Only
    private static final String AD_PREFERENECES = SharedPreferenceDefinitions.Subclass_Preferences.toString();
    private static final String SHOW_AD = "pref1";

    private WeakReference<AdView> mAdViewReference;
    private WeakReference<Button> mUpsellReference;

    public SRFreeAdManager(@NonNull WorkerManager manager) {
        super(manager);
    }

    public synchronized void onActivityCreated(@NonNull final Activity activity, @Nullable SubscriptionManager subscriptionManager) {
        super.onActivityCreated(activity, subscriptionManager);
        final AdView adView = (AdView) activity.findViewById(R.id.adView);
        final Button upsell = (Button) activity.findViewById(R.id.adView_upsell);
        mAdViewReference = new WeakReference<>(adView);
        mUpsellReference = new WeakReference<>(upsell);

        final AnalyticsManager analyticsManager = ((SmartReceiptsApplication)activity.getApplication()).getAnalyticsManager();
        if (adView != null) {
            if (shouldShowAds(adView)) {
                if (showUpsell()) {
                    analyticsManager.record(Events.Purchases.AdUpsellShown);
                    adView.setVisibility(View.GONE);
                    upsell.setVisibility(View.VISIBLE);
                } else {
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            // If we fail to load the ad, just hide it
                            analyticsManager.record(Events.Purchases.AdUpsellShownOnFailure);
                            adView.setVisibility(View.GONE);
                            upsell.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdLoaded() {
                            upsell.setVisibility(View.GONE);
                        }
                    });
                    loadAdDelayed(adView);
                }
            } else {
                hideAdAndUpsell();
            }
        }

        if (getSubscriptionManager() != null) {
            getSubscriptionManager().addEventListener(this);
        }

        upsell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getSubscriptionManager() != null) {
                    analyticsManager.record(Events.Purchases.AdUpsellTapped);
                    getSubscriptionManager().queryBuyIntent(Subscription.SmartReceiptsPlus, PurchaseSource.AdBanner);
                }
            }
        });
    }

    public synchronized void onResume() {
        super.onResume();
        final AdView adView = mAdViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.resume();
            } else {
                hideAdAndUpsell();
            }
        }
    }

    public synchronized void onPause() {
        final AdView adView = mAdViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.pause();
            } else {
                hideAdAndUpsell();
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
                hideAdAndUpsell();
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
    public synchronized void onPurchaseSuccess(@NonNull Subscription subscription, @NonNull PurchaseSource purchaseSource, @NonNull SubscriptionWallet updatedSubscriptionWallet) {
        Logger.info(this, "Received purchase success in our ad manager for: %s", subscription);
        if (Subscription.SmartReceiptsPlus == subscription) {
            final AdView adView = mAdViewReference.get();
            if (adView != null) {
                adView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (shouldShowAds(adView)) {
                            Logger.warn(this, "Showing the original ad following a purchase");
                            adView.setVisibility(View.VISIBLE);
                        } else {
                            Logger.info(this, "Hiding the original ad following a purchase");
                            adView.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }
    }

    @Override
    public synchronized void onPurchaseFailed(@NonNull PurchaseSource purchaseSource) {
        // Intentional Stub. Handled with parent activity
    }

    private void hideAdAndUpsell() {
        final AdView adView = mAdViewReference.get();
        final Button upsell = mUpsellReference.get();
        if (adView != null) {
            adView.setVisibility(View.GONE);
        }
        if (upsell != null) {
            upsell.setVisibility(View.GONE);
        }
    }

    /**
     * The {@link AdView#loadAd(AdRequest)} is really slow and cannot be moved off the main thread (ugh).
     * We use this method to slightly defer the ad loading process until the core UI of the app loads, so
     * users can see data immediately
     *
     * @param adView
     */
    private void loadAdDelayed(@NonNull final AdView adView) {
        adView.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    adView.loadAd(getAdRequest());
                } catch (Exception e) {
                    Logger.error(this, "Swallowing ad load exception... ", e);
                    // Swallowing all exception b/c I'm lazy and don't want to handle activity finishing states
                }
            }
        }, 50);
    }

    private boolean showUpsell() {
        final Random random = new Random(SystemClock.uptimeMillis());
        return UPSELL_FREQUENCY >= random.nextInt(RANDOM_MAX + 1);
    }
}
