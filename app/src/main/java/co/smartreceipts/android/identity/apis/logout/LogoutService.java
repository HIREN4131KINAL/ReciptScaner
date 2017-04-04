package co.smartreceipts.android.identity.apis.logout;

import retrofit2.http.POST;
import rx.Observable;

public interface LogoutService {

    @POST("api/users/log_out")
    Observable<LogoutResponse> logOut();
}
