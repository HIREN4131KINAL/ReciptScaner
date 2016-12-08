package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import co.smartreceipts.android.model.ProcessingStatus;

/**
 * Tracks the different processing states that a given {@link co.smartreceipts.android.model.Trip} can reside in.
 */
public enum TripProcessingStatus implements ProcessingStatus {

    /**
     * Indicates that this Trip should still be treated as in use (i.e. receipts are still being added)
     */
    InUse("InUse"),

    /**
     * Indicates that a report has been generated for this trip but may not have been submitted
     */
    Generated("Generated"),

    /**
     * Indicates that a report has been submitted for this trip
     */
    Submitted("Submitted"),

    /**
     * Indicates that this trip has been reimbursed by whomever is paying for it
     */
    Reimbursed("Reimbursed"),

    /**
     * Indicates that we're in an unknown state - shouldn't be normally be used
     */
    None("None");

    private final String mStatus;

    private TripProcessingStatus(final String status) {
        mStatus = status;
    }

    @Override
    public String getProcessingStatus() {
        return mStatus;
    }

    /**
     * Attempts to generate an enum representation of a given status string
     *
     * @param status - the provided status {@link java.lang.String}
     * @return the corresponding {@link co.smartreceipts.android.model.impl.TripProcessingStatus} or {@link co.smartreceipts.android.model.impl.TripProcessingStatus#None} if none match
     */
    public static TripProcessingStatus findProcessingStatusForString(final String status) {
        final TripProcessingStatus[] tripProcessingStatuses = TripProcessingStatus.values();
        for (int i = 0; i < tripProcessingStatuses.length; i++) {
            if (tripProcessingStatuses[i].getProcessingStatus().equals(status)) {
                return tripProcessingStatuses[i];
            }
        }
        return None;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mStatus);
    }

    public static final Creator<TripProcessingStatus> CREATOR = new Creator<TripProcessingStatus>() {
        public TripProcessingStatus createFromParcel(Parcel source) {
            return findProcessingStatusForString(source.readString());
        }

        public TripProcessingStatus[] newArray(int size) {
            return new TripProcessingStatus[size];
        }
    };
}
