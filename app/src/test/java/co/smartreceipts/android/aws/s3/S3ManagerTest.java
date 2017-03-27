package co.smartreceipts.android.aws.s3;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
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

import java.io.File;
import java.util.concurrent.Executors;

import co.smartreceipts.android.SameThreadExecutorService;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class S3ManagerTest {

    private static final String PATH = "path/";
    private static final String RANDOM_KEY = "key";
    private static final String FILE_NAME = "img.jpg";
    private static final String URL = "https://www.smartreceipts.co/download";

    // Class under test
    S3Manager s3Manager;

    @Mock
    S3ClientFactory s3ClientFactory;

    @Mock
    AmazonS3Client amazonS3Client;

    @Mock
    TransferUtility transferUtility;

    @Mock
    S3KeyGeneratorFactory keyGeneratorFactory;

    @Mock
    S3KeyGenerator keyGenerator;

    @Mock
    TransferObserver transferObserver;

    @Captor
    ArgumentCaptor<String> keyCaptor;

    @Captor
    ArgumentCaptor<TransferListener>  transferListenerCaptor;

    File file = new File(FILE_NAME);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(s3ClientFactory.getAmazonS3()).thenReturn(Observable.just(amazonS3Client));
        when(s3ClientFactory.getTransferUtility()).thenReturn(Observable.just(transferUtility));
        when(keyGeneratorFactory.get()).thenReturn(Observable.just(keyGenerator));
        when(keyGenerator.getS3Key()).thenReturn(RANDOM_KEY);
        when(transferUtility.upload(eq("smartreceipts"), keyCaptor.capture(), eq(file))).thenReturn(transferObserver);
        when(amazonS3Client.getResourceUrl("smartreceipts", PATH + RANDOM_KEY + FILE_NAME)).thenReturn(URL);

        s3Manager = new S3Manager(s3ClientFactory, keyGeneratorFactory, new SameThreadExecutorService());
    }

    @Test
    public void uploadFails() {
        final TestSubscriber<String> subscriber = new TestSubscriber<>();
        s3Manager.upload(file, PATH).subscribe(subscriber);

        final Exception exception = new Exception("test");
        verify(transferObserver).setTransferListener(transferListenerCaptor.capture());
        transferListenerCaptor.getValue().onError(-1, exception);

        subscriber.assertNoValues();
        subscriber.assertNotCompleted();
        subscriber.assertError(exception);
        verify(transferObserver).cleanTransferListener();
    }

    @Test
    public void uploadCancelled() {
        final TestSubscriber<String> subscriber = new TestSubscriber<>();
        s3Manager.upload(file, PATH).subscribe(subscriber);

        verify(transferObserver).setTransferListener(transferListenerCaptor.capture());
        transferListenerCaptor.getValue().onStateChanged(-1, TransferState.CANCELED);

        subscriber.assertNoValues();
        subscriber.assertNotCompleted();
        subscriber.assertError(Exception.class);
        verify(transferObserver).cleanTransferListener();
    }

    @Test
    public void uploadFailed() {
        final TestSubscriber<String> subscriber = new TestSubscriber<>();
        s3Manager.upload(file, PATH).subscribe(subscriber);

        verify(transferObserver).setTransferListener(transferListenerCaptor.capture());
        transferListenerCaptor.getValue().onStateChanged(-1, TransferState.FAILED);

        subscriber.assertNoValues();
        subscriber.assertNotCompleted();
        subscriber.assertError(Exception.class);
        verify(transferObserver).cleanTransferListener();
    }

    @Test
    public void uploadSuccess() {
        final TestSubscriber<String> subscriber = new TestSubscriber<>();
        s3Manager.upload(file, PATH).subscribe(subscriber);

        verify(transferObserver).setTransferListener(transferListenerCaptor.capture());
        transferListenerCaptor.getValue().onStateChanged(-1, TransferState.COMPLETED);

        subscriber.assertValue(URL);
        subscriber.assertCompleted();
        subscriber.assertNoErrors();
        verify(transferObserver).cleanTransferListener();
    }

}