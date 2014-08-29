package co.smartreceipts.android.sync;

public interface SyncProvider<T extends SyncKey> extends NetworkStateChangeListener {

	/**
	 * Determines if we can use this provider to support this particular category of synchronization operations
	 * 
	 * @param request
	 *            - the request to check
	 * @return {@code true} if this is supported. {@code false} otherwise.
	 */
	public boolean supportsSynchronization(SyncRequest<T> request);

}
