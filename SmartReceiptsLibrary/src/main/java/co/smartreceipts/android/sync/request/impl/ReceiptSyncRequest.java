package co.smartreceipts.android.sync.request.impl;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.sync.request.SyncRequestType;
import co.smartreceipts.android.sync.request.SyncUploadCategory;

/**
 * An implementation of the {@link co.smartreceipts.android.sync.request.SyncRequest} interface for
 * {@link co.smartreceipts.android.model.Trip} objects.
 *
 * @author williambaumann
 */
public class ReceiptSyncRequest extends AbstractSyncRequest<Receipt> {

    public ReceiptSyncRequest(Receipt requestData, SyncRequestType requestType) {
        super(requestData, requestType);
    }

    @Override
    public SyncUploadCategory getSyncUploadCategory() {
        // TODO: Possible bug if we delete the file here?
        if (getRequestData().hasFile()) {
            return SyncUploadCategory.DataAndFile;
        }
        else {
            return SyncUploadCategory.DataOnly;
        }
    }

    @Override
    public Class<Receipt> getRequestDataClass() {
        return Receipt.class;
    }
}
