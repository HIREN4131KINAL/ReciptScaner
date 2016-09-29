package co.smartreceipts.android.sync.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

/**
 * This class provides a simple mechanism to determine whether or not we're currently connected to a mobile network. It
 * will also send out broadcasts if we lose/gain this connection.
 */
public class MobileNetworkProviderImpl extends AbstractNetworkProvider {

	private final Context mContext;
	private final ConnectivityManager mConnectivityManager;
	private final MobileDataStateChangeBroadcastReceiver mMobileDataStateChangeBroadcastReceiver;

	public MobileNetworkProviderImpl(@NonNull Context context) {
        this(context, (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
	}

    public MobileNetworkProviderImpl(@NonNull Context context, @NonNull ConnectivityManager connectivityManager) {
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mConnectivityManager = Preconditions.checkNotNull(connectivityManager);
        mMobileDataStateChangeBroadcastReceiver = new MobileDataStateChangeBroadcastReceiver();
    }

    @Override
    public void initialize() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mMobileDataStateChangeBroadcastReceiver, intentFilter);
    }

    @Override
    public void deinitialize() {
        mContext.unregisterReceiver(mMobileDataStateChangeBroadcastReceiver);
        super.deinitialize();
    }

    @Override
	public boolean isNetworkAvailable() {
		return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected();
	}

	final class MobileDataStateChangeBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				notifyStateChange();
			}
		}

	}

}
