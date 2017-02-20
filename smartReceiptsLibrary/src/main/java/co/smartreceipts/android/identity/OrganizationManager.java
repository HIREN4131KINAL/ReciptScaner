package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.identity.apis.organizations.Organization;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponse;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsService;
import co.smartreceipts.android.identity.store.IdentityStore;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
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
                    public Observable<OrganizationsResponse> call(final OrganizationsResponse organizationsResponse) {
                        return applyOrganizationsResponse(organizationsResponse)
                                .map(new Func1<Object, OrganizationsResponse>() {
                                    @Override
                                    public OrganizationsResponse call(Object o) {
                                        return organizationsResponse;
                                    }
                                });
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.error(this, "Failed to complete the organizations request", throwable);
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
    private Observable<Object> applyOrganizationsResponse(@NonNull final OrganizationsResponse response) {
        return getPrimaryOrganization(response)
                .flatMap(new Func1<Organization, Observable<Object>>() {
                    @Override
                    public Observable<Object> call(Organization organization) {
                        return getOrganizationSettings(organization)
                                .flatMap(new Func1<JsonObject, Observable<Object>>() {
                                    @Override
                                    public Observable<Object> call(final JsonObject settings) {
                                        return userPreferenceManager.getUserPreferencesObservable()
                                                .flatMapIterable(new Func1<List<UserPreference<?>>, Iterable<UserPreference<?>>>() {
                                                    @Override
                                                    public Iterable<UserPreference<?>> call(List<UserPreference<?>> userPreferences) {
                                                        return userPreferences;
                                                    }
                                                })
                                                .flatMap(new Func1<UserPreference<?>, Observable<?>>() {
                                                    @Override
                                                    public Observable<?> call(UserPreference<?> toUserPreference) {
                                                        return apply(settings, toUserPreference);
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    @NonNull
    private Observable<Organization> getPrimaryOrganization(@NonNull final OrganizationsResponse response) {
        return Observable.create(new Observable.OnSubscribe<Organization>() {
            @Override
            public void call(Subscriber<? super Organization> subscriber) {
                if (response.getOrganizations() != null && !response.getOrganizations().isEmpty()) {
                    final Organization organization = response.getOrganizations().get(0);
                    if (organization.getError() != null && organization.getError().hasError()) {
                        subscriber.onError(new ApiValidationException(TextUtils.join(", ", organization.getError().getErrors())));
                    } else {
                        subscriber.onNext(organization);
                        subscriber.onCompleted();
                    }
                } else {
                    subscriber.onCompleted();
                }
            }
        });
    }

    @NonNull
    private Observable<JsonObject> getOrganizationSettings(@NonNull final Organization organization) {
        return Observable.create(new Observable.OnSubscribe<JsonObject>() {
            @Override
            public void call(Subscriber<? super JsonObject> subscriber) {
                if (organization.getAppSettings() != null && organization.getAppSettings().getSettings() != null) {
                    subscriber.onNext(organization.getAppSettings().getSettings());
                }
                subscriber.onCompleted();
            }
        });
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> Observable<T> apply(@NonNull JsonObject settings, @NonNull UserPreference<T> toPreference) {
        final String preferenceName = userPreferenceManager.getName(toPreference);
        if (settings.has(preferenceName)) {
            final JsonElement element = settings.get(preferenceName);
            if (!element.isJsonNull()) {
                Logger.debug(OrganizationManager.this, "Giving preference \'{}\' a value of {}.", preferenceName, element);
                if (Boolean.class.equals(toPreference.getType())) {
                    return userPreferenceManager.setObservable(toPreference, (T) Boolean.valueOf(element.getAsBoolean()));
                } else if (String.class.equals(toPreference.getType())) {
                    return userPreferenceManager.setObservable(toPreference, (T) element.getAsString());
                } else if (Float.class.equals(toPreference.getType())) {
                    return userPreferenceManager.setObservable(toPreference, (T) Float.valueOf(element.getAsFloat()));
                } else if (Integer.class.equals(toPreference.getType())) {
                    return userPreferenceManager.setObservable(toPreference, (T) Integer.valueOf(element.getAsInt()));
                } else {
                    return Observable.error(new Exception("Unsupported organization setting type for " + preferenceName));
                }
            } else {
                Logger.debug(OrganizationManager.this, "Skipping preference \'{}\', which is defined as null.", preferenceName, element);
                return Observable.empty();
            }
        } else {
            Logger.warn(OrganizationManager.this, "Failed to find preference: {}", preferenceName);
            return Observable.empty();
        }
    }

}
