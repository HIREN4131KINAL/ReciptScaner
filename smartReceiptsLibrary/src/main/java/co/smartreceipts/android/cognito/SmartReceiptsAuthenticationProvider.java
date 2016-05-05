package co.smartreceipts.android.cognito;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;
import com.amazonaws.regions.Regions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.apis.me.MeResponse;
import co.smartreceipts.android.apis.me.MeService;
import co.smartreceipts.android.identity.EmailAddress;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.LoginCallback;
import co.smartreceipts.android.identity.Token;
import retrofit2.Call;
import retrofit2.Response;

public class SmartReceiptsAuthenticationProvider extends AWSAbstractCognitoDeveloperIdentityProvider implements LoginCallback {

    private static final String TAG = SmartReceiptsAuthenticationProvider.class.getSimpleName();

    private final IdentityManager mIdentityManager;
    private final LocalCognitoTokenStore mLocalCognitoTokenStore;
    private final ServiceManager mServiceManager;
    private final Executor mExecutor;

    public SmartReceiptsAuthenticationProvider(@NonNull Context context, @NonNull IdentityManager identityManager, @NonNull ServiceManager serviceManager, @NonNull Regions regions) {
        this(identityManager, new LocalCognitoTokenStore(context), serviceManager, regions, Executors.newSingleThreadExecutor());
    }

    public SmartReceiptsAuthenticationProvider(@NonNull IdentityManager identityManager, @NonNull LocalCognitoTokenStore localCognitoTokenStore, @NonNull ServiceManager serviceManager, @NonNull Regions regions, @NonNull Executor executor) {
        super(null, "us-east-1:cdcc971a-b67f-4bc0-9a12-291b5d416518", regions);

        mIdentityManager = identityManager;
        mLocalCognitoTokenStore = localCognitoTokenStore;
        mServiceManager = serviceManager;
        mExecutor = executor;

        mIdentityManager.registerLoginCallback(this);
    }

    @Override
    public String getProviderName() {
        return "login.smartreceipts.co";
    }

    @Override
    public String refresh() {
        // Null things out to start
        setToken(null);

        try {
            final Response<MeResponse> response = submitMeRequest();
            if (response != null && response.isSuccessful()) {
                final MeResponse meResponse = response.body();
                if (meResponse.getUser() != null && meResponse.getUser().getCognito() != null) {
                    final CognitoToken cognitoToken = new CognitoToken(meResponse.getUser().getCognito());
                    // TODO: Add defensive logic here for split cases (e.g. id == null && token != null)
                    update(cognitoToken.getIdentityId(), cognitoToken.getCognitoToken());
                    return cognitoToken.getCognitoToken();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (final IOException e) {
            Log.e(TAG, "", e);
            return null;
        }

    }

    @Override
    @Nullable
    public String getIdentityId() {
        final String identityId = mLocalCognitoTokenStore.getCognitoToken().getIdentityId();
        if (identityId == null) {
            try {
                final Response<MeResponse> response = submitMeRequest();
                if (response != null && response.isSuccessful()) {
                    final MeResponse meResponse = response.body();
                    if (meResponse.getUser() != null && meResponse.getUser().getCognito() != null) {
                        final CognitoToken cognitoToken = new CognitoToken(meResponse.getUser().getCognito());
                        return cognitoToken.getIdentityId();
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (final IOException e) {
                Log.e(TAG, "", e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void onLoginSuccess() {
        final EmailAddress emailAddress = mIdentityManager.getEmail();
        if (emailAddress != null) {
            Map<String, String> logins = getLogins();
            if (logins == null) {
                logins = new HashMap<>();
            }
            logins.put(getProviderName(), emailAddress.get());
            setLogins(logins);

            // According to the docs, we always need to call refresh when updating this (then why don't they handle this internally...)?
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
        }
    }

    @Override
    public void onLoginFailure() {

    }


    @Nullable
    private Response<MeResponse> submitMeRequest() throws IOException {
        if (mIdentityManager.isLoggedIn()) {
            final MeService meService = mServiceManager.getService(MeService.class);
            final EmailAddress emailAddress = mIdentityManager.getEmail();
            final Token token = mIdentityManager.getToken();
            if (emailAddress != null && token != null) {
                final Call<MeResponse> call = meService.me(emailAddress.get(), token.get());
                return call.execute();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

}
