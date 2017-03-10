package co.smartreceipts.android.identity.apis.organizations;

import retrofit2.http.GET;
import rx.Observable;

public interface OrganizationsService {

    @GET("api/organizations")
    Observable<OrganizationsResponse> organizations();

}
