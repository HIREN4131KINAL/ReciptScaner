package co.smartreceipts.android.identity.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.FragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.identity.apis.login.LoginParams;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import co.smartreceipts.android.identity.apis.login.SmartReceiptsUserLogin;
import co.smartreceipts.android.identity.apis.logout.LogoutResponse;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.identity.widget.presenters.MyAccountPresenter;
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.support.AndroidSupportInjection;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class LoginFragment extends WBFragment {

    public static final String TAG = LoginFragment.class.getSimpleName();

    private static final String OUT_LOGIN_PARAMS = "out_login_params";

    @Inject
    AnalyticsManager analyticsManager;

    private MyAccountPresenter myAccountPresenter;
    private LoginInteractor loginInteractor;
    private NavigationHandler navigationHandler;

    private LoginParams cachedLoginParams;
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
        this.navigationHandler = new NavigationHandler(getActivity(), getFragmentManager(), new FragmentProvider());
        this.loginInteractor = new LoginInteractor(getFragmentManager(),
                getSmartReceiptsApplication().getIdentityManager(),
                analyticsManager);
        if (savedInstanceState != null) {
            final SmartReceiptsUserLogin loginParams = savedInstanceState.getParcelable(OUT_LOGIN_PARAMS);
            if (loginParams != null) {
                logIn(loginParams);
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
        this.myAccountPresenter = new MyAccountPresenter(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.navigationHandler.navigateUpToTripsFragment();
            return true;
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
        }
        if (this.compositeSubscription == null) {
            this.compositeSubscription = new CompositeSubscription();
        }
        this.myAccountPresenter.onResume();

        this.compositeSubscription.add(myAccountPresenter.getLoginParamsStream()
            .subscribe(new Action1<LoginParams>() {
                @Override
                public void call(LoginParams loginParams) {
                    logIn(loginParams);
                }
            }));
        this.compositeSubscription.add(myAccountPresenter.getLogoutStream()
            .flatMap(new Func1<Void, Observable<LogoutResponse>>() {
                @Override
                public Observable<LogoutResponse> call(Void aVoid) {
                    return loginInteractor.logOut();
                }
            })
            // TODO: Handle rotation stuff here?
            .subscribe(new Action1<LogoutResponse>() {
                @Override
                public void call(LogoutResponse logoutResponse) {
                    // TODO: onSuccess
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    // TODO: onError
                }
            }));
        this.compositeSubscription.add(this.loginInteractor.isLoggedIn()
            .subscribe(new Action1<EmailAddress>() {
                @Override
                public void call(EmailAddress emailAddress) {
                    if (emailAddress != null) {
                        if (actionBar != null) {
                            actionBar.setTitle(R.string.my_account_toolbar_title);
                        }
                        myAccountPresenter.presentExistingUserSignedIn(emailAddress);
                    } else {
                        if (actionBar != null) {
                            actionBar.setTitle(R.string.login_toolbar_title);
                        }
                        myAccountPresenter.presentNoUserSignedIn();
                    }
                }
            }));
    }

    @Override
    public void onPause() {
        Logger.debug(this, "onPause");
        this.myAccountPresenter.onPause();
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
        outState.putParcelable(OUT_LOGIN_PARAMS, this.cachedLoginParams);
    }

    private void logIn(@NonNull LoginParams loginParams) {
        this.cachedLoginParams = loginParams;
        if (this.compositeSubscription == null) {
            this.compositeSubscription = new CompositeSubscription();
        }
        this.compositeSubscription.add(this.loginInteractor.login(loginParams)
                .subscribe(new Action1<LoginResponse>() {
                    @Override
                    public void call(LoginResponse org) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        myAccountPresenter.presentLoginFailure();
                        loginInteractor.onLoginResultsConsumed(cachedLoginParams);
                        cachedLoginParams = null;
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        myAccountPresenter.presentLoginSuccess();
                        loginInteractor.onLoginResultsConsumed(cachedLoginParams);
                        cachedLoginParams = null;
                    }
                }));
    }
}