package wb.receipts.ad;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.res.Resources;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.NativeExpressAdView;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.ad.AdManager;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.SubscriptionEventsListener;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.utils.log.Logger;
import wb.receipts.R;

@ApplicationScope
public class FreeAdManager implements AdManager, SubscriptionEventsListener {

    private static final int RANDOM_MAX = 100;
    private static final int UPSELL_FREQUENCY = 1; // Out of 100

    //Preference Identifiers - SubClasses Only
    private static final String AD_PREFERENECES = SharedPreferenceDefinitions.Subclass_Preferences.toString();
    private static final String SHOW_AD = "pref1";

    private WeakReference<NativeExpressAdView> mAdViewReference;
    private WeakReference<Button> mUpsellReference;

    private PurchaseManager purchaseManager;

    @Inject
    FreeAdManager() {
    }

    public synchronized void onActivityCreated(@NonNull final Activity activity, @Nullable PurchaseManager purchaseManager) {
        this.purchaseManager = purchaseManager;

        final ViewGroup container = (ViewGroup) activity.findViewById(R.id.adView_container);
        final Button upsell = (Button) activity.findViewById(R.id.adView_upsell);

        final NativeExpressAdView adView = new NativeExpressAdView(activity);
        adView.setAdSize(calculateAdSize());
        adView.setAdUnitId(activity.getResources().getString(R.string.adUnitId));
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        adView.setLayoutParams(params);
        container.addView(adView);

        mAdViewReference = new WeakReference<>(adView);
        mUpsellReference = new WeakReference<>(upsell);

        final AnalyticsManager analyticsManager = ((SmartReceiptsApplication) activity.getApplication()).getAnalyticsManager();
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

        if (this.purchaseManager != null) {
            this.purchaseManager.addEventListener(this);
        }

        upsell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (FreeAdManager.this.purchaseManager != null) {
                    analyticsManager.record(Events.Purchases.AdUpsellTapped);
                    FreeAdManager.this.purchaseManager.queryBuyIntent(InAppPurchase.SmartReceiptsPlus, PurchaseSource.AdBanner);
                }
            }
        });
    }

    @NonNull
    private AdSize calculateAdSize() {
        float density = Resources.getSystem().getDisplayMetrics().density;
        int heightPixels = Resources.getSystem().getDisplayMetrics().heightPixels;
        int heightDps = (int) (heightPixels / density);

        int widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
        int widthDps = (int) (widthPixels / density);

        // Use FULL_WIDTH unless the screen width is greater than the max width
        int adWidth = (widthDps < 1200) ? AdSize.FULL_WIDTH : 1200;

        if (heightDps < 700) {
            return new AdSize(adWidth, 80);
        } else if (heightDps < 1000) {
            return new AdSize(adWidth, 100);
        } else {
            return new AdSize(adWidth, 130);
        }
    }

    public synchronized void onResume() {
        final NativeExpressAdView adView = mAdViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.resume();
            } else {
                hideAdAndUpsell();
            }
        }
    }

    public synchronized void onPause() {
        final NativeExpressAdView adView = mAdViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.pause();
            } else {
                hideAdAndUpsell();
            }
        }
    }

    public synchronized void onDestroy() {
        final NativeExpressAdView adView = mAdViewReference.get();
        if (adView != null) {
            if (shouldShowAds(adView)) {
                adView.destroy();
            } else {
                hideAdAndUpsell();
            }
        }
        if (purchaseManager != null) {
            purchaseManager.removeEventListener(this);
        }
    }

    private boolean shouldShowAds(@NonNull NativeExpressAdView adView) {
        final boolean hasProSubscription = purchaseManager != null
                && purchaseManager.getPurchaseWallet().hasActivePurchase(InAppPurchase.SmartReceiptsPlus);
        final boolean areAdsEnabledLocally = adView.getContext().getSharedPreferences(AD_PREFERENECES, 0).getBoolean(SHOW_AD, true);
        return areAdsEnabledLocally && !hasProSubscription;
    }

    private static AdRequest getAdRequest() {
        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("BFB48A3556EED9C87CB3AD907780D610")
                .addTestDevice("E03AEBCB2894909B8E4EC87C0368C242")
                .build();
    }

    @Override
    public synchronized void onPurchasesAvailable(@NonNull List<InAppPurchase> availablePurchases) {
        // Refresh our subscriptions now
        final NativeExpressAdView adView = mAdViewReference.get();
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
    public synchronized void onPurchasesUnavailable() {
        // Intentional Stub. Handled with parent activity
    }

    @Override
    public synchronized void onPurchaseIntentAvailable(@NonNull InAppPurchase inAppPurchase, @NonNull PendingIntent pendingIntent, @NonNull String key) {
        // Intentional Stub. Handled with parent activity
    }

    @Override
    public synchronized void onPurchaseIntentUnavailable(@NonNull InAppPurchase inAppPurchase) {
        // Intentional Stub. Handled with parent activity
    }

    @Override
    public synchronized void onPurchaseSuccess(@NonNull InAppPurchase inAppPurchase, @NonNull PurchaseSource purchaseSource, @NonNull PurchaseWallet updatedPurchaseWallet) {
        Logger.info(this, "Received purchase success in our ad manager for: {}", inAppPurchase);
        if (InAppPurchase.SmartReceiptsPlus == inAppPurchase) {
            final NativeExpressAdView adView = mAdViewReference.get();
            if (adView != null) {
                adView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (shouldShowAds(adView)) {
                            Logger.warn(this, "Showing the original ad following a purchase");
                            adView.setVisibility(View.VISIBLE);
                        } else {
                            Logger.info(this, "Hiding the original ad following a purchase");
                            hideAdAndUpsell();
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
        final NativeExpressAdView adView = mAdViewReference.get();
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
    private void loadAdDelayed(@NonNull final NativeExpressAdView adView) {
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
