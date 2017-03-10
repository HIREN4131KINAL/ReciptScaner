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
import co.smartreceipts.android.identity.apis.logout.LogoutResponse;
import co.smartreceipts.android.identity.apis.logout.LogoutService;
import co.smartreceipts.android.identity.apis.me.MeResponse;
import co.smartreceipts.android.identity.apis.me.MeService;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponse;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.identity.store.IdentityStore;
import co.smartreceipts.android.identity.store.MutableIdentityStore;
import co.smartreceipts.android.identity.store.Token;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.AsyncSubject;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

public class IdentityManager implements IdentityStore {

    private final Context context;
    private final ServiceManager serviceManager;
    private final Analytics analytics;
    private final MutableIdentityStore mutableIdentityStore;
    private final OrganizationManager organizationManager;
    private final Map<LoginParams, Subject<LoginResponse, LoginResponse>> loginMap = new ConcurrentHashMap<>();

    private Subject<LogoutResponse, LogoutResponse> logoutSubject;

    public IdentityManager(@NonNull Context context, @NonNull MutableIdentityStore mutableIdentityStore,
                           @NonNull ServiceManager serviceManager, @NonNull Analytics analytics,
                           @NonNull UserPreferenceManager userPreferenceManager) {
        this(context, mutableIdentityStore, serviceManager, analytics, new OrganizationManager(serviceManager, mutableIdentityStore, userPreferenceManager));
    }

    public IdentityManager(@NonNull Context context, @NonNull MutableIdentityStore mutableIdentityStore,
                           @NonNull ServiceManager serviceManager, @NonNull Analytics analytics,
                           @NonNull OrganizationManager organizationManager) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.serviceManager = Preconditions.checkNotNull(serviceManager);
        this.analytics = Preconditions.checkNotNull(analytics);
        this.mutableIdentityStore = Preconditions.checkNotNull(mutableIdentityStore);
        this.organizationManager = Preconditions.checkNotNull(organizationManager);
    }

    @Nullable
    @Override
    public EmailAddress getEmail() {
        return mutableIdentityStore.getEmail();
    }

    @Nullable
    @Override
    public Token getToken() {
        return mutableIdentityStore.getToken();
    }

    @Override
    public boolean isLoggedIn() {
        return mutableIdentityStore.isLoggedIn();
    }

    public synchronized Observable<LoginResponse> logIn(@NonNull final LoginParams login) {
        Preconditions.checkNotNull(login.getEmail(), "A valid email must be provided to log-in");

        Logger.info(this, "Initiating user log-in");
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
                                mutableIdentityStore.setEmailAndToken(login.getEmail(), loginResponse.getToken());
                                return Observable.just(loginResponse);
                            } else {
                                return Observable.error(new ApiValidationException("The response did not contain a valid API token"));
                            }
                        }
                    })
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logger.error(this, "Failed to complete the log-in request", throwable);
                            analytics.record(Events.Identity.UserLoginFailure);
                        }
                    })
                    .flatMap(new Func1<LoginResponse, Observable<LoginResponse>>() {
                        @Override
                        public Observable<LoginResponse> call(final LoginResponse loginResponse) {
                            return organizationManager.getOrganizations()
                                    .flatMap(new Func1<OrganizationsResponse, Observable<LoginResponse>>() {
                                        @Override
                                        public Observable<LoginResponse> call(OrganizationsResponse response) {
                                            return Observable.just(loginResponse);
                                        }
                                    });
                        }
                    })
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            Logger.info(this, "Successfully completed the log-in request");
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

    public synchronized Observable<LogoutResponse> logOut() {
        Logger.info(this, "Initiating user log-out");
        this.analytics.record(Events.Identity.UserLogout);

        final LogoutService logoutService = serviceManager.getService(LogoutService.class);

        if (logoutSubject == null) {
            logoutSubject = BehaviorSubject.create();
            logoutService.logOut()
                    .doOnNext(new Action1<LogoutResponse>() {
                        @Override
                        public void call(LogoutResponse logoutResponse) {
                            mutableIdentityStore.setEmailAndToken(null, null);
                        }
                    })
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logger.error(this, "Failed to complete the log-out request", throwable);
                            analytics.record(Events.Identity.UserLogoutFailure);
                        }
                    })
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            Logger.info(this, "Successfully completed the log-out request");
                            analytics.record(Events.Identity.UserLogoutSuccess);
                        }
                    })
                    .subscribe(logoutSubject);
        }
        return logoutSubject;
    }

    public synchronized void markLogoutComplete() {
        logoutSubject = null;
    }

    @NonNull
    public Observable<MeResponse> getMe() {
        if (isLoggedIn()) {
            return serviceManager.getService(MeService.class).me();
        } else {
            return Observable.error(new IllegalStateException("Cannot fetch the user's account until we're logged in"));
        }
    }

    @NonNull
    public Observable<OrganizationsResponse> getOrganizations() {
        return organizationManager.getOrganizations();
    }
}
