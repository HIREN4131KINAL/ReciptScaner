package co.smartreceipts.android.imports;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import rx.Observable;

public class AutoFailImportProcessor implements FileImportProcessor {
    @NonNull
    @Override
    public Observable<File> process(@NonNull Uri uri) {
        return Observable.error(new UnsupportedOperationException());
    }
}
