package co.smartreceipts.android.imports;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public interface FileImportListener {

    /**
     * Called when we deem the file to have been successfully imported
     *
     * @param file the resultant file that was imported
     * @param requestCode the request code that triggered the import
     * @param resultCode the result code that trigged the response
     */
    void onImportSuccess(@NonNull File file, int requestCode, int resultCode);

    /**
     * Called when we failed to import the file (e.g. IO failure, user cancelled, etc.)
     *
     * @param e the {@link Throwable} that caused the failure or {@code null} if one did not occur
     * @param requestCode the request code that triggered the import
     * @param resultCode the result code that trigged the response
     */
    void onImportFailed(@Nullable Throwable e, int requestCode, int resultCode);
}
