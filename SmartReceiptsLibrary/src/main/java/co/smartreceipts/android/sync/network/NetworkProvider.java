package co.smartreceipts.android.sync.network;

/**
 * This interface defines different network provider types (e.g. WiFi-Only vs Mobile) in order to allow us to track the
 * current state and changing states of each
 * 
 * @author Will Baumann
 */
public interface NetworkProvider {

	/**
	 * This action will be broadcast whenever our network provider state has changed. It will be sent with one of the
	 * following extras:
	 * 
	 * <ul>
	 * <li>{@link #EXTRA_NETWORK_STATE_CONNECTED}</li>
	 * </ul>
	 */
	public static final String ACTION_NETWORK_STATE_CHANGED = "co.smartreceipts.android.sync.network.StateChanged";

	/**
	 * This boolean extra contains the following data:
	 * 
	 * <ul>
	 * <li>{@code true} - if we now have a network connection</li>
	 * <li>{@code false} - if we no longer have a connection</li>
	 * </ul>
	 */
	public static final String EXTRA_NETWORK_STATE_CONNECTED = "co.smartreceipts.android.sync.network.Connection";

	/**
	 * Checks if a network connection is currently available
	 * 
	 * @return {@code true} if the network is currently available. {@code false} if it is not.
	 */
	public boolean isNetworkAvailable();

}
