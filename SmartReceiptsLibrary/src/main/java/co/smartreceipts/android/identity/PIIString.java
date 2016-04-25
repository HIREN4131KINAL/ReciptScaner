package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;

/**
 * Allows us to manage a PII string in a safe manner
 */
class PIIString implements PIIMarker<String> {

    private final String mPiiString;

    public PIIString(@NonNull String piiString) {
        this.mPiiString = piiString;
    }

    @Override
    @NonNull
    public String get() {
        return mPiiString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PIIString)) return false;

        PIIString piiString = (PIIString) o;

        return mPiiString.equals(piiString.mPiiString);

    }

    @Override
    public int hashCode() {
        return mPiiString.hashCode();
    }

    @Override
    public String toString() {
        return mPiiString;
    }

}
