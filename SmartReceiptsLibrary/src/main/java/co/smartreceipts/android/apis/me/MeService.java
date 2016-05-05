package co.smartreceipts.android.apis.me;

import android.support.annotation.NonNull;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * An interface that we can use in conjunction with network requests
 */
public interface MeService {

    @GET("api/users/me")
    Call<MeResponse> me(@NonNull @Query("auth_params[email]") String email, @NonNull @Query("auth_params[token]") String token);
}
