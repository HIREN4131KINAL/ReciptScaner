package co.smartreceipts.android.aws.cognito;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.hadisatrio.optional.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.me.Cognito;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CognitoManagerTest {

    // Class under test
    CognitoManager cognitoManager;

    Context context = RuntimeEnvironment.application;

    @Mock
    IdentityManager identityManager;

    @Mock
    CognitoIdentityProvider cognitoIdentityProvider;

    @Mock
    Cognito cognito;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(cognitoIdentityProvider.prefetchCognitoTokenIfNeeded()).thenReturn(Single.just(cognito));
        when(identityManager.isLoggedInStream()).thenReturn(Observable.just(true));
        cognitoManager = new CognitoManager(context, identityManager, cognitoIdentityProvider, Schedulers.immediate());
    }

    @Test
    public void initializeAndGetCognitoCachingCredentialsProviderWhenNotLoggedIn() {
        final BehaviorSubject<Boolean> isLoggedInStream = BehaviorSubject.create(false);
        when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
        cognitoManager.initialize();

        final TestSubscriber<Optional<CognitoCachingCredentialsProvider>> testSubscriber = new TestSubscriber<>();
        cognitoManager.getCognitoCachingCredentialsProvider().subscribe(testSubscriber);

        testSubscriber.assertValue(Optional.<CognitoCachingCredentialsProvider>absent());
        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void initializeAndGetCognitoCachingCredentialsProviderWhenLoggedIn() {
        final BehaviorSubject<Boolean> isLoggedInStream = BehaviorSubject.create(true);
        when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
        cognitoManager.initialize();

        final TestSubscriber<Optional<CognitoCachingCredentialsProvider>> testSubscriber = new TestSubscriber<>();
        cognitoManager.getCognitoCachingCredentialsProvider().subscribe(testSubscriber);
        testSubscriber.assertValueCount(1);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        assertTrue(cognitoManager.getCognitoCachingCredentialsProvider().toBlocking().first().isPresent());
    }

    @Test
    public void initializeAndGetCognitoCachingCredentialsProviderWhenLoggedInAfter() {
        final BehaviorSubject<Boolean> isLoggedInStream = BehaviorSubject.create(false);
        when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
        cognitoManager.initialize();

        final TestSubscriber<Optional<CognitoCachingCredentialsProvider>> testSubscriber = new TestSubscriber<>();
        cognitoManager.getCognitoCachingCredentialsProvider().subscribe(testSubscriber);
        testSubscriber.assertValue(Optional.<CognitoCachingCredentialsProvider>absent());
        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();

        isLoggedInStream.onNext(true);

        testSubscriber.assertValueCount(2);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        assertTrue(cognitoManager.getCognitoCachingCredentialsProvider().toBlocking().first().isPresent());
    }

    @Test
    public void initializeCallsPrefetchCognitoTokenIfNeeded() {
        cognitoManager.initialize();
        verify(cognitoIdentityProvider).prefetchCognitoTokenIfNeeded();
    }

    @Test
    public void getCognitoCachingCredentialsProviderReDrivesCallsPrefetchCognitoTokenOnFailure() {
        cognitoManager.initialize();
        when(cognitoIdentityProvider.prefetchCognitoTokenIfNeeded()).thenReturn(Single.<Cognito>error(new Exception("Test")));
        verify(cognitoIdentityProvider).prefetchCognitoTokenIfNeeded();

        when(cognitoIdentityProvider.prefetchCognitoTokenIfNeeded()).thenReturn(Single.just(cognito));
        final TestSubscriber<Optional<CognitoCachingCredentialsProvider>> testSubscriber = new TestSubscriber<>();
        cognitoManager.getCognitoCachingCredentialsProvider().subscribe(testSubscriber);
        verify(cognitoIdentityProvider).prefetchCognitoTokenIfNeeded();
    }

}