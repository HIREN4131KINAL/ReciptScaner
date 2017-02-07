package co.smartreceipts.android.identity.apis.login;

import android.support.annotation.NonNull;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * An interface that we can use in conjunction with network requests
 */
public interface LoginService {

    @POST("api/users/log_in")
    Observable<LoginResponse> logIn(@NonNull @Body LoginPayload loginPayload);
}
