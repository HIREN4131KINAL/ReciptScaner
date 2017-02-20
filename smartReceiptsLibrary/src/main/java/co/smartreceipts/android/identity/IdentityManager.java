package co.smartreceipts.android.identity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.identity.apis.login.LoginParams;
import co.smartreceipts.android.identity.apis.login.LoginPayload;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import co.smartreceipts.android.identity.apis.login.LoginService;
import co.smartreceipts.android.identity.apis.me.MeResponse;
import co.smartreceipts.android.identity.apis.me.MeService;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponse;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsService;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.identity.store.IdentityStore;
import co.smartreceipts.android.identity.store.Token;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.AsyncSubject;
import rx.subjects.Subject;

public class IdentityManager {

    private final Context context;
    private final ServiceManager serviceManager;
    private final Analytics analytics;
    private final IdentityStore identityStore;
    private final OrganizationManager organizationManager;
    private final Map<LoginParams, Subject<LoginResponse, LoginResponse>> loginMap = new ConcurrentHashMap<>();

    public IdentityManager(@NonNull Context context, @NonNull ServiceManager serviceManager, @NonNull Analytics analytics,
                           @NonNull UserPreferenceManager userPreferenceManager) {
        this(context, serviceManager, analytics, new IdentityStore(context), userPreferenceManager);
    }

    public IdentityManager(@NonNull Context context, @NonNull ServiceManager serviceManager, @NonNull Analytics analytics,
                           @NonNull IdentityStore identityStore, @NonNull UserPreferenceManager userPreferenceManager) {
        this(context, serviceManager, analytics, identityStore, new OrganizationManager(serviceManager, identityStore, userPreferenceManager));
    }

    public IdentityManager(@NonNull Context context, @NonNull ServiceManager serviceManager, @NonNull Analytics analytics,
                           @NonNull IdentityStore identityStore, @NonNull OrganizationManager organizationManager) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.serviceManager = Preconditions.checkNotNull(serviceManager);
        this.analytics = Preconditions.checkNotNull(analytics);
        this.identityStore = Preconditions.checkNotNull(identityStore);
        this.organizationManager = Preconditions.checkNotNull(organizationManager);
    }

    public boolean isLoggedIn() {
        return getEmail() != null && getToken() != null;
    }

    @Nullable
    public EmailAddress getEmail() {
        return identityStore.getEmail();
    }

    @Nullable
    public Token getToken() {
        return identityStore.getToken();
    }

    public synchronized Observable<LoginResponse> logIn(@NonNull final LoginParams login) {
        Preconditions.checkNotNull(login.getEmail(), "A valid email must be provided to login");

        Logger.info(this, "Initiating user login");
        this.analytics.record(Events.Identity.UserLogin);

        final LoginService loginService = serviceManager.getService(LoginService.class);
        final LoginPayload request = new LoginPayload(login);

        Subject<LoginResponse, LoginResponse> loginSubject = loginMap.get(login);
        if (loginSubject == null) {
            loginSubject = AsyncSubject.create();
            loginService.logIn(request)
                    .flatMap(new Func1<LoginResponse, Observable<LoginResponse>>() {
                        @Override
                        public Observable<LoginResponse> call(LoginResponse loginResponse) {
                            if (loginResponse.getToken() != null) {
                                identityStore.setEmailAddress(login.getEmail());
                                identityStore.setToken(loginResponse.getToken());
                                return Observable.just(loginResponse);
                            } else {
                                return Observable.error(new ApiValidationException("The response did not contain a valid API token"));
                            }
                        }
                    })
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logger.info(this, "Failed to complete the login request");
                            analytics.record(Events.Identity.UserLoginFailure);
                        }
                    })
                    .flatMap(new Func1<LoginResponse, Observable<LoginResponse>>() {
                        @Override
                        public Observable<LoginResponse> call(final LoginResponse loginResponse) {
                            return Observable.just(loginResponse);
                        }
                    })
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            Logger.info(this, "Successfully completed the login request");
                            analytics.record(Events.Identity.UserLoginSuccess);
                        }
                    })
                    .subscribe(loginSubject);
            loginMap.put(login, loginSubject);
        }
        return loginSubject;
    }

    public synchronized void markLoginComplete(@NonNull final LoginParams login) {
        loginMap.remove(login);
    }

    @NonNull
    public Observable<MeResponse> getMe() {
        if (identityStore.getEmail() != null && identityStore.getToken() != null) {
            return serviceManager.getService(MeService.class).me(identityStore.getEmail(), identityStore.getToken());
        } else {
            return Observable.error(new IllegalStateException("Cannot fetch the user's account until we're logged in"));
        }
    }

    @NonNull
    public Observable<OrganizationsResponse> getOrganizations() {
        return organizationManager.getOrganizations();
    }
}
