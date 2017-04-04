package co.smartreceipts.android.imports;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.utils.UriUtils;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Subscriber;
import wb.android.storage.StorageManager;

public class GenericFileImportProcessor implements FileImportProcessor {

    private final Trip trip;
    private final StorageManager storageManner;
    private final ContentResolver contentResolver;

    public GenericFileImportProcessor(@NonNull Trip trip, @NonNull StorageManager storageManager, @NonNull Context context) {
        this(trip, storageManager, context.getContentResolver());
    }

    public GenericFileImportProcessor(@NonNull Trip trip, @NonNull StorageManager storageManager, @NonNull ContentResolver contentResolver) {
        this.trip = Preconditions.checkNotNull(trip);
        this.storageManner = Preconditions.checkNotNull(storageManager);
        this.contentResolver = Preconditions.checkNotNull(contentResolver);
    }

    @NonNull
    @Override
    public Observable<File> process(@NonNull final Uri uri) {
        Logger.info(GenericFileImportProcessor.this, "Attempting to import: {}", uri);
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                InputStream inputStream = null;
                try {
                    inputStream = contentResolver.openInputStream(uri);
                    final File destination = storageManner.getFile(trip.getDirectory(), System.currentTimeMillis() + "." + UriUtils.getExtension(uri, contentResolver));
                    if (storageManner.copy(inputStream, destination, true)) {
                        subscriber.onNext(destination);
                        subscriber.onCompleted();
                        Logger.info(GenericFileImportProcessor.this, "Successfully copied Uri to the Smart Receipts directory");
                    } else {
                        subscriber.onError(new FileNotFoundException());
                    }
                } catch (IOException e) {
                    Logger.error(GenericFileImportProcessor.this, "Failed to import uri", e);
                    subscriber.onError(e);
                } finally {
                    StorageManager.closeQuietly(inputStream);
                }
            }
        });
    }

}
