package co.smartreceipts.android.aws.cognito;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.IdentityManager;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;


@ApplicationScope
public class CognitoManager {

    private final Context context;
    private final IdentityManager identityManager;
    private final CognitoIdentityProvider cognitoIdentityProvider;
    private final Scheduler subscribeOnScheduler;
    private final PublishSubject<Object> retryErrorsOnSubscribePredicate = PublishSubject.create();
    private final ReplaySubject<Optional<CognitoCachingCredentialsProvider>> cachingCredentialsProviderReplaySubject = ReplaySubject.createWithSize(1);
    private Subscription cachingCredentialsProviderSubscription;

    @Inject
    public CognitoManager(Context context, IdentityManager identityManager) {
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
        cachingCredentialsProviderSubscription = identityManager.isLoggedInStream()
                .subscribeOn(subscribeOnScheduler)
                .flatMap((new Func1<Boolean, Observable<Optional<CognitoCachingCredentialsProvider>>>() {
                    @Override
                    public Observable<Optional<CognitoCachingCredentialsProvider>> call(Boolean isLoggedIn) {
                        if (isLoggedIn) {
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
                                    .map(new Func1<Cognito, Optional<CognitoCachingCredentialsProvider>>() {
                                        @Override
                                        public Optional<CognitoCachingCredentialsProvider> call(Cognito cognito) {
                                            final SmartReceiptsAuthenticationProvider authenticationProvider = new SmartReceiptsAuthenticationProvider(cognitoIdentityProvider, getRegions());
                                            return Optional.of(new CognitoCachingCredentialsProvider(context, authenticationProvider, getRegions()));
                                        }
                                    })
                                    .onErrorReturn(new Func1<Throwable, Optional<CognitoCachingCredentialsProvider>>() {
                                        @Override
                                        public Optional<CognitoCachingCredentialsProvider> call(Throwable throwable) {
                                            return Optional.absent();
                                        }
                                    })
                                    .toObservable();
                        } else {
                            return Observable.just(Optional.<CognitoCachingCredentialsProvider>absent());
                        }
                    }
                }))
                .subscribe(new Subscriber<Optional<CognitoCachingCredentialsProvider>>() {
                    @Override
                    public void onCompleted() {
                        cachingCredentialsProviderReplaySubject.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        cachingCredentialsProviderReplaySubject.onError(e);
                    }

                    @Override
                    public void onNext(Optional<CognitoCachingCredentialsProvider> cognitoCachingCredentialsProviderOptional) {
                        cachingCredentialsProviderReplaySubject.onNext(cognitoCachingCredentialsProviderOptional);
                        if (cognitoCachingCredentialsProviderOptional.isPresent()) {
                            cachingCredentialsProviderReplaySubject.onCompleted();
                            if (cachingCredentialsProviderSubscription != null) {
                                cachingCredentialsProviderSubscription.unsubscribe();
                            }
                        }
                    }
                });

    }

    /**
     * @return an {@link Optional} instance of the {@link CognitoCachingCredentialsProvider}. Once we
     * fetch a valid entry, this should be treated as a singleton for the lifetime of the parent
     * {@link CognitoManager} object, since we use a replay subject
     */
    @NonNull
    public Observable<Optional<CognitoCachingCredentialsProvider>> getCognitoCachingCredentialsProvider() {
        return cachingCredentialsProviderReplaySubject.asObservable()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        // Any time we subscribe, let's see if we can resolve any latent errors
                        retryErrorsOnSubscribePredicate.onNext(new Object());
                    }
                });
    }

    @NonNull
    public Regions getRegions() {
        return Regions.US_EAST_1;
    }

}
