package co.smartreceipts.android.sync;

/**
 * Returns the result of a sync operation
 */
public interface SyncResult<T> {

	/**
	 * @return the {@link SyncRequestType} for this upload request
	 */
	public SyncRequestType getSyncRequestType();

	/**
	 * @return the {@link SyncUploadCategory} for this upload request
	 */
	public SyncUploadCategory getSyncUploadCategory();

	/**
	 * @return - the data returned by this result
	 */
	public T getResultData();

}
