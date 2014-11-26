package co.smartreceipts.android.sync.provider.parse;

import org.apache.http.MethodNotSupportedException;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.sync.response.SyncError;
import co.smartreceipts.android.sync.request.SyncRequest;
import co.smartreceipts.android.sync.response.SyncResponse;
import co.smartreceipts.android.sync.network.NetworkProvider;

/**
 * Handles the process of submitting a {@link co.smartreceipts.android.sync.request.SyncRequest} to the Parse
 * backend for a {@link co.smartreceipts.android.model.Trip} object
 *
 * @author williambaumann
 */
public class ParseTripSyncHelper extends AbstractParseSyncHelper<Trip> {

    public ParseTripSyncHelper(NetworkProvider networkProvider) {
        super(networkProvider);
    }

    @Override
    protected SyncResponse<Trip> onSubmitSyncRequestWithNetwork(SyncRequest<Trip> request) {
        throw new UnsupportedOperationException("Not ready yet");
    }

}
