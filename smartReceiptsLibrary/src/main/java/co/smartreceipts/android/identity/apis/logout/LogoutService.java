package co.smartreceipts.android.identity.apis.logout;

import android.support.annotation.NonNull;

import co.smartreceipts.android.identity.apis.login.LoginPayload;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface LogoutService {

    @POST("api/users/log_out")
    Observable<LogoutService> logOut(@NonNull @Query("auth_params[email]") CharSequence email, @NonNull @Query("auth_params[token]") CharSequence token);
}
