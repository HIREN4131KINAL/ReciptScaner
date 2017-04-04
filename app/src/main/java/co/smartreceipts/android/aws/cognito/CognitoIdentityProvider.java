package co.smartreceipts.android.aws.cognito;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.me.Cognito;
import co.smartreceipts.android.identity.apis.me.MeResponse;
import co.smartreceipts.android.identity.apis.me.User;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

class CognitoIdentityProvider {

    private final IdentityManager identityManager;
    private final LocalCognitoTokenStore localCognitoTokenStore;

    public CognitoIdentityProvider(@NonNull IdentityManager identityManager, @NonNull Context context) {
        this(identityManager, new LocalCognitoTokenStore(context));
    }

    @VisibleForTesting
    CognitoIdentityProvider(@NonNull IdentityManager identityManager, @NonNull LocalCognitoTokenStore localCognitoTokenStore) {
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.localCognitoTokenStore = Preconditions.checkNotNull(localCognitoTokenStore);
    }

    @NonNull
    public Single<Cognito> prefetchCognitoTokenIfNeeded() {
        return Observable.create(new Observable.OnSubscribe<Cognito>() {
                    @Override
                    public void call(Subscriber<? super Cognito> subscriber) {
                        subscriber.onNext(localCognitoTokenStore.getCognitoToken());
                        subscriber.onCompleted();
                    }
                })
                .flatMap(new Func1<Cognito, Observable<Cognito>>() {
                    @Override
                    public Observable<Cognito> call(Cognito cognito) {
                        if (cognito == null || cognito.getCognitoToken() == null || cognito.getIdentityId() == null) {
                            Logger.debug(CognitoIdentityProvider.this, "Existing cognito token is invalid. Pre-fetching...");
                            return refreshCognitoToken().toObservable();
                        } else {
                            Logger.debug(CognitoIdentityProvider.this, "Existing cognito token is valid");
                            return Observable.just(cognito);
                        }
                    }
                })
                .toSingle();
    }

    @NonNull
    public Single<Cognito> refreshCognitoToken() {
        return this.identityManager.getMe()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Logger.debug(CognitoIdentityProvider.this, "Clearing the cached cognito token to refresh");
                        localCognitoTokenStore.persist(null);
                    }
                })
                .map(new Func1<MeResponse, Cognito>() {
                    @Override
                    public Cognito call(@Nullable MeResponse meResponse) {
                        if (meResponse != null && meResponse.getUser() != null) {
                            Logger.debug(CognitoIdentityProvider.this, "Retrieve a valid token response");
                            final User user = meResponse.getUser();
                            return new Cognito(user.getCognitoToken(), user.getIdentityId(), user.getCognitoTokenExpiresAt());
                        } else {
                            Logger.warn(CognitoIdentityProvider.this, "Failed to fetch a valid token");
                            return null;
                        }
                    }
                })
                .doOnNext(new Action1<Cognito>() {
                    @Override
                    public void call(@Nullable Cognito cognito) {
                        localCognitoTokenStore.persist(cognito);
                    }
                })
                .toSingle();
    }

    @Nullable
    public Cognito synchronouslyRefreshCognitoToken() {
        return refreshCognitoToken().onErrorReturn(new Func1<Throwable, Cognito>() {
                    @Override
                    public Cognito call(Throwable throwable) {
                        return null;
                    }
                })
                .toBlocking().value();
    }

    @Nullable
    public Cognito getCachedCognitoToken() {
        return localCognitoTokenStore.getCognitoToken();
    }
}
