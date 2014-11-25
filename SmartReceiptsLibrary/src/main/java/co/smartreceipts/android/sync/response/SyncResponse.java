package co.smartreceipts.android.sync.response;

import co.smartreceipts.android.sync.request.SyncRequestType;
import co.smartreceipts.android.sync.request.SyncUploadCategory;

/**
 * Returns the result of a sync operation
 */
public interface SyncResponse<T> {

	/**
	 * @return the {@link co.smartreceipts.android.sync.request.SyncRequestType} for this upload request
	 */
	public SyncRequestType getSyncRequestType();

	/**
	 * @return the {@link co.smartreceipts.android.sync.request.SyncUploadCategory} for this upload request
	 */
	public SyncUploadCategory getSyncUploadCategory();

	/**
	 * @return - the data returned by this result
	 */
	public T getResultData();

}
