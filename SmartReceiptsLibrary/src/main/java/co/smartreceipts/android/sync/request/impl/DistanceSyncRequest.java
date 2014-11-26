package co.smartreceipts.android.sync.request.impl;

import android.os.Parcel;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.sync.request.SyncRequestType;

/**
 * An implementation of the {@link co.smartreceipts.android.sync.request.SyncRequest} interface for
 * {@link co.smartreceipts.android.model.Trip} objects.
 *
 * @author williambaumann
 */
public class DistanceSyncRequest extends AbstractSyncRequest<Distance> {

    public DistanceSyncRequest(Distance requestData, SyncRequestType requestType) {
        super(requestData, requestType, Distance.class);
    }

    private DistanceSyncRequest(Parcel in) {
        super(in);
    }

    public static final Creator<DistanceSyncRequest> CREATOR = new Creator<DistanceSyncRequest>() {
        public DistanceSyncRequest createFromParcel(Parcel source) {
            return new DistanceSyncRequest(source);
        }

        public DistanceSyncRequest[] newArray(int size) {
            return new DistanceSyncRequest[size];
        }
    };

}
