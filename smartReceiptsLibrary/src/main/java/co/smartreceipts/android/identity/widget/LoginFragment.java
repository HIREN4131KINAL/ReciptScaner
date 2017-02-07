package co.smartreceipts.android.identity.widget;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.FragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import co.smartreceipts.android.identity.apis.login.SmartReceiptsUserLogin;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponse;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class LoginFragment extends WBFragment {

    public static final String TAG = LoginFragment.class.getSimpleName();

    private static final String OUT_LOGIN_PARAMS = "out_login_params";

    private NavigationHandler mNavigationHandler;
    private IdentityManager mIdentityManager;

    private SmartReceiptsUserLogin mLoginParams;
    private Subscription mSubscription;

    private EditText mEmailInput;
    private EditText mPasswordInput;
    private Button mLoginButton;

    private TextView mDebug1;
    private TextView mDebug2;
    private TextView mDebug3;

    @NonNull
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mNavigationHandler = new NavigationHandler(getActivity(), getFragmentManager(), new FragmentProvider());
        mIdentityManager = getSmartReceiptsApplication().getIdentityManager();
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
        mEmailInput = (EditText) view.findViewById(R.id.login_email);
        mPasswordInput = (EditText) view.findViewById(R.id.login_password);
        mLoginButton = (Button) view.findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmailInput.getText().toString();
                final String password = mPasswordInput.getText().toString();
                logIn(new SmartReceiptsUserLogin(email, password));
            }
        });

        mDebug1 = (TextView) view.findViewById(R.id.login_debug_is_logged_in);
        mDebug2 = (TextView) view.findViewById(R.id.login_debug_email);
        mDebug3 = (TextView) view.findViewById(R.id.login_debug_token);
        showDebugText();
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
            mNavigationHandler.navigateUpToTripsFragment();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (!mNavigationHandler.isDualPane()) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            } else {
                actionBar.setHomeButtonEnabled(false);
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    @Override
    public void onPause() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        if (mLoginParams != null) {
            outState.putParcelable(OUT_LOGIN_PARAMS, mLoginParams);
        }
    }

    private void logIn(@NonNull SmartReceiptsUserLogin loginParams) {
        mLoginParams = loginParams;
        mSubscription = mIdentityManager.logIn(loginParams)
                .flatMap(new Func1<LoginResponse, Observable<OrganizationsResponse>>() {
                    @Override
                    public Observable<OrganizationsResponse> call(LoginResponse loginResponse) {
                        return mIdentityManager.getOrganizations();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mLoginButton.setEnabled(false);
                    }
                })
                .subscribe(new Action1<OrganizationsResponse>() {
                    @Override
                    public void call(OrganizationsResponse org) {
                        Toast.makeText(getContext(), "Login Success", Toast.LENGTH_SHORT).show();
                        showDebugText();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mIdentityManager.markLoginComplete(mLoginParams);
                        mLoginParams = null;
                        mLoginButton.setEnabled(true);
                        Toast.makeText(getContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mIdentityManager.markLoginComplete(mLoginParams);
                        mLoginParams = null;
                        mLoginButton.setEnabled(true);
                    }
                });
    }

    private void showDebugText() {
        mDebug1.setText("Is logged in == " + mIdentityManager.isLoggedIn());
        mDebug2.setText("Email: " + mIdentityManager.getEmail());
        mDebug3.setText("Token: " + mIdentityManager.getToken());
    }
}