package co.smartreceipts.android.sync;

import android.os.Parcelable;

import co.smartreceipts.android.sync.request.SyncRequestType;
import co.smartreceipts.android.sync.response.SyncError;
import co.smartreceipts.android.sync.response.SyncResponse;

/**
 * This interface defines a series of callback mechanisms that can be used to track when our data has been successfully
 * synchronized or not
 * 
 * @author Will Baumann
 */
public interface SyncListener<T extends Parcelable> {

	/**
	 * This method will be called whenever our data has successfully synchronized with the back-end
	 * 
	 * @param type
	 *            - the {@link co.smartreceipts.android.sync.request.SyncRequestType} of the request
	 * @param result
	 *            - the {@link co.smartreceipts.android.sync.response.SyncResponse} that was received
	 */
	public void onSyncSuccess(SyncRequestType type, SyncResponse<T> result);

	/**
	 * This method will be called whenever our data has failed to synchronize with the back-end
	 * 
	 * @param type
	 *            - the {@link SyncRequestType} of the request
	 * @param exception
	 *            - the {@link co.smartreceipts.android.sync.response.SyncError}, which details why the sync failed
	 */
	public void onSyncError(SyncRequestType type, SyncError exception);
}
