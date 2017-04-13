package co.smartreceipts.android.identity.widget;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.login.UserCredentialsPayload;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class LoginInteractor {

    private final IdentityManager identityManager;
    private final NavigationHandler navigationHandler;
    private final Analytics analytics;

    @Inject
    public LoginInteractor(IdentityManager identityManager, LoginFragment loginFragment, Analytics analytics) {
        this(identityManager, new NavigationHandler(loginFragment), analytics);
    }

    @VisibleForTesting
    LoginInteractor(@NonNull IdentityManager identityManager, @NonNull NavigationHandler navigationHandler,
                    @NonNull Analytics analytics) {
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
        this.analytics = Preconditions.checkNotNull(analytics);
    }

    @NonNull
    public Observable<LoginResponse> loginOrSignUp(@NonNull UserCredentialsPayload userCredentialsPayload) {
        Logger.info(this, "Initiating user login (or sign up)");
        return this.identityManager.logInOrSignUp(userCredentialsPayload)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        analytics.record(new ErrorEvent(throwable));
                    }
                });
    }

    public void onLoginResultsConsumed(@NonNull UserCredentialsPayload userCredentialsPayload) {
        this.identityManager.markLoginComplete(userCredentialsPayload);
    }

    public boolean navigateBack() {
        return this.navigationHandler.navigateBack();
    }

}
