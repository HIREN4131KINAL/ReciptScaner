package co.smartreceipts.android.sync.network;

import android.content.Context;

/**
 * This class provides a simple mechanism to determine whether or not we're currently connected to a mobile or wifi
 * network. It will also send out broadcasts if we lose/gain this connection.
 * 
 * @author Will Baumann
 */
public class AnyNetworkProviderImpl implements NetworkProvider {

	private final WifiNetworkProviderImpl mWifiNetworkProviderImpl;
	private final MobileNetworkProviderImpl mMobileNetworkProviderImpl;

	/**
	 * This operates as the default constructor for this class.
	 * 
	 * @param context
	 *            - a {@link Context} is required to track this information
	 */
	public AnyNetworkProviderImpl(Context context) {
		mWifiNetworkProviderImpl = new WifiNetworkProviderImpl(context);
		mMobileNetworkProviderImpl = new MobileNetworkProviderImpl(context);
	}

	@Override
	public boolean isNetworkAvailable() {
		return mWifiNetworkProviderImpl.isNetworkAvailable() || mMobileNetworkProviderImpl.isNetworkAvailable();
	}

}
