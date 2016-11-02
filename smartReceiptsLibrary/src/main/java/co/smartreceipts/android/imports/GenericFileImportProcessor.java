package co.smartreceipts.android.imports;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.utils.UriUtils;
import rx.Observable;
import rx.Subscriber;
import wb.android.storage.StorageManager;

public class GenericFileImportProcessor implements FileImportProcessor {

    private final Trip mTrip;
    private final StorageManager mStorageManner;
    private final ContentResolver mContentResolver;

    public GenericFileImportProcessor(@NonNull Trip trip, @NonNull StorageManager storageManager, @NonNull Context context) {
        this(trip, storageManager, context.getContentResolver());
    }

    public GenericFileImportProcessor(@NonNull Trip trip, @NonNull StorageManager storageManager, @NonNull ContentResolver contentResolver) {
        mTrip = Preconditions.checkNotNull(trip);
        mStorageManner = Preconditions.checkNotNull(storageManager);
        mContentResolver = Preconditions.checkNotNull(contentResolver);
    }

    @NonNull
    @Override
    public Observable<File> process(@NonNull final Uri uri) {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                InputStream inputStream = null;
                try {
                    inputStream = mContentResolver.openInputStream(uri);
                    final File destination = mStorageManner.getFile(mTrip.getDirectory(), System.currentTimeMillis() + "." + UriUtils.getExtension(uri, mContentResolver));
                    if (mStorageManner.copy(inputStream, destination, true)) {
                        subscriber.onNext(destination);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new FileNotFoundException());
                    }
                } catch (IOException e) {
                    subscriber.onError(e);
                } finally {
                    StorageManager.closeQuietly(inputStream);
                }
            }
        });
    }

}
