package co.smartreceipts.android.ocr.widget.configuration;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.activities.DaggerFragmentNavigationHandler;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.utils.log.Logger;

@FragmentScope
public class OcrConfigurationRouter {

    private final NavigationHandler navigationHandler;
    private final IdentityManager identityManager;

    @Inject
    public OcrConfigurationRouter(@NonNull DaggerFragmentNavigationHandler<OcrConfigurationFragment> navigationHandler, @NonNull IdentityManager identityManager) {
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
        this.identityManager = Preconditions.checkNotNull(identityManager);
    }

    public void navigateToProperLocation(@Nullable Bundle savedInstanceState) {
        if (!identityManager.isLoggedIn()) {
            if (savedInstanceState == null) {
                Logger.info(this, "User not logged in. Sending to the log in screen");
                navigationHandler.navigateToLoginScreen();
            } else {
                Logger.info(this, "Returning to this fragment after not signing in. Navigating back rather than looping back to the log in screen");
                this.navigationHandler.navigateBack();
            }
        } else {
            Logger.debug(this, "User is already logged in. Doing nothing and remaining on this screen");
        }
    }

    public boolean navigateBack() {
        return this.navigationHandler.navigateBack();
    }
}
