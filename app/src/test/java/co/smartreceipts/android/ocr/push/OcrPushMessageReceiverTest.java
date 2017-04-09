package co.smartreceipts.android.ocr.push;

import android.os.Bundle;

import com.google.firebase.messaging.RemoteMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Constructor;

import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

@RunWith(RobolectricTestRunner.class)
public class OcrPushMessageReceiverTest {

    // Class under test
    OcrPushMessageReceiver ocrPushMessageReceiver;

    @Before
    public void setUp() throws Exception {
        ocrPushMessageReceiver = new OcrPushMessageReceiver(Schedulers.immediate());
    }

    @Test
    public void onMessageReceivedTriggersObservable() throws Exception {
        final TestSubscriber<Object> testSubscriber = new TestSubscriber<>();
        ocrPushMessageReceiver.getOcrPushResponse().subscribe(testSubscriber);

        final Constructor<RemoteMessage> constructor = RemoteMessage.class.getDeclaredConstructor(Bundle.class);
        constructor.setAccessible(true);
        final RemoteMessage message = constructor.newInstance(new Bundle());
        ocrPushMessageReceiver.onMessageReceived(message);

        testSubscriber.assertValueCount(1);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

}