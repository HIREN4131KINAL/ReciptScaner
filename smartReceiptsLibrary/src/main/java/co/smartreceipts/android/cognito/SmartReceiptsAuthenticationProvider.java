package co.smartreceipts.android.cognito;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;
import com.amazonaws.regions.Regions;

import co.smartreceipts.android.apis.hosts.BetaSmartReceiptsHostConfiguration;
import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.apis.login.LoginPayload;
import co.smartreceipts.android.apis.login.LoginResponse;
import co.smartreceipts.android.apis.login.LoginService;
import co.smartreceipts.android.apis.login.SmartReceiptsUserLogin;
import retrofit2.Call;
import retrofit2.Callback;

public class SmartReceiptsAuthenticationProvider extends AWSAbstractCognitoDeveloperIdentityProvider {

    private final LocalCognitoTokenStore mLocalCognitoTokenStore;
    private final ServiceManager mServiceManager;

    public SmartReceiptsAuthenticationProvider(@NonNull Context context, @NonNull ServiceManager serviceManager) {
        super(null, "us-east-1:cdcc971a-b67f-4bc0-9a12-291b5d416518", Regions.US_EAST_1);

        mLocalCognitoTokenStore = new LocalCognitoTokenStore(context);
        mServiceManager = serviceManager;
    }

    @Override
    public String getProviderName() {
        return "login.smartreceipts.co";
    }

    @Override
    public String refresh() {

        setToken(null);

        update(identityId, token);
        return token;

    }

    // If the app has a valid identityId return it, otherwise get a valid
    // identityId from your backend.

    @Override
    public String getIdentityId() {
        final ServiceManager serviceManager = new ServiceManager(new BetaSmartReceiptsHostConfiguration());
        final LoginService loginService = serviceManager.getService(LoginService.class);
        final LoginPayload request = new LoginPayload(new SmartReceiptsUserLogin("user", "password"));

        loginService.logIn(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, retrofit2.Response<LoginResponse> response) {
                Log.i("TAG", "WILL");
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.i("TAG", "WILL");
            }
        });

        return "";
    }

}
