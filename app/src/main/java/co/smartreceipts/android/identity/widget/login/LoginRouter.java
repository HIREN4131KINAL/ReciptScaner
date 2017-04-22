package co.smartreceipts.android.identity.widget.login;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.activities.DaggerFragmentNavigationHandler;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.di.scopes.FragmentScope;

@FragmentScope
public class LoginRouter {

    private final NavigationHandler navigationHandler;

    @Inject
    public LoginRouter(@NonNull DaggerFragmentNavigationHandler<LoginFragment> navigationHandler) {
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
    }

    public boolean navigateBack() {
        return this.navigationHandler.navigateBack();
    }
}
