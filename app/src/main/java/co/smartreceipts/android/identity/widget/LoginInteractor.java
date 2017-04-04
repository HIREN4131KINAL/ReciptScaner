package co.smartreceipts.android.identity.widget;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.login.LoginParams;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import co.smartreceipts.android.identity.apis.logout.LogoutResponse;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

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
    public Observable<EmailAddress> isLoggedIn() {
        return Observable.create(new Observable.OnSubscribe<EmailAddress>() {
            @Override
            public void call(Subscriber<? super EmailAddress> subscriber) {
                if (identityManager.isLoggedIn()) {
                    subscriber.onNext(identityManager.getEmail());
                } else {
                    subscriber.onNext(null);
                }
                subscriber.onCompleted();
            }
        });
    }

    @NonNull
    public Observable<LoginResponse> login(@NonNull LoginParams loginParams) {
        Logger.info(this, "Initiating user login");
        return this.identityManager.logIn(loginParams)
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void onLoginResultsConsumed(@NonNull LoginParams loginParams) {
        this.identityManager.markLoginComplete(loginParams);
    }

    @NonNull
    public Observable<LogoutResponse> logOut() {
        return this.identityManager.logOut()
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void onLogoutResultsConsumed() {
        this.identityManager.markLogoutComplete();
    }

}
