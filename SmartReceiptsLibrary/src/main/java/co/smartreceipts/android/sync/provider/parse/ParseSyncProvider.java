package co.smartreceipts.android.sync.provider.parse;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.sync.request.SyncRequest;
import co.smartreceipts.android.sync.network.NetworkProvider;

/**
 * Created by williambaumann on 11/24/14.
 */
public class ParseSyncProvider implements SyncProvider {

    private final NetworkProvider mNetworkProvider;
    private final ParseTripSyncHelper mParseSyncTripProvider;

    public ParseSyncProvider(NetworkProvider networkProvider) {
        mNetworkProvider = networkProvider;
        mParseSyncTripProvider = new ParseTripSyncHelper(networkProvider);
    }

    @Override
    public boolean supportsSynchronization(SyncRequest request) {
        return true;
    }

    @Override
    public boolean submitSyncRequest(SyncRequest syncRequest) {
        if (syncRequest.getRequestDataClass().equals(Trip.class)) {
            // mParseSyncTripProvider.submitSyncRequest(syncRequest);
        }
        return true;
    }

    @Override
    public void onNetworkConnectivityLost() {

    }

    @Override
    public void onNetworkConnectivityGained() {

    }
}
