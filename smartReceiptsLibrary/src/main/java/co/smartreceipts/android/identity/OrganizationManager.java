package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.identity.apis.organizations.Organization;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponse;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsService;
import co.smartreceipts.android.identity.store.IdentityStore;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class OrganizationManager {

    private final ServiceManager serviceManager;
    private final IdentityStore identityStore;
    private final UserPreferenceManager userPreferenceManager;

    public OrganizationManager(@NonNull ServiceManager serviceManager, @NonNull IdentityStore identityStore, @NonNull UserPreferenceManager userPreferenceManager) {
        this.serviceManager = Preconditions.checkNotNull(serviceManager);
        this.identityStore = Preconditions.checkNotNull(identityStore);
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
    }

    @NonNull
    public Observable<OrganizationsResponse> getOrganizations() {
        return getOrganizationsApiRequest()
                .flatMap(new Func1<OrganizationsResponse, Observable<OrganizationsResponse>>() {
                    @Override
                    public Observable<OrganizationsResponse> call(OrganizationsResponse organizationsResponse) {
                        return applyOrganizationsResponse(organizationsResponse);
                    }
                });
    }

    @NonNull
    private Observable<OrganizationsResponse> getOrganizationsApiRequest() {
        if (identityStore.getEmail() != null && identityStore.getToken() != null) {
            return serviceManager.getService(OrganizationsService.class).organizations(identityStore.getEmail(), identityStore.getToken());
        } else {
            return Observable.error(new IllegalStateException("Cannot fetch the user's organizations until we're logged in"));
        }
    }

    @NonNull
    private Observable<OrganizationsResponse> applyOrganizationsResponse(@NonNull final OrganizationsResponse response) {
        return getPrimaryOrganization(response)
                .flatMap(new Func1<Organization, Observable<Organization>>() {
                    @Override
                    public Observable<Organization> call(Organization organization) {
                        return applyOrganizationPreferences(organization);
                    }
                })
                .map(new Func1<Organization, OrganizationsResponse>() {
                    @Override
                    public OrganizationsResponse call(Organization organization) {
                        return response;
                    }
                });
    }

    @NonNull
    private Observable<Organization> getPrimaryOrganization(@NonNull final OrganizationsResponse response) {
        return Observable.create(new Observable.OnSubscribe<Organization>() {
            @Override
            public void call(Subscriber<? super Organization> subscriber) {
                if (response.getOrganizations() != null && !response.getOrganizations().isEmpty()) {
                    subscriber.onNext(response.getOrganizations().get(0));
                }
                subscriber.onCompleted();
            }
        });
    }

    @NonNull
    private Observable<Organization> applyOrganizationPreferences(@NonNull final Organization organization) {
        return Observable.create(new Observable.OnSubscribe<Organization>() {
            @Override
            public void call(Subscriber<? super Organization> subscriber) {
                Logger.info(OrganizationManager.class, "Found response: {}", organization.getAppSettings().getSettings());
                subscriber.onNext(organization);
                subscriber.onCompleted();
            }
        });
    }


}
