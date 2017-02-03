package co.smartreceipts.android.imports;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.ocr.OcrInteractor;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

public class ActivityFileResultImporter {

    private static final String TAG = ActivityFileResultImporter.class.getSimpleName();

    private final Context context;
    private final FileImportProcessorFactory factory;
    private final Analytics analytics;
    private final ActivityImporterHeadlessFragment headlessFragment;
    private final OcrInteractor ocrInteractor;
    private final Scheduler subscribeOnScheduler;
    private final Scheduler observeOnScheduler;

    public ActivityFileResultImporter(@NonNull Context context, @NonNull FragmentManager fragmentManager, @NonNull Trip trip, 
                                      @NonNull PersistenceManager persistenceManager, @NonNull Analytics analytics) {
        this(context, fragmentManager, new FileImportProcessorFactory(context, trip, persistenceManager), analytics, new OcrInteractor(context), Schedulers.io(), AndroidSchedulers.mainThread());
    }

    public ActivityFileResultImporter(@NonNull Context context, @NonNull FragmentManager fragmentManager, @NonNull FileImportProcessorFactory factory,
                                      @NonNull Analytics analytics, @NonNull OcrInteractor ocrInteractor,
                                      @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.factory = Preconditions.checkNotNull(factory);
        this.analytics = Preconditions.checkNotNull(analytics);
        this.ocrInteractor = Preconditions.checkNotNull(ocrInteractor);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        this.observeOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        
        Preconditions.checkNotNull(fragmentManager);
        ActivityImporterHeadlessFragment headlessFragment = (ActivityImporterHeadlessFragment) fragmentManager.findFragmentByTag(TAG);
        if (headlessFragment == null) {
            headlessFragment = new ActivityImporterHeadlessFragment();
            fragmentManager.beginTransaction().add(headlessFragment, TAG).commit();
        }
        this.headlessFragment = headlessFragment;
        if (this.headlessFragment.importSubject == null) {
            headlessFragment.importSubject = ReplaySubject.create(1);
        }
    }

    public void onActivityResult(final int requestCode, final int resultCode, @Nullable Intent data, @Nullable final Uri proposedImageSaveLocation) {
        Logger.info(this, "Performing import of onActivityResult data: {}", data);

        if (headlessFragment.localSubscription != null) {
            Logger.warn(this, "Clearing cached local subscription, a previous request was never fully completed");
            headlessFragment.localSubscription.unsubscribe();
            headlessFragment.localSubscription = null;
        }
        headlessFragment.localSubscription = getSaveLocation(requestCode, resultCode, data, proposedImageSaveLocation)
                .subscribeOn(subscribeOnScheduler)
                .flatMap(new Func1<Uri, Observable<File>>() {
                    @Override
                    public Observable<File> call(@NonNull final Uri uri) {
                        return factory.get(requestCode).process(uri);
                    }
                })
                .flatMap(new Func1<File, Observable<ActivityFileResultImporterResponse>>() {
                    @Override
                    public Observable<ActivityFileResultImporterResponse> call(final File file) {
                        return ocrInteractor.scan(file)
                                .map(new Func1<OcrResponse, ActivityFileResultImporterResponse>() {
                                    @Override
                                    public ActivityFileResultImporterResponse call(OcrResponse ocrResponse) {
                                        return new ActivityFileResultImporterResponse(file, ocrResponse, requestCode, resultCode);
                                    }
                                });
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.error(ActivityFileResultImporter.this, "Failed to save import result", throwable);
                        analytics.record(new ErrorEvent(ActivityFileResultImporter.this, throwable));
                    }
                })
                .observeOn(observeOnScheduler)
                .subscribe(headlessFragment.importSubject);
    }

    public Observable<ActivityFileResultImporterResponse> getResultStream() {
        return headlessFragment.importSubject.asObservable();
    }

    public void dispose() {
        if (headlessFragment.localSubscription != null) {
            headlessFragment.localSubscription.unsubscribe();
            headlessFragment.localSubscription = null;
        }
        if (headlessFragment.importSubject != null) {
            this.headlessFragment.importSubject = ReplaySubject.create(1);
        }
    }

    public static final class ActivityImporterHeadlessFragment extends Fragment {
        
        private Subject<ActivityFileResultImporterResponse, ActivityFileResultImporterResponse> importSubject = ReplaySubject.create(1);
        private Subscription localSubscription;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

    private Observable<Uri> getSaveLocation(final int requestCode, final int resultCode, @Nullable final Intent data, @Nullable final Uri proposedImageSaveLocation) {
        return Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(Subscriber<? super Uri> subscriber) {
                if (resultCode == Activity.RESULT_OK) {
                    if ((data == null || data.getData() == null) && proposedImageSaveLocation == null) {
                        subscriber.onError(new FileNotFoundException("Unknown intent data and proposed save location for request " + requestCode + " with result " + resultCode));
                    } else {
                        final Uri uri;
                        if (data != null && data.getData() != null) {
                            uri = data.getData();
                        } else {
                            uri = proposedImageSaveLocation;
                        }

                        if (uri == null) {
                            subscriber.onError(new FileNotFoundException("Null Uri for request " + requestCode + " with result " + resultCode));
                        } else {
                            Logger.info(ActivityFileResultImporter.this, "Image save location determined as {}", uri);
                            subscriber.onNext(uri);
                            subscriber.onCompleted();
                        }
                    }
                } else {
                    Logger.warn(ActivityFileResultImporter.this, "Unknown activity result code (likely user cancelled): {} ", resultCode);
                    subscriber.onCompleted();
                }
            }
        });
    }

}
