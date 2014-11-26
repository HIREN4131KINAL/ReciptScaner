package co.smartreceipts.android.model;

import android.os.Parcelable;

/**
 * Tracks the current processing status the a given {@link co.smartreceipts.android.model.Trip} or
 * {@link co.smartreceipts.android.model.Receipt} may be in (e.g. what is just created, has it been
 * submitted, was it approved, etc.)
 */
public interface ProcessingStatus extends Parcelable {

    /**
     * Returns the current processing status string representation for this item (e.g. has it been submitted)
     *
     * @return - the current processing status {@link java.lang.String}
     */
    String getProcessingStatus();

}
