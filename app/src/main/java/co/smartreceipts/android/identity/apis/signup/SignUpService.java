package co.smartreceipts.android.identity.apis.signup;

import android.support.annotation.NonNull;

import co.smartreceipts.android.identity.apis.login.LoginPayload;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface SignUpService {

    @POST("api/users/sign_up")
    Observable<LoginResponse> signUp(@NonNull @Body SignUpPayload signUpPayload);
}
