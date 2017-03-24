package co.smartreceipts.android.utils;

public interface Feature {

    /**
     * Checks if this particular feature is enabled
     *
     * @return {@code true} if it is enabled. {@code false} otherwise
     */
    boolean isEnabled();
}
