package co.smartreceipts.android.apis.organizations;

import android.support.annotation.NonNull;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface OrganizationsService {

    @GET("api/organizations")
    Observable<Void> organizations(@NonNull @Query("auth_params[email]") CharSequence email, @NonNull @Query("auth_params[token]") CharSequence token);

}
