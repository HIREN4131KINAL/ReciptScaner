package co.smartreceipts.android.sync.response.impl;

import android.os.Parcel;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.sync.request.SyncRequest;

/**
 * An implementation of the {@link co.smartreceipts.android.sync.response.SyncResponse} interface for
 * {@link co.smartreceipts.android.model.Distance} objects.
 *
 * @author williambaumann
 */
public class DistanceSyncResponse extends AbstractSyncResponse<Distance> {

    public DistanceSyncResponse(Distance response, SyncRequest<Distance> request) {
        super(response, request);
    }

    private DistanceSyncResponse(Parcel in) {
        super(in);
    }

    public static final Creator<DistanceSyncResponse> CREATOR = new Creator<DistanceSyncResponse>() {
        public DistanceSyncResponse createFromParcel(Parcel source) {
            return new DistanceSyncResponse(source);
        }

        public DistanceSyncResponse[] newArray(int size) {
            return new DistanceSyncResponse[size];
        }
    };
}
