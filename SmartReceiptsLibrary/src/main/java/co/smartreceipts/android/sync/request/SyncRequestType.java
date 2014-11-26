package co.smartreceipts.android.sync.request;

import android.os.Parcelable;

/**
 * Tracks different available request types
 *
 * @author Will Baumann
 */
public interface SyncRequestType extends Parcelable {

    String getType();
}
