package co.smartreceipts.android.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.activities.DefaultFragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.apis.login.SmartReceiptsUserLogin;
import co.smartreceipts.android.apis.me.MeService;
import co.smartreceipts.android.apis.me.MeResponse;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.LoginCallback;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends WBFragment implements LoginCallback {

    public static final String TAG = LoginFragment.class.getSimpleName();

    private NavigationHandler mNavigationHandler;
    private IdentityManager mIdentityManager;

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
        mNavigationHandler = new NavigationHandler(getActivity(), getFragmentManager(), new DefaultFragmentProvider());
        mIdentityManager = getSmartReceiptsApplication().getIdentityManager();
        mIdentityManager.registerLoginCallback(this);
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
                mIdentityManager.logIn(new SmartReceiptsUserLogin(email, password));
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
    public void onDestroy() {
        mIdentityManager.unregisterLoginCallback(this);
        super.onDestroy();
    }

    @Override
    public void onLoginSuccess() {
        mEmailInput.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "Login Success", Toast.LENGTH_SHORT).show();
                showDebugText();
            }
        });
    }

    @Override
    public void onLoginFailure() {
        mEmailInput.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDebugText() {
        mDebug1.setText("Is logged in == " + mIdentityManager.isLoggedIn());
        mDebug2.setText("Email: " + mIdentityManager.getEmail());
        mDebug3.setText("Token: " + mIdentityManager.getToken());
    }
}