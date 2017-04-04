package co.smartreceipts.android.aws.cognito;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.apis.me.Cognito;
import rx.Single;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

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
        cognitoManager = new CognitoManager(context, identityManager, cognitoIdentityProvider, Schedulers.immediate());
    }

    @Test
    public void initializeWhenNotLoggedIn() {
        final BehaviorSubject<Boolean> isLoggedInStream = BehaviorSubject.create(false);
        when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
        cognitoManager.initialize();

        final TestSubscriber<CognitoCachingCredentialsProvider> testSubscriber = new TestSubscriber<>();
        cognitoManager.getCognitoCachingCredentialsProvider().subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void initializeWhenLoggedIn() {
        final BehaviorSubject<Boolean> isLoggedInStream = BehaviorSubject.create(true);
        when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
        cognitoManager.initialize();

        final TestSubscriber<CognitoCachingCredentialsProvider> testSubscriber = new TestSubscriber<>();
        cognitoManager.getCognitoCachingCredentialsProvider().subscribe(testSubscriber);

        testSubscriber.assertValueCount(1);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void initializeWhenLoggedInAfter() {
        final BehaviorSubject<Boolean> isLoggedInStream = BehaviorSubject.create(false);
        when(identityManager.isLoggedInStream()).thenReturn(isLoggedInStream);
        cognitoManager.initialize();

        final TestSubscriber<CognitoCachingCredentialsProvider> testSubscriber = new TestSubscriber<>();
        cognitoManager.getCognitoCachingCredentialsProvider().subscribe(testSubscriber);

        isLoggedInStream.onNext(true);

        testSubscriber.assertValueCount(1);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

}