package co.smartreceipts.android.sync.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.net.ConnectivityManagerCompat;

import com.google.common.base.Preconditions;

/**
 * This class provides a simple mechanism to determine whether or not we're currently connected to a wifi network. It
 * will also send out broadcasts if we lose/gain this connection. Please note that this class does not check if there is
 * a wifi login page that is blocking actual network access. This can be done by implementing the solution described in
 * the "Handling Network Sign-On" section of this site:
 * http://developer.android.com/reference/java/net/HttpURLConnection.html.
 */
public class WifiNetworkProviderImpl extends AbstractNetworkProvider {

	private final Context mContext;
	private final ConnectivityManager mConnectivityManager;
	private final WifiStateChangeBroadcastReceiver mWifiStateChangeBroadcastReceiver;

	public WifiNetworkProviderImpl(@NonNull Context context) {
		this(context, (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
	}

    public WifiNetworkProviderImpl(@NonNull Context context, @NonNull ConnectivityManager connectivityManager) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mConnectivityManager = Preconditions.checkNotNull(connectivityManager);
        mWifiStateChangeBroadcastReceiver = new WifiStateChangeBroadcastReceiver();
    }

    @Override
    public void initialize() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        mContext.registerReceiver(mWifiStateChangeBroadcastReceiver, intentFilter);
    }

    @Override
    public void deinitialize() {
        mContext.unregisterReceiver(mWifiStateChangeBroadcastReceiver);
        super.deinitialize();
    }

    @Override
	public boolean isNetworkAvailable() {
		return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
	}

	final class WifiStateChangeBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                notifyStateChange();
			}
		}

	}

}
