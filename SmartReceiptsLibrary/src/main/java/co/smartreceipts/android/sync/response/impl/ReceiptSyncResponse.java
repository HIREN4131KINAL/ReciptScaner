package co.smartreceipts.android.sync.response.impl;

import android.os.Parcel;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.sync.request.SyncRequest;

/**
 * An implementation of the {@link co.smartreceipts.android.sync.response.SyncResponse} interface for
 * {@link co.smartreceipts.android.model.Receipt} objects.
 *
 * @author williambaumann
 */
public class ReceiptSyncResponse extends AbstractSyncResponse<Receipt> {

    public ReceiptSyncResponse(Receipt response, SyncRequest<Receipt> request) {
        super(response, request);
    }

    private ReceiptSyncResponse(Parcel in) {
        super(in);
    }

    public static final Creator<ReceiptSyncResponse> CREATOR = new Creator<ReceiptSyncResponse>() {
        public ReceiptSyncResponse createFromParcel(Parcel source) {
            return new ReceiptSyncResponse(source);
        }

        public ReceiptSyncResponse[] newArray(int size) {
            return new ReceiptSyncResponse[size];
        }
    };
}
