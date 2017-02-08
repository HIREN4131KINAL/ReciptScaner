package co.smartreceipts.android.identity.widget;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Preconditions;
import com.jakewharton.rxbinding.widget.RxTextView;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.identity.apis.login.LoginParams;
import co.smartreceipts.android.identity.apis.login.SmartReceiptsUserLogin;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class LoginPresenter {

    private static final int MINIMUM_EMAIL_LENGTH = 2;
    private static final int MINIMUM_PASSWORD_LENGTH = 2;

    private final EditText emailInput;
    private final EditText passwordInput;
    private final Button loginButton;

    private final PublishSubject<LoginParams> loginParamsSubject = PublishSubject.create();
    private Subscription subscription;

    private TextView debug1;
    private TextView debug2;
    private TextView debug3;

    public LoginPresenter(@NonNull View view) {
        emailInput = Preconditions.checkNotNull((EditText) view.findViewById(R.id.login_email));
        passwordInput = Preconditions.checkNotNull((EditText) view.findViewById(R.id.login_password));
        loginButton = Preconditions.checkNotNull((Button) view.findViewById(R.id.login_button));

        // TODO: Delete
        debug1 = (TextView) view.findViewById(R.id.login_debug_is_logged_in);
        debug2 = (TextView) view.findViewById(R.id.login_debug_email);
        debug3 = (TextView) view.findViewById(R.id.login_debug_token);
        showDebugText();
    }

    public void onResume() {
        subscription = new CompositeSubscription();
        subscription = Observable.combineLatest(
                RxTextView.textChanges(emailInput),
                RxTextView.textChanges(passwordInput),
                new Func2<CharSequence, CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence email, CharSequence password) {
                        return email != null && email.length() >= MINIMUM_EMAIL_LENGTH && password != null && password.length() >= MINIMUM_PASSWORD_LENGTH;
                    }
                }).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean enableLoginButton) {
                loginButton.setEnabled(enableLoginButton);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailInput.getText().toString();
                final String password = passwordInput.getText().toString();
                loginButton.setEnabled(false);
                loginParamsSubject.onNext(new SmartReceiptsUserLogin(email, password));
            }
        });
    }

    public void onPause() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @NonNull
    public Observable<LoginParams> getLoginParamsStream() {
        return loginParamsSubject.asObservable();
    }

    public void presentLoginSuccess() {
        Toast.makeText(loginButton.getContext(), "Login Success", Toast.LENGTH_SHORT).show();
        loginButton.setEnabled(true);
        showDebugText();
    }

    public void presentLoginFailure() {
        Toast.makeText(loginButton.getContext(), "Login Failure", Toast.LENGTH_SHORT).show();
        loginButton.setEnabled(true);
    }

    @Deprecated
    private void showDebugText() {
        final SmartReceiptsApplication application = (SmartReceiptsApplication) this.debug1.getContext().getApplicationContext();
        debug1.setText("Is logged in == " + application.getIdentityManager().isLoggedIn());
        debug2.setText("Email: " + application.getIdentityManager().getEmail());
        debug3.setText("Token: " + application.getIdentityManager().getToken());
    }
}
