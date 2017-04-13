package co.smartreceipts.android.identity.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.identity.apis.login.UserCredentialsPayload;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import co.smartreceipts.android.identity.apis.login.SmartReceiptsUserLogin;
import co.smartreceipts.android.identity.widget.presenters.LoginPresenter;
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.support.AndroidSupportInjection;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class LoginFragment extends WBFragment {

    public static final String TAG = LoginFragment.class.getSimpleName();

    private static final String OUT_LOGIN_PARAMS = "out_login_params";

    @Inject
    LoginInteractor loginInteractor;

    private LoginPresenter loginPresenter;

    private UserCredentialsPayload cachedUserCredentialsPayload;
    private CompositeSubscription compositeSubscription;

    @NonNull
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            final SmartReceiptsUserLogin loginParams = savedInstanceState.getParcelable(OUT_LOGIN_PARAMS);
            if (loginParams != null) {
                logInOrSignUp(loginParams);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.loginPresenter = new LoginPresenter(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return loginInteractor.navigateBack();
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.login_toolbar_title);
            actionBar.setSubtitle("");
        }
        if (this.compositeSubscription == null) {
            this.compositeSubscription = new CompositeSubscription();
        }
        this.loginPresenter.onResume();

        this.compositeSubscription.add(loginPresenter.getLoginOrSignUpParamsStream()
            .subscribe(new Action1<UserCredentialsPayload>() {
                @Override
                public void call(UserCredentialsPayload userCredentialsPayload) {
                    logInOrSignUp(userCredentialsPayload);
                }
            }));
    }

    @Override
    public void onPause() {
        Logger.debug(this, "onPause");
        this.loginPresenter.onPause();
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        outState.putParcelable(OUT_LOGIN_PARAMS, this.cachedUserCredentialsPayload);
    }

    @Override
    public void onDestroyView() {
        Logger.debug(this, "onDestroyView");
        this.loginPresenter.onDestroyView();
        super.onDestroyView();
    }

    private void logInOrSignUp(@NonNull UserCredentialsPayload userCredentialsPayload) {
        this.cachedUserCredentialsPayload = userCredentialsPayload;
        if (this.compositeSubscription == null) {
            this.compositeSubscription = new CompositeSubscription();
        }
        this.compositeSubscription.add(this.loginInteractor.loginOrSignUp(userCredentialsPayload)
                .subscribe(new Action1<LoginResponse>() {
                    @Override
                    public void call(LoginResponse org) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        loginPresenter.presentLoginFailure(throwable);
                        loginInteractor.onLoginResultsConsumed(cachedUserCredentialsPayload);
                        cachedUserCredentialsPayload = null;
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        loginPresenter.presentLoginSuccess();
                        loginInteractor.onLoginResultsConsumed(cachedUserCredentialsPayload);
                        cachedUserCredentialsPayload = null;
                        loginInteractor.navigateBack();
                    }
                }));
    }
}