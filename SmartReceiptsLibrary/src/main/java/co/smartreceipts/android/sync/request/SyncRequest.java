package co.smartreceipts.android.sync.request;

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Implementations of this class can handle how synchronization requests are transmitted
 * 
 * @author Will Baumann
 */
public interface SyncRequest<T extends Parcelable> extends Parcelable {

	/**
	 * @return the {@link SyncRequestType} for this upload request
	 */
    @NonNull
    public SyncRequestType getSyncRequestType();

	/**
	 * @return the desired payload or {@code} null if nothing is being uploaded
	 */
    @NonNull
    public T getRequestData();

    /**
     * @return the {@link java.lang.Class} type of {@link #getRequestData()}
     */
    @NonNull
    public Class<T> getRequestDataClass();
}
