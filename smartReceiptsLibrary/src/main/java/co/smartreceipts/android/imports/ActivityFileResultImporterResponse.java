package co.smartreceipts.android.imports;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.File;

public class ActivityFileResultImporterResponse {

    private final File file;
    private final int requestCode;
    private final int resultCode;

    public ActivityFileResultImporterResponse(@NonNull File file, int requestCode, int resultCode) {
        this.file = Preconditions.checkNotNull(file);
        this.requestCode = requestCode;
        this.resultCode = resultCode;
    }

    /**
     * @return the resultant file that was imported
     */
    @NonNull
    public File getFile() {
        return file;
    }

    /**
     * @return the request code that triggered the import
     */
    public int getRequestCode() {
        return requestCode;
    }

    /**
     * @return the result code that from the response
     */
    public int getResultCode() {
        return resultCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActivityFileResultImporterResponse)) return false;

        ActivityFileResultImporterResponse that = (ActivityFileResultImporterResponse) o;

        if (requestCode != that.requestCode) return false;
        if (resultCode != that.resultCode) return false;
        return file.equals(that.file);

    }

    @Override
    public int hashCode() {
        int result = file.hashCode();
        result = 31 * result + requestCode;
        result = 31 * result + resultCode;
        return result;
    }

    @Override
    public String toString() {
        return "ActivityFileResultImporterResponse{" +
                "file=" + file +
                ", requestCode=" + requestCode +
                ", resultCode=" + resultCode +
                '}';
    }
}
