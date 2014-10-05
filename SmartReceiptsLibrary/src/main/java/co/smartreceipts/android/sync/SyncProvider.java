package co.smartreceipts.android.sync;

import co.smartreceipts.android.sync.network.NetworkStateChangeListener;

public interface SyncProvider extends NetworkStateChangeListener {

	/**
	 * Determines if we can use this provider to support this particular category of synchronization operations
	 * 
	 * @param request
	 *            - the request to check
	 * @return {@code true} if this is supported. {@code false} otherwise.
	 */
	public boolean supportsSynchronization(SyncRequest<?> request);

	/**
	 * Submits a synchronization request to be uploaded to our back-end
	 * 
	 * @param syncRequest
	 *            - the {@link SyncRequest} to upload
	 * @return {@code true} if at {@link SyncProvider} is registered that supports this request type
	 */
	public boolean submitSyncRequest(SyncRequest<?> syncRequest);

}
