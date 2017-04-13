package co.smartreceipts.android.identity.widget.presenters;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.identity.apis.login.LoginParams;
import co.smartreceipts.android.identity.store.EmailAddress;
import rx.Observable;

public class MyAccountPresenter {

    private final LoginPresenter loginPresenter;
    private final LogoutPresenter logoutPresenter;

    public MyAccountPresenter(@NonNull View view) {
        this(new LoginPresenter(view), new LogoutPresenter(view));
    }

    public MyAccountPresenter(@NonNull LoginPresenter loginPresenter, @NonNull LogoutPresenter logoutPresenter) {
        this.loginPresenter = Preconditions.checkNotNull(loginPresenter);
        this.logoutPresenter = Preconditions.checkNotNull(logoutPresenter);
    }

    public void onResume() {
        loginPresenter.onResume();
        logoutPresenter.onResume();
    }

    public void onPause() {
        loginPresenter.onPause();
        logoutPresenter.onPause();
    }

    @NonNull
    public Observable<LoginParams> getLoginParamsStream() {
        return loginPresenter.getLoginParamsStream();
    }

    @NonNull
    public Observable<Void> getLogoutStream() {
        return logoutPresenter.getLogoutStream();
    }

    public void presentNoUserSignedIn() {
        loginPresenter.present();
        logoutPresenter.hide();
    }

    public void presentExistingUserSignedIn(@NonNull EmailAddress emailAddress) {
        logoutPresenter.present(emailAddress);
        loginPresenter.hide();
    }

    public void presentLoginSuccess() {
        loginPresenter.presentLoginSuccess();
    }

    public void presentLoginFailure() {
        loginPresenter.presentLoginFailure();
    }

}
