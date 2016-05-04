package co.smartreceipts.android.identity.pii;

/**
 * Allows us to manage a PII string in a safe manner
 */
public class PIIString implements PIIMarker<String> {

    private final String mPiiString;

    public PIIString(String piiString) {
        this.mPiiString = piiString;
    }

    @Override
    public String get() {
        return mPiiString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PIIString)) return false;

        PIIString piiString = (PIIString) o;

        return mPiiString != null ? mPiiString.equals(piiString.mPiiString) : piiString.mPiiString == null;

    }

    @Override
    public int hashCode() {
        return mPiiString != null ? mPiiString.hashCode() : 0;
    }

    @Override
    public String toString() {
        return mPiiString;
    }

}
