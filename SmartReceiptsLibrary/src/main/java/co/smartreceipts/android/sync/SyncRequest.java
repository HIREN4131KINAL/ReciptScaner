package co.smartreceipts.android.sync;

/**
 * Implementations of this class can handle how synchronization requests are transmitted
 * 
 * @author Will Baumann
 */
public interface SyncRequest<T> {

	/**
	 * @return the {@link SyncRequestType} for this upload request
	 */
	public SyncRequestType getSyncRequestType();

	/**
	 * @return the {@link SyncUploadCategory} for this upload request
	 */
	public SyncUploadCategory getSyncUploadCategory();

	/**
	 * @return the desired payload or {@code} null if nothing is being uploaded
	 */
	public T getRequestData();
}
