package co.smartreceipts.android.sync;

/**
 * This interface defines different network provider types (e.g. WiFi-Only vs Mobile) in order to allow us to track the
 * current state and changing states of each
 * 
 * @author Will Baumann
 */
public interface NetworkProvider {

	/**
	 * Checks if a network connection is currently available
	 * 
	 * @return {@code true} if the network is currently available. {@code false} if it is not.
	 */
	public boolean isNetworkAvailable();

	/**
	 * This method is called whenever we regain our current network connection
	 */
	public void onNetworkConnectionRegained();

	/**
	 * This method is called whenever we lose our current network connection
	 */
	public void onNetworkConnectionLost();

}
