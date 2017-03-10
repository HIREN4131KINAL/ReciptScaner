package co.smartreceipts.android.identity.apis.me;

import retrofit2.http.GET;
import rx.Observable;

/**
 * An interface that we can use in conjunction with network requests
 */
public interface MeService {

    @GET("api/users/me")
    Observable<MeResponse> me();

}
