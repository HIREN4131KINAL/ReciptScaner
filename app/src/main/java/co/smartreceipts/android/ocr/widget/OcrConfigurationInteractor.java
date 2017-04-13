package co.smartreceipts.android.ocr.widget;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

@FragmentScope
public class OcrConfigurationInteractor {

    private final NavigationHandler navigationHandler;
    private final IdentityManager identityManager;
    private final OcrPurchaseTracker ocrPurchaseTracker;
    private final Analytics analytics;

    @Inject
    public OcrConfigurationInteractor(OcrConfigurationFragment fragment, IdentityManager identityManager, OcrPurchaseTracker ocrPurchaseTracker,
                                      Analytics analytics) {
        this(new NavigationHandler(fragment), identityManager, ocrPurchaseTracker, analytics);
    }

    @VisibleForTesting
    OcrConfigurationInteractor(@NonNull NavigationHandler navigationHandler, @NonNull IdentityManager identityManager,
                               @NonNull OcrPurchaseTracker ocrPurchaseTracker, @NonNull Analytics analytics) {
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.ocrPurchaseTracker = Preconditions.checkNotNull(ocrPurchaseTracker);
        this.analytics = Preconditions.checkNotNull(analytics);
    }

    @Nullable
    public EmailAddress getEmail() {
        return identityManager.getEmail();
    }

    @NonNull
    public Observable<Integer> getRemainingScansStream() {
        return ocrPurchaseTracker.getRemainingScansStream()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void routeToProperLocation(@Nullable Bundle savedInstanceState) {
        if (!identityManager.isLoggedIn()) {
            if (savedInstanceState == null) {
                Logger.info(this, "User not logged in. Sending to the log in screen");
            } else {
                Logger.info(this, "Returning to this fragment after not signing in. Navigating back rather than looping back to the log in screen");
                navigateBack();
            }
        } else {
            Logger.debug(this, "User is already logged in. Doing nothing and remaining on this screen");
        }
    }

    public boolean navigateBack() {
        return this.navigationHandler.navigateBack();
    }

}
