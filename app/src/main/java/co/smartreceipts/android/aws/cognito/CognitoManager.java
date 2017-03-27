package co.smartreceipts.android.aws.cognito;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.google.common.base.Preconditions;

import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.me.Cognito;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

public class CognitoManager {

    private final Context context;
    private final IdentityManager identityManager;
    private final CognitoIdentityProvider cognitoIdentityProvider;
    private final Scheduler subscribeOnScheduler;
    private final PublishSubject<Object> retryErrorsOnSubscribePredicate = PublishSubject.create();
    private final ReplaySubject<CognitoCachingCredentialsProvider> cachingCredentialsProviderReplaySubject = ReplaySubject.create(1);

    public CognitoManager(@NonNull Context context, @NonNull IdentityManager identityManager) {
        this(context, identityManager, new CognitoIdentityProvider(identityManager, context), Schedulers.io());
    }

    @VisibleForTesting
    CognitoManager(@NonNull Context context, @NonNull IdentityManager identityManager,
                   @NonNull CognitoIdentityProvider cognitoIdentityProvider, @NonNull Scheduler subscribeOnScheduler) {
        this.context = Preconditions.checkNotNull(context);
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.cognitoIdentityProvider = Preconditions.checkNotNull(cognitoIdentityProvider);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    public void initialize() {
        identityManager.isLoggedInStream()
                .subscribeOn(subscribeOnScheduler)
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean isLoggedIn) {
                        return isLoggedIn;
                    }
                })
                .flatMap(new Func1<Boolean, Observable<CognitoCachingCredentialsProvider>>() {
                    @Override
                    public Observable<CognitoCachingCredentialsProvider> call(Boolean isLoggedIn) {
                        return cognitoIdentityProvider.prefetchCognitoTokenIfNeeded()
                                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                                    @Override
                                    public Observable<?> call(Observable<? extends Throwable> errors) {
                                        return errors.flatMap(new Func1<Throwable, Observable<?>>() {
                                            @Override
                                            public Observable<?> call(Throwable throwable) {
                                                return retryErrorsOnSubscribePredicate;
                                            }
                                        });
                                    }
                                })
                                .map(new Func1<Cognito, CognitoCachingCredentialsProvider>() {
                                    @Override
                                    public CognitoCachingCredentialsProvider call(Cognito cognito) {
                                        final SmartReceiptsAuthenticationProvider authenticationProvider = new SmartReceiptsAuthenticationProvider(cognitoIdentityProvider, getRegions());
                                        return new CognitoCachingCredentialsProvider(context, authenticationProvider, getRegions());
                                    }
                                })
                                .toObservable();
                    }
                })
                .subscribe(cachingCredentialsProviderReplaySubject);
    }

    @NonNull
    public Observable<CognitoCachingCredentialsProvider> getCognitoCachingCredentialsProvider() {
        return cachingCredentialsProviderReplaySubject.asObservable()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        // Any time we subscribe, let's see if we can resolve any latent errors
                        retryErrorsOnSubscribePredicate.onNext(new Object());
                    }
                }).take(1);
    }

    @NonNull
    public Regions getRegions() {
        return Regions.US_EAST_1;
    }

}
