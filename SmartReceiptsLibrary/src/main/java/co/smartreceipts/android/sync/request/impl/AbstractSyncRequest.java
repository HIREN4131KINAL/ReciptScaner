package co.smartreceipts.android.sync.request.impl;

import co.smartreceipts.android.sync.request.SyncRequest;
import co.smartreceipts.android.sync.request.SyncRequestType;

/**
 * An abstract implementation of the {@link co.smartreceipts.android.sync.request.SyncRequest} interface
 * in order to provide a base implementation for common data types.
 *
 * @author williambaumann
 */
abstract class AbstractSyncRequest<T> implements SyncRequest<T> {

    private final SyncRequestType mSyncRequestType;
    private final T mRequestData;

    protected AbstractSyncRequest(T requestData, SyncRequestType requestType) {
        mRequestData = requestData;
        mSyncRequestType = requestType;
    }

    @Override
    public SyncRequestType getSyncRequestType() {
        return mSyncRequestType;
    }

    @Override
    public T getRequestData() {
        return mRequestData;
    }

}
