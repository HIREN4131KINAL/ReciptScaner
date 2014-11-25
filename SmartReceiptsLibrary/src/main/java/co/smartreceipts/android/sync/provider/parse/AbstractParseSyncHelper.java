package co.smartreceipts.android.sync.provider.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import co.smartreceipts.android.sync.response.SyncException;
import co.smartreceipts.android.sync.request.SyncRequest;
import co.smartreceipts.android.sync.response.SyncResponse;
import co.smartreceipts.android.sync.network.NetworkProvider;

/**
 * A package private abstract class, which we will use to manage uploads to parse. This class
 * has been designed as a helper in order that we can easily manage the upload of a particular
 * data type {@link T}.
 *
 * @author Will Baumann
 */
abstract class AbstractParseSyncHelper<T> {

    private final List<SyncRequest> mOutstandingRequests;
    private final NetworkProvider mNetworkProvider;

    public AbstractParseSyncHelper(NetworkProvider networkProvider) {
        mOutstandingRequests = new CopyOnWriteArrayList<SyncRequest>();
        mNetworkProvider = networkProvider;
    }

    final void submitSyncRequest(SyncRequest<T> request) throws SyncException {
        if (mNetworkProvider.isNetworkAvailable()) {
            onSubmitSyncRequestWithNetwork(request);
        } else {
            mOutstandingRequests.add(request);
        }
    }

    final void onNetworkConnectivityLost() {

    }

    final void onNetworkConnectivityGained() throws SyncException {
        if (!mOutstandingRequests.isEmpty()) {
            final List<SyncRequest<T>> listSnapshot = new ArrayList<SyncRequest<T>>(mOutstandingRequests);
            mOutstandingRequests.clear();
            for (SyncRequest<T> request : listSnapshot) {
                onSubmitSyncRequestWithNetwork(request);
            }
        }
    }

    /**
     * This should be over-ridden as needed by subclasses to manage the implementation details of actually submitting
     * the sync request
     *
     * @param request - the {@link java.util.List} of {@link co.smartreceipts.android.sync.request.SyncRequest}s to send.
     */
    protected abstract SyncResponse<T> onSubmitSyncRequestWithNetwork(SyncRequest<T> request) throws SyncException;

}
