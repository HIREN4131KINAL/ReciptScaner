package co.smartreceipts.android.imports;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileNotFoundException;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ActivityFileResultImporterTest {

    // Class under test
    ActivityFileResultImporter fileResultImporter;

    @Mock
    Context context;

    @Mock
    ContentResolver contentResolver;

    @Mock
    FileImportProcessorFactory factory;

    @Mock
    FileImportProcessor processor;

    @Mock
    Analytics analytics;

    @Mock
    Intent intent;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(context.getApplicationContext()).thenReturn(context);
        when(context.getContentResolver()).thenReturn(contentResolver);
        when(factory.get(anyInt())).thenReturn(processor);
        FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class).create().get();

        fileResultImporter = new ActivityFileResultImporter(context, activity.getSupportFragmentManager(), factory, analytics, Schedulers.immediate(), Schedulers.immediate());
    }

    @Test
    public void onActivityResultCancelled() {
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        fileResultImporter.getResultStream().subscribe(testSubscriber);
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_CANCELED, null, null);

        testSubscriber.assertNoValues();
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void onActivityResultCancelledWithIndependentOrdering() {
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_CANCELED, null, null);
        fileResultImporter.getResultStream().subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void onActivityResultWithNullIntentAndNullLocation() {
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        fileResultImporter.getResultStream().subscribe(testSubscriber);
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, null, null);

        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(FileNotFoundException.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithNullIntentAndNullLocationWithIndependentOrdering() {
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, null, null);
        fileResultImporter.getResultStream().subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(FileNotFoundException.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithIntentNullDataAndNullLocation() {
        when(intent.getData()).thenReturn(null);
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        fileResultImporter.getResultStream().subscribe(testSubscriber);
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, intent, null);

        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(FileNotFoundException.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithIntentNullDataAndNullLocationWithIndependentOrdering() {
        when(intent.getData()).thenReturn(null);
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, intent, null);
        fileResultImporter.getResultStream().subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(FileNotFoundException.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithProcessingFailure() {
        final Uri uri = Uri.EMPTY;
        when(intent.getData()).thenReturn(uri);
        when(processor.process(uri)).thenReturn(Observable.<File>error(new Exception("Test")));
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        fileResultImporter.getResultStream().subscribe(testSubscriber);
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, intent, null);

        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(Exception.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithProcessingFailureWithIndependentOrdering() {
        final Uri uri = Uri.EMPTY;
        when(intent.getData()).thenReturn(uri);
        when(processor.process(uri)).thenReturn(Observable.<File>error(new Exception("Test")));
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, intent, null);
        fileResultImporter.getResultStream().subscribe(testSubscriber);

        testSubscriber.assertNoValues();
        testSubscriber.assertNotCompleted();
        testSubscriber.assertError(Exception.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithValidIntent() {
        final Uri uri = Uri.EMPTY;
        final File file = new File("");
        final int requestCode = RequestCodes.IMPORT_GALLERY_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(intent.getData()).thenReturn(uri);
        when(processor.process(uri)).thenReturn(Observable.just(file));
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        fileResultImporter.getResultStream().subscribe(testSubscriber);
        fileResultImporter.onActivityResult(requestCode, responseCode, intent, null);

        testSubscriber.assertValue(new ActivityFileResultImporterResponse(file, requestCode, responseCode));
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void onActivityResultWithValidIntentWithIndependentOrdering() {
        final Uri uri = Uri.EMPTY;
        final File file = new File("");
        final int requestCode = RequestCodes.IMPORT_GALLERY_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(intent.getData()).thenReturn(uri);
        when(processor.process(uri)).thenReturn(Observable.just(file));
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(requestCode, responseCode, intent, null);
        fileResultImporter.getResultStream().subscribe(testSubscriber);

        testSubscriber.assertValue(new ActivityFileResultImporterResponse(file, requestCode, responseCode));
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void onActivityResultWithValidSaveLocation() {
        final Uri uri = Uri.EMPTY;
        final File file = new File("");
        final int requestCode = RequestCodes.IMPORT_GALLERY_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(processor.process(uri)).thenReturn(Observable.just(file));
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        fileResultImporter.getResultStream().subscribe(testSubscriber);
        fileResultImporter.onActivityResult(requestCode, responseCode, null, uri);

        testSubscriber.assertValue(new ActivityFileResultImporterResponse(file, requestCode, responseCode));
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void onActivityResultWithValidSaveLocationWithIndependentOrdering() {
        final Uri uri = Uri.EMPTY;
        final File file = new File("");
        final int requestCode = RequestCodes.IMPORT_GALLERY_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(processor.process(uri)).thenReturn(Observable.just(file));
        final TestSubscriber<ActivityFileResultImporterResponse> testSubscriber = new TestSubscriber<>();

        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(requestCode, responseCode, null, uri);
        fileResultImporter.getResultStream().subscribe(testSubscriber);

        testSubscriber.assertValue(new ActivityFileResultImporterResponse(file, requestCode, responseCode));
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

}