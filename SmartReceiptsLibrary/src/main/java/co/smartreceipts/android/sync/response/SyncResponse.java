package co.smartreceipts.android.sync.response;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import co.smartreceipts.android.sync.request.SyncRequest;
import co.smartreceipts.android.sync.request.SyncRequestType;

/**
 * Returns the response for a given request
 *
 * @author williambaumann
 */
public interface SyncResponse<T extends Parcelable> extends Parcelable {

    /**
     * @return - the {@link co.smartreceipts.android.sync.request.SyncRequest} that was used to
     * produce this response.
     */
    @NonNull
    SyncRequest<T> getRequest();

    /**
     * @return - the data returned by this result
     */
    @NonNull
    public T getResponse();

}
