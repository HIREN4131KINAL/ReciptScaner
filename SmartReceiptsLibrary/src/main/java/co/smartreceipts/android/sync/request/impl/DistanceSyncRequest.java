package co.smartreceipts.android.sync.request.impl;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.sync.request.SyncRequestType;
import co.smartreceipts.android.sync.request.SyncUploadCategory;

/**
 * An implementation of the {@link co.smartreceipts.android.sync.request.SyncRequest} interface for
 * {@link co.smartreceipts.android.model.Trip} objects.
 *
 * @author williambaumann
 */
public class DistanceSyncRequest extends AbstractSyncRequest<Distance> {

    public DistanceSyncRequest(Distance requestData, SyncRequestType requestType) {
        super(requestData, requestType);
    }

    @Override
    public SyncUploadCategory getSyncUploadCategory() {
        return SyncUploadCategory.DataOnly;
    }

    @Override
    public Class<Distance> getRequestDataClass() {
        return Distance.class;
    }
}
