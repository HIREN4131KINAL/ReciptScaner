package co.smartreceipts.android.imports;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;

import io.reactivex.Single;

public class AutoFailImportProcessor implements FileImportProcessor {
    @NonNull
    @Override
    public Single<File> process(@NonNull Uri uri) {
        return Single.error(new UnsupportedOperationException());
    }
}
