package co.smartreceipts.android.sync.request.impl;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.sync.request.SyncRequestType;
import co.smartreceipts.android.sync.request.SyncUploadCategory;

/**
 * An implementation of the {@link co.smartreceipts.android.sync.request.SyncRequest} interface for
 * {@link co.smartreceipts.android.model.Trip} objects.
 *
 * @author williambaumann
 */
public class TripSyncRequest extends AbstractSyncRequest<Trip> {

    public TripSyncRequest(Trip requestData, SyncRequestType requestType) {
        super(requestData, requestType);
    }

    @Override
    public SyncUploadCategory getSyncUploadCategory() {
        return SyncUploadCategory.DataOnly;
    }

    @Override
    public Class<Trip> getRequestDataClass() {
        return Trip.class;
    }
}
