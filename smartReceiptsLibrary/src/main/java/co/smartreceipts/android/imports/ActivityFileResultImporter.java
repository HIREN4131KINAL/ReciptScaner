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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;
import wb.android.google.camera.PhotoModule;
import wb.android.google.camera.data.Log;
import wb.android.storage.StorageManager;

public class ActivityFileResultImporter {

    private static final String TAG = ActivityFileResultImporter.class.getSimpleName();

    private final Context mContext;
    private final ActivityImporterHeadlessFragment mHeadlessFragment;
    private final Trip mTrip;
    private final StorageManager mStorageManager;
    private final Preferences mPreferences;
    private final Analytics mAnalytics;

    public ActivityFileResultImporter(@NonNull Context context, @NonNull FragmentManager fragmentManager, @NonNull Trip trip, 
                                      @NonNull PersistenceManager persistenceManager, @NonNull Analytics analytics) {
        this(context, fragmentManager, trip, persistenceManager.getStorageManager(), persistenceManager.getPreferences(), analytics);
    }

    public ActivityFileResultImporter(@NonNull Context context, @NonNull FragmentManager fragmentManager, @NonNull Trip trip, 
                                      @NonNull StorageManager storageManager, @NonNull Preferences preferences, @NonNull Analytics analytics) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mTrip = Preconditions.checkNotNull(trip);
        mStorageManager = Preconditions.checkNotNull(storageManager);
        mPreferences = Preconditions.checkNotNull(preferences);
        mAnalytics = Preconditions.checkNotNull(analytics);
        
        Preconditions.checkNotNull(fragmentManager);
        ActivityImporterHeadlessFragment headlessFragment = (ActivityImporterHeadlessFragment) fragmentManager.findFragmentByTag(TAG);
        if (headlessFragment == null) {
            headlessFragment = new ActivityImporterHeadlessFragment();
            fragmentManager.beginTransaction().add(headlessFragment, TAG).commit();
        }
        mHeadlessFragment = headlessFragment;
    }

    public void onActivityResult(final int requestCode, final int resultCode, @Nullable Intent data, @Nullable final Uri proposedImageSaveLocation) {
        Logger.info(this, "Performing import of onActivityResult data: {}", data);

        if (mHeadlessFragment.localSubscription != null) {
            Logger.warn(this, "Clearing cached local subscription, a previous request was never fully completed");
            mHeadlessFragment.localSubscription.unsubscribe();
            mHeadlessFragment.localSubscription = null;
        }
        mHeadlessFragment.importSubject = BehaviorSubject.create();
        mHeadlessFragment.localSubscription = getSaveLocation(requestCode, resultCode, data, proposedImageSaveLocation)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<Uri, Observable<File>>() {
                    @Override
                    public Observable<File> call(@NonNull final Uri uri) {
                        final FileImportProcessor importProcessor;
                        if (RequestCodes.PHOTO_REQUESTS.contains(requestCode)) {
                            importProcessor = new ImageImportProcessor(mTrip, mStorageManager, mPreferences, mContext);
                        } else if (RequestCodes.PDF_REQUESTS.contains(requestCode)) {
                            importProcessor = new GenericFileImportProcessor(mTrip, mStorageManager, mContext);
                        } else {
                            importProcessor = new AutoFailImportProcessor();
                        }
                        return importProcessor.process(uri)
                                .doOnNext(new Action1<File>() {
                                    @Override
                                    @SuppressWarnings("ResultOfMethodCallIgnored")
                                    public void call(File file) {
                                        if (file != null) {
                                            final File uriLocation = new File(uri.getPath());
                                            if (!file.equals(uriLocation)) {
                                                uriLocation.delete(); // Clean up
                                            }
                                        }
                                    }
                                });
                    }
                })
                .map(new Func1<File, ActivityFileResultImporterResponse>() {
                    @Override
                    public ActivityFileResultImporterResponse call(@NonNull File file) {
                        return new ActivityFileResultImporterResponse(file, requestCode, resultCode);
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.error(ActivityFileResultImporter.this, "Failed to save import result", throwable);
                        mAnalytics.record(new ErrorEvent(ActivityFileResultImporter.this, throwable));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mHeadlessFragment.importSubject);
    }

    public Observable<ActivityFileResultImporterResponse> getResultStream() {
        if (mHeadlessFragment.importSubject == null) {
            return Observable.empty();
        } else {
            return mHeadlessFragment.importSubject.asObservable();
        }
    }

    public void dispose() {
        if (mHeadlessFragment.localSubscription != null) {
            mHeadlessFragment.localSubscription.unsubscribe();
            mHeadlessFragment.localSubscription = null;
        }
        if (mHeadlessFragment.importSubject != null) {
            mHeadlessFragment.importSubject = null;
        }
    }

    public static final class ActivityImporterHeadlessFragment extends Fragment {
        
        private Subject<ActivityFileResultImporterResponse, ActivityFileResultImporterResponse> importSubject;
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
                } else if (resultCode == PhotoModule.RESULT_SAVE_FAILED) {
                    subscriber.onError(new FileNotFoundException("Failed to save request " + requestCode + " with result " + resultCode));
                } else {
                    Logger.warn(ActivityFileResultImporter.this, "Unknown activity result code (likely user cancelled): {} ", resultCode);
                    subscriber.onCompleted();
                }
            }
        });
    }

}
