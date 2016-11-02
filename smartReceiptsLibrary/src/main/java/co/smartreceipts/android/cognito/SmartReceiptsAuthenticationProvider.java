package co.smartreceipts.android.cognito;

import android.util.Log;

import com.amazonaws.auth.AWSAbstractCognitoDeveloperIdentityProvider;
import com.amazonaws.regions.Regions;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;


public class SmartReceiptsAuthenticationProvider extends AWSAbstractCognitoDeveloperIdentityProvider {


    public SmartReceiptsAuthenticationProvider() {
        super(null, "us-east-1:cdcc971a-b67f-4bc0-9a12-291b5d416518", Regions.US_EAST_1);
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
        final Retrofit restAdapter = new RetrofitFactory().get();
        final LoginService loginService = restAdapter.create(LoginService.class);
        final LoginPayload request = new LoginPayload(new LoginParams("login", "user", "password", null));
        loginService.logIn(request).enqueue(new Callback<UserIdResponse>() {
            @Override
            public void onResponse(Call<UserIdResponse> call, retrofit2.Response<UserIdResponse> response) {
                Log.i("TAG", "WILL");
            }

            @Override
            public void onFailure(Call<UserIdResponse> call, Throwable t) {
                Log.i("TAG", "WILL");
            }
        });

        final UserIdService userIdService = restAdapter.create(UserIdService.class);
        userIdService.getUserId("2").enqueue(new Callback<UserIdResponse>() {
            @Override
            public void onResponse(Call<UserIdResponse> call, retrofit2.Response<UserIdResponse> response) {
                Log.i("TAG", "WILL");
            }

            @Override
            public void onFailure(Call<UserIdResponse> call, Throwable t) {
                Log.i("TAG", "WILL");
            }
        });
        return "";
    }

}
