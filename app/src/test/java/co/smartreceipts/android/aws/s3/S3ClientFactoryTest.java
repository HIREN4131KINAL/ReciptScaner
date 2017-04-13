package co.smartreceipts.android.aws.s3;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.hadisatrio.optional.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import co.smartreceipts.android.aws.cognito.CognitoManager;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.BehaviorSubject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class S3ClientFactoryTest {

    // Class under test
    S3ClientFactory s3ClientFactory;

    @Mock
    CognitoManager cognitoManager;

    @Mock
    CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(cognitoManager.getCognitoCachingCredentialsProvider()).thenReturn(Observable.just(Optional.of(cognitoCachingCredentialsProvider)));
        s3ClientFactory = new S3ClientFactory(RuntimeEnvironment.application, cognitoManager);
    }

    @Test
    public void getAmazonS3() {
        final TestSubscriber<Optional<AmazonS3Client>> subscriber = new TestSubscriber<>();
        s3ClientFactory.getAmazonS3().subscribe(subscriber);

        subscriber.assertCompleted();
        subscriber.assertNoErrors();

        final Optional<AmazonS3Client> s3Client = s3ClientFactory.getAmazonS3().toBlocking().first();
        assertTrue(s3Client.isPresent());
    }

    @Test
    public void getAmazonS3WhenInitiallyAbsent() {
        final BehaviorSubject<Optional<CognitoCachingCredentialsProvider>> subject = BehaviorSubject.create();
        when(cognitoManager.getCognitoCachingCredentialsProvider()).thenReturn(subject);

        subject.onNext(Optional.<CognitoCachingCredentialsProvider>absent());
        final TestSubscriber<Optional<AmazonS3Client>> subscriber = new TestSubscriber<>();
        s3ClientFactory.getAmazonS3().subscribe(subscriber);

        subscriber.assertValueCount(1);
        subscriber.assertNotCompleted();
        subscriber.assertNoErrors();

        final Optional<AmazonS3Client> s3Client1 = s3ClientFactory.getAmazonS3().toBlocking().first();
        assertFalse(s3Client1.isPresent());

        // Now re-drive with an actual value:
        subject.onNext(Optional.of(cognitoCachingCredentialsProvider));

        subscriber.assertCompleted();
        subscriber.assertNoErrors();

        final Optional<AmazonS3Client> s3Client2 = s3ClientFactory.getAmazonS3().toBlocking().first();
        assertTrue(s3Client2.isPresent());
    }
}