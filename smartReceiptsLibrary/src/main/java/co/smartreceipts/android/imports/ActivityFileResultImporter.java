package co.smartreceipts.android.imports;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import co.smartreceipts.android.fragments.ImportPhotoPdfDialogFragment;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import wb.android.google.camera.PhotoModule;
import wb.android.google.camera.data.Log;
import wb.android.storage.StorageManager;

public class ActivityFileResultImporter {

    private static final String TAG = ActivityFileResultImporter.class.getSimpleName();

    private final Context mContext;
    private final Trip mTrip;
    private final StorageManager mStorageManager;
    private final Preferences mPreferences;

    public ActivityFileResultImporter(@NonNull Context context, @NonNull Trip trip, @NonNull PersistenceManager persistenceManager) {
        this(context, trip, persistenceManager.getStorageManager(), persistenceManager.getPreferences());
    }

    public ActivityFileResultImporter(@NonNull Context context, @NonNull Trip trip, @NonNull StorageManager storageManager, @NonNull Preferences preferences) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mTrip = Preconditions.checkNotNull(trip);
        mStorageManager = Preconditions.checkNotNull(storageManager);
        mPreferences = Preconditions.checkNotNull(preferences);
    }

    public void onActivityResult(final int requestCode, final int resultCode, @Nullable Intent data, @Nullable final Uri proposedImageSaveLocation, @NonNull final FileImportListener listener) {
        if (resultCode == Activity.RESULT_OK) {
            if ((data == null || data.getData() == null) && proposedImageSaveLocation == null) {
                listener.onImportFailed(null, requestCode, resultCode);
            } else {
                final Uri uri;
                if (data != null && data.getData() != null) {
                    uri = data.getData();
                } else {
                    uri = proposedImageSaveLocation;
                }
                final FileImportProcessor importProcessor;
                if (RequestCodes.PHOTO_REQUESTS.contains(requestCode)) {
                    importProcessor = new ImageImportProcessor(mTrip, mStorageManager, mPreferences, mContext);
                } else if (RequestCodes.PDF_REQUESTS.contains(requestCode)) {
                    importProcessor = new GenericFileImportProcessor(mTrip, mStorageManager, mContext);
                } else {
                    importProcessor = new AutoFailImportProcessor();
                }

                final AtomicReference<Subscription> subscriptionReference = new AtomicReference<>();
                subscriptionReference.set(importProcessor.process(uri)
                                     .subscribeOn(Schedulers.io())
                                     .observeOn(AndroidSchedulers.mainThread())
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
                                     })
                                     .subscribe(new Action1<File>() {
                                         @Override
                                         public void call(File file) {
                                             if (file != null) {
                                                 listener.onImportSuccess(file, requestCode, resultCode);
                                             } else {
                                                 listener.onImportFailed(null, requestCode, resultCode);
                                             }
                                         }
                                     }, new Action1<Throwable>() {
                                         @Override
                                         public void call(Throwable throwable) {
                                             listener.onImportFailed(throwable, requestCode, resultCode);
                                             subscriptionReference.get().unsubscribe();
                                         }
                                     }, new Action0() {
                                         @Override
                                         public void call() {
                                             subscriptionReference.get().unsubscribe();
                                         }
                                     }));

            }
        } else if (resultCode == PhotoModule.RESULT_SAVE_FAILED) {
            listener.onImportFailed(null, requestCode, resultCode);
        } else {
            Log.w(TAG, "Unknown activity result code (likely user cancelled) - " + resultCode);
        }
    }

}
