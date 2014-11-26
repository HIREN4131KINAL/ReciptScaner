package co.smartreceipts.android.sync.response;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import co.smartreceipts.android.sync.request.SyncRequest;

/**
 * This is used to encapsulate error data that may occur during a sync operation
 * 
 * @author Will Baumann
 */
public interface SyncError extends Parcelable {

    /**
     * @return - the {@link co.smartreceipts.android.sync.request.SyncRequest} that was used to
     * produce this error.
     */
    @NonNull
    SyncRequest<?> getRequest();

    /**
     * @return - the {@link java.lang.String} containing the error message
     */
    @NonNull
    String getErrorMessage();

}
