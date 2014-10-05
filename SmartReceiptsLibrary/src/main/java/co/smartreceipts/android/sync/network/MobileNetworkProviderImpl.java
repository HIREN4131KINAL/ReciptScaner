package co.smartreceipts.android.sync.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

/**
 * This class provides a simple mechanism to determine whether or not we're currently connected to a mobile network. It
 * will also send out broadcasts if we lose/gain this connection.
 * 
 * @author Will Baumann
 */
public class MobileNetworkProviderImpl implements NetworkProvider {

	private final Context mContext;
	private final ConnectivityManager mConnectivityManager;
	private final MobileDataStateChangeBroadcastReceiver mMobileDataStateChangeBroadcastReceiver;

	/**
	 * This operates as the default constructor for this class.
	 * 
	 * @param context
	 *            - a {@link Context} is required to track this information
	 */
	public MobileNetworkProviderImpl(Context context) {
		mContext = context.getApplicationContext();
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		mMobileDataStateChangeBroadcastReceiver = new MobileDataStateChangeBroadcastReceiver();
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(mMobileDataStateChangeBroadcastReceiver, intentFilter);
	}

	@Override
	public boolean isNetworkAvailable() {
		return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
	}

	/**
	 * A nested broadcast receiver, which listens for wifi state change events
	 * 
	 * @author Will Baumann
	 */
	static final class MobileDataStateChangeBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				final Intent stateChangedIntent = new Intent(NetworkProvider.ACTION_NETWORK_STATE_CHANGED);
				final ConnectivityManager connectivityManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {
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
