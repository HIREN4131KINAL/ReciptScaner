package co.smartreceipts.android.ocr;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.aws.s3.S3Manager;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.ocr.apis.OcrService;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.apis.model.RecognitionResponse;
import co.smartreceipts.android.ocr.apis.model.RecongitionRequest;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.android.ocr.push.OcrPushMessageReceiver;
import co.smartreceipts.android.ocr.push.OcrPushMessageReceiverFactory;
import co.smartreceipts.android.push.PushManager;
import co.smartreceipts.android.utils.Feature;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OcrInteractorTest {

    private static final String IMG_NAME = "123456789.jpg";
    private static final String ID = "id";

    // Class under test
    OcrInteractor ocrInteractor;

    Context context = RuntimeEnvironment.application;

    @Mock
    S3Manager s3Manager;

    @Mock
    IdentityManager identityManager;

    @Mock
    ServiceManager ocrServiceManager;

    @Mock
    PushManager pushManager;

    @Mock
    OcrPurchaseTracker ocrPurchaseTracker;

    @Mock
    OcrPushMessageReceiverFactory ocrPushMessageReceiverFactory;

    @Mock
    OcrPushMessageReceiver pushMessageReceiver;

    @Mock
    Feature ocrFeature;

    @Mock
    File file;

    @Mock
    OcrService ocrService;

    @Mock
    RecognitionResponse recognitionResponse;

    @Mock
    RecognitionResponse.Recognition recognition;

    @Mock
    RecognitionResponse.RecognitionData recognitionData;

    @Mock
    OcrResponse ocrResponse;

    TestSubscriber<OcrResponse> testSubscriber;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testSubscriber = new TestSubscriber<>();

        when(ocrFeature.isEnabled()).thenReturn(true);
        when(identityManager.isLoggedIn()).thenReturn(true);
        when(ocrPurchaseTracker.hasAvailableScans()).thenReturn(true);
        when(ocrPushMessageReceiverFactory.get()).thenReturn(pushMessageReceiver);
        when(s3Manager.upload(file, "ocr/")).thenReturn(Observable.just("https://aws.amazon.com/smartreceipts/ocr/" + IMG_NAME));
        when(ocrServiceManager.getService(OcrService.class)).thenReturn(ocrService);
        when(recognitionResponse.getRecognition()).thenReturn(recognition);
        when(recognition.getId()).thenReturn(ID);
        when(recognition.getData()).thenReturn(recognitionData);
        when(recognitionData.getRecognitionData()).thenReturn(ocrResponse);
        when(ocrService.scanReceipt(new RecongitionRequest("ocr/" + IMG_NAME))).thenReturn(Observable.just(recognitionResponse));
        when(pushMessageReceiver.getOcrPushResponse()).thenReturn(Observable.just(new Object()));
        when(ocrService.getRecognitionResult(ID)).thenReturn(Observable.just(recognitionResponse));

        ocrInteractor = new OcrInteractor(context, s3Manager, identityManager, ocrServiceManager, pushManager, ocrPurchaseTracker, ocrPushMessageReceiverFactory, ocrFeature);
    }

    @Test
    public void scanWhenFeatureIsDisabled() {
        when(ocrFeature.isEnabled()).thenReturn(false);
        ocrInteractor.scan(file).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(new OcrResponse());
        testSubscriber.onCompleted();
        testSubscriber.assertNoErrors();
        verifyZeroInteractions(s3Manager, ocrServiceManager, pushManager, pushMessageReceiver);
    }

    @Test
    public void scanWhenNotLoggedIn() {
        when(identityManager.isLoggedIn()).thenReturn(false);
        ocrInteractor.scan(file).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(new OcrResponse());
        testSubscriber.onCompleted();
        testSubscriber.assertNoErrors();
        verifyZeroInteractions(s3Manager, ocrServiceManager, pushManager, pushMessageReceiver);
    }

    @Test
    public void scanWithNoAvailableScans() {
        when(ocrPurchaseTracker.hasAvailableScans()).thenReturn(false);
        ocrInteractor.scan(file).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(new OcrResponse());
        testSubscriber.onCompleted();
        testSubscriber.assertNoErrors();
        verifyZeroInteractions(s3Manager, ocrServiceManager, pushManager, pushMessageReceiver);
    }

    @Test
    public void scanButS3UploadFails() {
        when(s3Manager.upload(file, "ocr/")).thenReturn(Observable.<String>error(new Exception("test")));
        ocrInteractor.scan(file).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(new OcrResponse());
        testSubscriber.onCompleted();
        testSubscriber.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
        verify(ocrService, never()).scanReceipt(any(RecongitionRequest.class));
        verify(ocrService, never()).getRecognitionResult(anyString());
        verify(pushMessageReceiver, never()).getOcrPushResponse();
        verifyZeroInteractions(ocrServiceManager);
    }

    @Test
    public void scanButS3ReturnsUnexpectedUrl() {
        when(s3Manager.upload(file, "ocr/")).thenReturn(Observable.just("https://test.com"));
        ocrInteractor.scan(file).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(new OcrResponse());
        testSubscriber.onCompleted();
        testSubscriber.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
        verify(ocrService, never()).scanReceipt(any(RecongitionRequest.class));
        verify(ocrService, never()).getRecognitionResult(anyString());
        verify(pushMessageReceiver, never()).getOcrPushResponse();
        verifyZeroInteractions(ocrServiceManager);
    }

    @Test
    public void scanButRecognitionRequestFails() {
        when(ocrService.scanReceipt(new RecongitionRequest("ocr/" + IMG_NAME))).thenReturn(Observable.<RecognitionResponse>error(new Exception("test")));
        ocrInteractor.scan(file).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(new OcrResponse());
        testSubscriber.onCompleted();
        testSubscriber.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(ocrService).scanReceipt(new RecongitionRequest("ocr/" + IMG_NAME));
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
        verify(ocrService, never()).getRecognitionResult(anyString());
        verify(pushMessageReceiver, never()).getOcrPushResponse();
    }

    @Test
    public void scanButRecognitionResponseIsInvalidWithNullId() {
        when(recognition.getId()).thenReturn(null);
        ocrInteractor.scan(file).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(new OcrResponse());
        testSubscriber.onCompleted();
        testSubscriber.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(ocrService).scanReceipt(new RecongitionRequest("ocr/" + IMG_NAME));
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
        verify(ocrService, never()).getRecognitionResult(anyString());
        verify(pushMessageReceiver, never()).getOcrPushResponse();
    }

    @Test
    public void scanButGetRecognitionResult() {
        when(ocrService.getRecognitionResult(ID)).thenReturn(Observable.<RecognitionResponse>error(new Exception("test")));
        ocrInteractor.scan(file).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(new OcrResponse());
        testSubscriber.onCompleted();
        testSubscriber.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(ocrService).scanReceipt(new RecongitionRequest("ocr/" + IMG_NAME));
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
    }

    @Test
    public void scanCompletes() {
        ocrInteractor.scan(file).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(ocrResponse);
        testSubscriber.onCompleted();
        testSubscriber.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(ocrService).scanReceipt(new RecongitionRequest("ocr/" + IMG_NAME));
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
    }

    @Test
    public void scanCompletesEvenIfPushMessageTimesOutStillContinuesProcessing() {
        when(pushMessageReceiver.getOcrPushResponse()).thenReturn(Observable.error(new Exception("timeout")));
        ocrInteractor.scan(file).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue(ocrResponse);
        testSubscriber.onCompleted();
        testSubscriber.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(ocrService).scanReceipt(new RecongitionRequest("ocr/" + IMG_NAME));
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
    }

}