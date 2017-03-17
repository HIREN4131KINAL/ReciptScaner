package co.smartreceipts.android.imports;

import android.content.ContentResolver;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import co.smartreceipts.android.model.Trip;
import rx.observers.TestSubscriber;
import wb.android.storage.StorageManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class GenericFileImportProcessorTest {

    GenericFileImportProcessor importProcessor;

    @Mock
    Trip trip;

    @Mock
    StorageManager storageManner;

    @Mock
    ContentResolver contentResolver;

    @Mock
    Uri uri;

    @Mock
    File file;

    @Mock
    InputStream inputStream;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(uri.getScheme()).thenReturn(ContentResolver.SCHEME_CONTENT);
        when(contentResolver.getType(uri)).thenReturn("application/pdf");
        when(storageManner.getFile(any(File.class), anyString())).thenReturn(file);

        importProcessor = new GenericFileImportProcessor(trip, storageManner, contentResolver);
    }

    @Test
    public void processThrowsFileNotFoundException() throws Exception {
        when(contentResolver.openInputStream(uri)).thenThrow(new FileNotFoundException("Test"));

        final TestSubscriber<File> testSubscriber = new TestSubscriber<>();
        importProcessor.process(uri).subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(FileNotFoundException.class);
    }

    @Test
    public void processFailsToCopy() throws Exception {
        when(contentResolver.openInputStream(uri)).thenReturn(inputStream);
        when(storageManner.copy(inputStream, file, true)).thenReturn(false);

        final TestSubscriber<File> testSubscriber = new TestSubscriber<>();
        importProcessor.process(uri).subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(FileNotFoundException.class);
    }

    @Test
    public void processSuccess() throws Exception {
        when(contentResolver.openInputStream(uri)).thenReturn(inputStream);
        when(storageManner.copy(inputStream, file, true)).thenReturn(true);

        final TestSubscriber<File> testSubscriber = new TestSubscriber<>();
        importProcessor.process(uri).subscribe(testSubscriber);

        testSubscriber.assertValue(file);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

}