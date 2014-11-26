package co.smartreceipts.android.sync.response;

import android.os.Parcelable;

import co.smartreceipts.android.sync.request.SyncRequestType;

/**
 * Returns the result of a sync operation
 */
public interface SyncResponse<T extends Parcelable> extends Parcelable {

    /**
     * @return the {@link co.smartreceipts.android.sync.request.SyncRequestType} for this upload request
     */
    public SyncRequestType getSyncRequestType();

    /**
     * @return - the data returned by this result
     */
    public T getResultData();

}
