package co.smartreceipts.android.aws.s3;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
public class S3KeyGeneratorFactoryTest {

    @Test
    public void getAmazonS3() {
        final S3KeyGeneratorFactory factory = new S3KeyGeneratorFactory();
        final TestSubscriber<S3KeyGenerator> subscriber = new TestSubscriber<>();
        factory.get().subscribe(subscriber);

        subscriber.assertCompleted();
        subscriber.assertNoErrors();

        final S3KeyGenerator generator1 = factory.get().toBlocking().first();
        final S3KeyGenerator generator2 = factory.get().toBlocking().first();
        assertNotNull(generator1);
        assertNotNull(generator2);
        assertEquals(generator1, generator2);
    }

}