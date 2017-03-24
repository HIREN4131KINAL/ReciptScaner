package co.smartreceipts.android.config;

/**
 * Provides a top level mechanism from which we can easily toggle on/off certain components within the app
 * in order that we might better support certain "white-label" efforts for our clients. This is defined in
 * the form of a contract interface to enable easy over-riding if required
 */
public interface ConfigurationManager {

    /**
     * @return {@code true} if we should show the settings menu. {@code false} if it should be hidden
     */
    boolean isSettingsMenuAvailable();

    /**
     * @return {@code true} if we should show let the user create text receipts. {@code false} if it should be hidden
     */
    boolean isTextReceiptsOptionAvailable();

    /**
     * @return {@code true} if we should show let the user track distance. {@code false} if it should be hidden
     */
    boolean isDistanceTrackingOptionAvailable();
}
