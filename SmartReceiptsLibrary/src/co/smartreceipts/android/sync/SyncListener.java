package co.smartreceipts.android.sync;

/**
 * This interface defines a series of callback mechanisms that can be used to track when our data has been successfully
 * synchronized or not
 * 
 * @author Will Baumann
 */
public interface SyncListener<T> {

	/**
	 * This method will be called whenever our data has successfully synchronized with the back-end
	 * 
	 * @param type
	 *            - the {@link SyncRequestType} of the request
	 * @param result
	 *            - the {@link SyncResult} that was received
	 */
	public void onSyncSuccess(SyncRequestType type, SyncResult<T> result);

	/**
	 * This method will be called whenever our data has failed to synchronize with the back-end
	 * 
	 * @param type
	 *            - the {@link SyncRequestType} of the request
	 * @param exception
	 *            - the {@link SyncException}, which details why the sync failed
	 */
	public void onSyncError(SyncRequestType type, SyncException exception);
}
