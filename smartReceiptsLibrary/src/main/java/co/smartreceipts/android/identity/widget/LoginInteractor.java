package co.smartreceipts.android.identity.widget;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.login.LoginParams;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;

public class LoginInteractor {

    private static final String TAG = LoginInteractor.class.getName();

    private final IdentityManager identityManager;
    private final Analytics analytics;

    public LoginInteractor(@NonNull FragmentManager fragmentManager, @NonNull IdentityManager identityManager, @NonNull Analytics analytics) {
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.analytics = Preconditions.checkNotNull(analytics);
        Preconditions.checkNotNull(fragmentManager);
    }

    @NonNull
    public Observable<LoginResponse> login(@NonNull LoginParams loginParams) {
        Logger.info(this, "Initiating user login");
        return this.identityManager.logIn(loginParams);
    }

    public void onLoginResultsConsumed(@NonNull LoginParams loginParams) {
        this.identityManager.markLoginComplete(loginParams);
    }

}
