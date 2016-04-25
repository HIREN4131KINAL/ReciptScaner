package co.smartreceipts.android.identity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.apis.login.LoginPayload;
import co.smartreceipts.android.apis.login.LoginResponse;
import co.smartreceipts.android.apis.login.LoginService;
import co.smartreceipts.android.apis.login.SmartReceiptsUserLogin;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IdentityManager {

    private final Context mContext;
    private final ServiceManager mServiceManager;
    private final IdentityStore mIdentityStore;

    public IdentityManager(@NonNull Context context, @NonNull ServiceManager serviceManager) {
        this(context, serviceManager, new IdentityStore(context));
    }

    public IdentityManager(@NonNull Context context, @NonNull ServiceManager serviceManager, @NonNull IdentityStore identityStore) {
        mContext = context.getApplicationContext();
        mServiceManager = serviceManager;
        mIdentityStore = identityStore;
    }

    public boolean isLoggedIn() {
        return getEmail() != null && getToken() != null;
    }

    @Nullable
    public EmailAddress getEmail() {
        return mIdentityStore.getEmail();
    }

    @Nullable
    public Token getToken() {
        return mIdentityStore.getToken();
    }

    public void logIn(@NonNull final SmartReceiptsUserLogin login, @NonNull final LoginCallback loginCallback) {
        final LoginService loginService = mServiceManager.getService(LoginService.class);
        final LoginPayload request = new LoginPayload(login);

        loginService.logIn(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    final LoginResponse body = response.body();
                    if (body.getToken() != null && login.getEmail() != null) {
                        mIdentityStore.setEmailAddress(login.getEmail());
                        mIdentityStore.setToken(body.getToken());
                        loginCallback.onSuccess();
                    } else {
                        loginCallback.onFailure();
                    }
                } else {
                    loginCallback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loginCallback.onFailure();
            }
        });
    }
}
