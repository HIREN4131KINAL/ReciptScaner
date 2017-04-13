package co.smartreceipts.android.identity.widget.presenters;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.apis.SmartReceiptsApiErrorResponse;
import co.smartreceipts.android.apis.SmartReceiptsApiException;
import co.smartreceipts.android.identity.apis.login.UserCredentialsPayload;
import co.smartreceipts.android.identity.apis.login.SmartReceiptsUserLogin;
import co.smartreceipts.android.identity.apis.login.SmartReceiptsUserSignUp;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import co.smartreceipts.android.utils.butterknife.ButterKnifeActions;
import co.smartreceipts.android.widget.Presenter;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class LoginPresenter implements Presenter {

    private static final int INVALID_CREDENTIALS_CODE = 401;

    private static final int MINIMUM_EMAIL_LENGTH = 6;
    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    private final Unbinder unbinder;
    private final PublishSubject<UserCredentialsPayload> loginParamsSubject = PublishSubject.create();

    @BindView(R.id.login_fields_hint) TextView loginFieldsHintMessage;
    @BindView(R.id.login_field_email) EditText emailInput;
    @BindView(R.id.login_field_password) EditText passwordInput;
    @BindViews({R.id.login_button, R.id.sign_up_button}) List<Button> buttons;

    private CompositeSubscription compositeSubscription;

    public LoginPresenter(@NonNull View view) {
        this.unbinder = ButterKnife.bind(this, view);

        emailInput.requestFocus();
        SoftKeyboardManager.showKeyboard(emailInput);
    }

    @Override
    public void onResume() {
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(Observable.combineLatest(
                simplePasswordFieldValidator(),
                simpleEmailFieldValidator(),
                new Func2<Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean isPasswordValid, Boolean isEmailValid) {
                        return isEmailValid && isPasswordValid;
                    }
                }).subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean enableLoginButton) {
                        if (enableLoginButton) {
                            loginFieldsHintMessage.setText(R.string.login_fields_hint_valid);
                        }
                        ButterKnife.apply(buttons, ButterKnifeActions.setEnabled(enableLoginButton));
                    }
                })
        );
    }

    @Override
    public void onPause() {
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }
    }

    @Override
    public void onDestroyView() {
        SoftKeyboardManager.hideKeyboard(emailInput);
        unbinder.unbind();
    }

    @NonNull
    public Observable<UserCredentialsPayload> getLoginOrSignUpParamsStream() {
        return loginParamsSubject.asObservable();
    }

    public void presentLoginSuccess() {
        Toast.makeText(emailInput.getContext(), R.string.login_success_toast, Toast.LENGTH_SHORT).show();
        ButterKnife.apply(buttons, ButterKnifeActions.setEnabled(true));
    }

    public void presentLoginFailure(@NonNull Throwable throwable) {
        String errorMessage = emailInput.getContext().getString(R.string.login_failure_toast);
        if (throwable instanceof SmartReceiptsApiException) {
            final SmartReceiptsApiException smartReceiptsApiException = (SmartReceiptsApiException) throwable;
            final SmartReceiptsApiErrorResponse errorResponse = smartReceiptsApiException.getErrorResponse();
            if (errorResponse != null && errorResponse.getErrors() != null && !errorResponse.getErrors().isEmpty()) {
                errorMessage = errorResponse.getErrors().get(0);
            }
            if (smartReceiptsApiException.getResponse() != null && smartReceiptsApiException.getResponse().code() == INVALID_CREDENTIALS_CODE) {
                errorMessage = emailInput.getContext().getString(R.string.login_failure_credentials_toast);
            }
        }

        Toast.makeText(emailInput.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        ButterKnife.apply(buttons, ButterKnifeActions.setEnabled(true));
    }

    @OnClick(R.id.login_button)
    void onLoginClicked() {
        final String email = emailInput.getText().toString();
        final String password = passwordInput.getText().toString();
        ButterKnife.apply(buttons, ButterKnifeActions.setEnabled(false));
        loginParamsSubject.onNext(new SmartReceiptsUserLogin(email, password));
    }

    @OnClick(R.id.sign_up_button)
    void onSignUpClicked() {
        final String email = emailInput.getText().toString();
        final String password = passwordInput.getText().toString();
        ButterKnife.apply(buttons, ButterKnifeActions.setEnabled(false));
        loginParamsSubject.onNext(new SmartReceiptsUserSignUp(email, password));
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
