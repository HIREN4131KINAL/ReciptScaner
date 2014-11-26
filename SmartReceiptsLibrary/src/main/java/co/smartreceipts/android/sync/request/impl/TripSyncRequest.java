package co.smartreceipts.android.sync.request.impl;

import android.os.Parcel;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.sync.request.SyncRequestType;

/**
 * An implementation of the {@link co.smartreceipts.android.sync.request.SyncRequest} interface for
 * {@link co.smartreceipts.android.model.Trip} objects.
 *
 * @author williambaumann
 */
public class TripSyncRequest extends AbstractSyncRequest<Trip> {

    public TripSyncRequest(Trip requestData, SyncRequestType requestType) {
        super(requestData, requestType, Trip.class);
    }

    private TripSyncRequest(Parcel in) {
        super(in);
    }

    public static final Creator<TripSyncRequest> CREATOR = new Creator<TripSyncRequest>() {
        public TripSyncRequest createFromParcel(Parcel source) {
            return new TripSyncRequest(source);
        }

        public TripSyncRequest[] newArray(int size) {
            return new TripSyncRequest[size];
        }
    };
}
