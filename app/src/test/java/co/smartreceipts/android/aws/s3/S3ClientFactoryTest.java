package co.smartreceipts.android.aws.s3;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import co.smartreceipts.android.aws.cognito.CognitoManager;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
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
        when(cognitoManager.getCognitoCachingCredentialsProvider()).thenReturn(Observable.just(cognitoCachingCredentialsProvider));
        s3ClientFactory = new S3ClientFactory(RuntimeEnvironment.application, cognitoManager);
    }

    @Test
    public void getAmazonS3() {
        final TestSubscriber<AmazonS3Client> subscriber = new TestSubscriber<>();
        s3ClientFactory.getAmazonS3().subscribe(subscriber);

        subscriber.assertCompleted();
        subscriber.assertNoErrors();

        final AmazonS3Client s3Client1 = s3ClientFactory.getAmazonS3().toBlocking().first();
        final AmazonS3Client s3Client2 = s3ClientFactory.getAmazonS3().toBlocking().first();
        assertNotNull(s3Client1);
        assertNotNull(s3Client2);
        assertEquals(s3Client1, s3Client2);
    }

    @Test
    public void getTransferUtility() {
        final TestSubscriber<TransferUtility> subscriber = new TestSubscriber<>();
        s3ClientFactory.getTransferUtility().subscribe(subscriber);

        subscriber.assertCompleted();
        subscriber.assertNoErrors();

        final TransferUtility transferUtility1 = s3ClientFactory.getTransferUtility().toBlocking().first();
        final TransferUtility transferUtility2 = s3ClientFactory.getTransferUtility().toBlocking().first();
        assertNotNull(transferUtility1);
        assertNotNull(transferUtility2);
        assertEquals(transferUtility1, transferUtility2);
    }

}