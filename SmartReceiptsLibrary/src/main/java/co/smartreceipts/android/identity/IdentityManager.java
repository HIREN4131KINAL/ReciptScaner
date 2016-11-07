package co.smartreceipts.android.identity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.apis.login.LoginParams;
import co.smartreceipts.android.apis.login.LoginPayload;
import co.smartreceipts.android.apis.login.LoginResponse;
import co.smartreceipts.android.apis.login.LoginService;
import co.smartreceipts.android.apis.login.SmartReceiptsUserLogin;
import co.smartreceipts.android.apis.me.MeResponse;
import co.smartreceipts.android.apis.me.MeService;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subjects.AsyncSubject;
import rx.subjects.Subject;

public class IdentityManager {

    private final Context mContext;
    private final ServiceManager mServiceManager;
    private final IdentityStore mIdentityStore;
    private final Map<LoginParams, Subject<LoginResponse, LoginResponse>> mLoginMap = new ConcurrentHashMap<>();

    public IdentityManager(@NonNull Context context, @NonNull ServiceManager serviceManager) {
        this(context, serviceManager, new IdentityStore(context));
    }

    public IdentityManager(@NonNull Context context, @NonNull ServiceManager serviceManager, @NonNull IdentityStore identityStore) {
        mContext = context.getApplicationContext();
        mServiceManager = serviceManager;
        mIdentityStore = identityStore;
    }

    public boolean isLoggedIn() {
        return getEmail() != null && getToken() != null;
    }

    @Nullable
    public EmailAddress getEmail() {
        return mIdentityStore.getEmail();
    }

    @Nullable
    public Token getToken() {
        return mIdentityStore.getToken();
    }

    public synchronized Observable<LoginResponse> logIn(@NonNull final SmartReceiptsUserLogin login) {
        Preconditions.checkNotNull(login.getEmail(), "A valid email must be provided to login");

        final LoginService loginService = mServiceManager.getService(LoginService.class);
        final LoginPayload request = new LoginPayload(login);

        Subject<LoginResponse, LoginResponse> loginSubject = mLoginMap.get(login);
        if (loginSubject == null) {
            loginSubject = AsyncSubject.create();;
            loginService.logIn(request)
                    .flatMap(new Func1<LoginResponse, Observable<LoginResponse>>() {
                        @Override
                        public Observable<LoginResponse> call(LoginResponse loginResponse) {
                            if (loginResponse.getToken() != null) {
                                mIdentityStore.setEmailAddress(login.getEmail());
                                mIdentityStore.setToken(loginResponse.getToken());
                                return Observable.just(loginResponse);
                            } else {
                                return Observable.error(new ApiValidationException("The response did not contain a valid API token"));
                            }
                        }
                    })
                    .subscribe(loginSubject);
            mLoginMap.put(login, loginSubject);
        }
        return loginSubject;
    }

    public synchronized void markLoginComplete(@NonNull final LoginParams login) {
        mLoginMap.remove(login);
    }

    public Observable<MeResponse> getMe() {
        if (mIdentityStore.getEmail() != null && mIdentityStore.getToken() != null) {
            return mServiceManager.getService(MeService.class).me(mIdentityStore.getEmail(), mIdentityStore.getToken());
        } else {
            return Observable.error(new IllegalStateException("Cannot fetch the user account until we're logged in"));
        }
    }
}
