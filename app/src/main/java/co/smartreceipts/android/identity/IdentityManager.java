package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import org.reactivestreams.Subscriber;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.apis.login.LoginPayload;
import co.smartreceipts.android.identity.apis.login.LoginService;
import co.smartreceipts.android.identity.apis.login.LoginType;
import co.smartreceipts.android.identity.apis.login.UserCredentialsPayload;
import co.smartreceipts.android.identity.apis.login.LoginResponse;
import co.smartreceipts.android.identity.apis.logout.LogoutResponse;
import co.smartreceipts.android.identity.apis.logout.LogoutService;
import co.smartreceipts.android.identity.apis.me.MeResponse;
import co.smartreceipts.android.identity.apis.me.MeService;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponse;
import co.smartreceipts.android.identity.apis.signup.SignUpPayload;
import co.smartreceipts.android.identity.apis.signup.SignUpService;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.identity.store.IdentityStore;
import co.smartreceipts.android.identity.store.MutableIdentityStore;
import co.smartreceipts.android.identity.store.Token;
import co.smartreceipts.android.push.apis.me.UpdatePushTokensRequest;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;


@ApplicationScope
public class IdentityManager implements IdentityStore {

    private final ServiceManager serviceManager;
    private final Analytics analytics;
    private final MutableIdentityStore mutableIdentityStore;
    private final OrganizationManager organizationManager;
    private final BehaviorSubject<Boolean> isLoggedInBehaviorSubject;
    private final Map<UserCredentialsPayload, Subject<LoginResponse, LoginResponse>> loginMap = new ConcurrentHashMap<>();

    private Subject<LogoutResponse> logoutSubject;

    @Inject
    public IdentityManager(Analytics analytics,
                           UserPreferenceManager userPreferenceManager,
                           MutableIdentityStore mutableIdentityStore,
                           ServiceManager serviceManager) {

        this.serviceManager = serviceManager;
        this.analytics = analytics;
        this.mutableIdentityStore = mutableIdentityStore;
        this.organizationManager = new OrganizationManager(serviceManager, mutableIdentityStore, userPreferenceManager);
        this.isLoggedInBehaviorSubject = BehaviorSubject.createDefault(isLoggedIn());

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

    /**
     * @return an {@link Observable} relay that will only emit {@link Subscriber#onNext(Object)} calls
     * (and never {@link Subscriber#onComplete()} or {@link Subscriber#onError(Throwable)} calls) under
     * the following circumstances:
     * <ul>
     * <li>When the app launches, it will emit {@code true} if logged in and {@code false} if not</li>
     * <li>When the user signs in, it will emit  {@code true}</li>
     * <li>When the user signs out, it will emit  {@code false}</li>
     * </ul>
     * <p>
     * Users of this class should expect a {@link BehaviorSubject} type behavior in which the current
     * state will always be emitted as soon as we subscribe
     * </p>
     */
    @NonNull
    public Observable<Boolean> isLoggedInStream() {
        return isLoggedInBehaviorSubject;
    }

    public synchronized Observable<LoginResponse> logInOrSignUp(@NonNull final UserCredentialsPayload credentials) {
        Preconditions.checkNotNull(credentials.getEmail(), "A valid email must be provided to log-in");

        Subject<LoginResponse, LoginResponse> loginSubject = loginMap.get(credentials);
        if (loginSubject == null) {
            loginSubject = AsyncSubject.create();

            final Observable<LoginResponse> loginResponseObservable;
            if (credentials.getLoginType() == LoginType.LogIn) {
                loginResponseObservable = serviceManager.getService(LoginService.class).logIn(new LoginPayload(credentials));
                Logger.info(this, "Initiating user log in");
                this.analytics.record(Events.Identity.UserLogin);
            } else if (credentials.getLoginType() == LoginType.SignUp) {
                loginResponseObservable = serviceManager.getService(SignUpService.class).signUp(new SignUpPayload(credentials));
                Logger.info(this, "Initiating user sign up");
                this.analytics.record(Events.Identity.UserSignUp);
            } else {
                throw new IllegalArgumentException("Unsupported log in type");
            }


            loginResponseObservable
                    .flatMap(new Func1<LoginResponse, Observable<LoginResponse>>() {
                        @Override
                        public Observable<LoginResponse> call(LoginResponse loginResponse) {
                            if (loginResponse.getToken() != null) {
                                mutableIdentityStore.setEmailAndToken(credentials.getEmail(), loginResponse.getToken());
                                return Observable.just(loginResponse);
                            } else {
                                return Observable.error(new ApiValidationException("The response did not contain a valid API token"));
                            }
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
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            if (credentials.getLoginType() == LoginType.LogIn) {
                                Logger.error(this, "Failed to complete the log in request", throwable);
                                analytics.record(Events.Identity.UserLoginFailure);
                            } else if (credentials.getLoginType() == LoginType.SignUp) {
                                Logger.error(this, "Failed to complete the sign up request", throwable);
                                analytics.record(Events.Identity.UserSignUpFailure);
                            }
                        }
                    })
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            isLoggedInBehaviorSubject.onNext(true);
                            if (credentials.getLoginType() == LoginType.LogIn) {
                                Logger.info(this, "Successfully completed the log in request");
                                analytics.record(Events.Identity.UserLoginSuccess);
                            } else if (credentials.getLoginType() == LoginType.SignUp) {
                                Logger.info(this, "Successfully completed the sign up request");
                                analytics.record(Events.Identity.UserSignUpSuccess);
                            }
                        }
                    })
                    .subscribe(loginSubject);
            loginMap.put(credentials, loginSubject);
        }
        return loginSubject;
    }

    public synchronized void markLoginComplete(@NonNull final UserCredentialsPayload login) {
        loginMap.remove(login);
    }

    public synchronized Observable<LogoutResponse> logOut() {
        Logger.info(this, "Initiating user log-out");
        this.analytics.record(Events.Identity.UserLogout);

        final LogoutService logoutService = serviceManager.getService(LogoutService.class);

        if (logoutSubject == null) {
            logoutSubject = BehaviorSubject.create();
            logoutService.logOut()
                    .doOnNext(logoutResponse -> mutableIdentityStore.setEmailAndToken(null, null))
                    .doOnError(throwable -> {
                        Logger.error(this, "Failed to complete the log-out request", throwable);
                        analytics.record(Events.Identity.UserLogoutFailure);
                    })
                    .doOnComplete(() -> {
                        Logger.info(this, "Successfully completed the log-out request");
                        isLoggedInBehaviorSubject.onNext(false);
                        analytics.record(Events.Identity.UserLogoutSuccess);
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
    public Observable<MeResponse> updateMe(@NonNull UpdatePushTokensRequest request) {
        if (isLoggedIn()) {
            return serviceManager.getService(MeService.class).me(request);
        } else {
            return Observable.error(new IllegalStateException("Cannot fetch the user's account until we're logged in"));
        }
    }

    @NonNull
    public Observable<OrganizationsResponse> getOrganizations() {
        return organizationManager.getOrganizations();
    }
}
