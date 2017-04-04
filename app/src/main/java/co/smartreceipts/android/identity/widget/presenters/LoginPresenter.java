package co.smartreceipts.android.identity.widget.presenters;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Preconditions;
import com.jakewharton.rxbinding.widget.RxTextView;

import co.smartreceipts.android.R;
import co.smartreceipts.android.identity.apis.login.LoginParams;
import co.smartreceipts.android.identity.apis.login.SmartReceiptsUserLogin;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class LoginPresenter {

    private static final int MINIMUM_EMAIL_LENGTH = 6;
    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    // Nobody signed in stuff
    private final View noActiveUserLayout;
    private final TextView loginFieldsHintMessage;
    private final EditText emailInput;
    private final EditText passwordInput;
    private final Button loginButton;

    private final PublishSubject<LoginParams> loginParamsSubject = PublishSubject.create();
    private CompositeSubscription compositeSubscription;

    public LoginPresenter(@NonNull View view) {
        noActiveUserLayout = Preconditions.checkNotNull(view.findViewById(R.id.no_active_user_layout));
        loginFieldsHintMessage = Preconditions.checkNotNull((TextView) view.findViewById(R.id.login_fields_hint));
        emailInput = Preconditions.checkNotNull((EditText) view.findViewById(R.id.login_field_email));
        passwordInput = Preconditions.checkNotNull((EditText) view.findViewById(R.id.login_field_password));
        loginButton = Preconditions.checkNotNull((Button) view.findViewById(R.id.login_button));
    }

    public void onResume() {
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(Observable.combineLatest(
                simpleEmailFieldValidator(),
                simplePasswordFieldValidator(),
                new Func2<Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean isEmailValid, Boolean isPasswordValid) {
                        return isEmailValid && isPasswordValid;
                    }
                }).subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean enableLoginButton) {
                        if (enableLoginButton) {
                            loginFieldsHintMessage.setText(R.string.login_fields_hint_valid);
                        }
                        loginButton.setEnabled(enableLoginButton);
                    }
                })
        );

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
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }
    }

    @NonNull
    public Observable<LoginParams> getLoginParamsStream() {
        return loginParamsSubject.asObservable();
    }

    public void present() {
        noActiveUserLayout.setVisibility(View.VISIBLE);
        emailInput.requestFocus();
        SoftKeyboardManager.showKeyboard(emailInput);
    }

    public void hide() {
        noActiveUserLayout.setVisibility(View.GONE);
    }

    public void presentLoginSuccess() {
        Toast.makeText(loginButton.getContext(), "Login Success", Toast.LENGTH_SHORT).show();
        loginButton.setEnabled(true);
    }

    public void presentLoginFailure() {
        Toast.makeText(loginButton.getContext(), "Login Failure", Toast.LENGTH_SHORT).show();
        loginButton.setEnabled(true);
    }

    @NonNull
    private Observable<Boolean> simpleEmailFieldValidator() {
        return RxTextView.textChanges(emailInput)
                .map(new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence emailCharSequence) {
                        if (emailCharSequence != null && emailCharSequence.length() >= MINIMUM_EMAIL_LENGTH) {
                            final String email = emailCharSequence.toString();
                            return email.contains("@") && email.contains(".");
                        } else {
                            return false;
                        }
                    }
                })
                .doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isEmailValid) {
                        if (isEmailValid) {
                            loginFieldsHintMessage.setText("");
                            validInputHighlight(emailInput);
                        } else {
                            loginFieldsHintMessage.setText(R.string.login_fields_hint_email);
                            errorInputHighlight(emailInput);
                        }
                    }
                });
    }

    @NonNull
    private Observable<Boolean> simplePasswordFieldValidator() {
        return RxTextView.textChanges(passwordInput)
                .map(new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence password) {
                        return password != null && password.length() >= MINIMUM_PASSWORD_LENGTH;
                    }
                })
                .doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean isPasswordValid) {
                        if (isPasswordValid) {
                            loginFieldsHintMessage.setText("");
                            validInputHighlight(passwordInput);
                        } else {
                            loginFieldsHintMessage.setText(R.string.login_fields_hint_password);
                            errorInputHighlight(passwordInput);
                        }
                    }
                });
    }

    private void validInputHighlight(@NonNull EditText editText) {
        final int color = ResourcesCompat.getColor(editText.getResources(), R.color.smart_receipts_colorSuccess, editText.getContext().getTheme());
        editText.getBackground().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    private void errorInputHighlight(@NonNull EditText editText) {
        final int color = ResourcesCompat.getColor(editText.getResources(), R.color.smart_receipts_colorAccent, editText.getContext().getTheme());
        editText.getBackground().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }
}
