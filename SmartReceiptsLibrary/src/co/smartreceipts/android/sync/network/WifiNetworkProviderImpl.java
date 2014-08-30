package co.smartreceipts.android.sync.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

/**
 * This class provides a simple mechanism to determine whether or not we're currently connected to a wifi network. It
 * will also send out broadcasts if we lose/gain this connection. Please note that this class does not check if there is
 * a wifi login page that is blocking actual network access. This can be done by implementing the solution described in
 * the "Handling Network Sign-On" section of this site:
 * http://developer.android.com/reference/java/net/HttpURLConnection.html.
 * 
 * @author Will Baumann
 */
public class WifiNetworkProviderImpl implements NetworkProvider {

	private final Context mContext;
	private final ConnectivityManager mConnectivityManager;
	private final WifiStateChangeBroadcastReceiver mWifiStateChangeBroadcastReceiver;

	/**
	 * This operates as the default constructor for this class.
	 * 
	 * @param context
	 *            - a {@link Context} is required to track this information
	 */
	public WifiNetworkProviderImpl(Context context) {
		mContext = context.getApplicationContext();
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifiStateChangeBroadcastReceiver = new WifiStateChangeBroadcastReceiver();
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		mContext.registerReceiver(mWifiStateChangeBroadcastReceiver, intentFilter);
	}

	@Override
	public boolean isNetworkAvailable() {
		return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
	}

	/**
	 * A nested broadcast receiver, which listens for wifi state change events
	 * 
	 * @author Will Baumann
	 */
	static final class WifiStateChangeBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				final Intent stateChangedIntent = new Intent(NetworkProvider.ACTION_NETWORK_STATE_CHANGED);
				if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
					stateChangedIntent.putExtra(EXTRA_NETWORK_STATE_CONNECTED, true); // Connected
				}
				else {
					stateChangedIntent.putExtra(EXTRA_NETWORK_STATE_CONNECTED, false); // Disconnected
				}
				context.sendBroadcast(stateChangedIntent);
			}
		}

	}

}
