package co.smartreceipts.android.sync.network;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

/**
 * This class provides a simple mechanism to determine whether or not we're currently connected to a mobile or wifi
 * network. It will also send out broadcasts if we lose/gain this connection.
 * 
 * @author Will Baumann
 */
public class AnyNetworkProviderImpl extends AbstractNetworkProvider implements NetworkStateChangeListener {

	private final WifiNetworkProviderImpl mWifiNetworkProviderImpl;
	private final MobileNetworkProviderImpl mMobileNetworkProviderImpl;

	public AnyNetworkProviderImpl(@NonNull Context context) {
		this(new WifiNetworkProviderImpl(context), new MobileNetworkProviderImpl(context));
	}

    public AnyNetworkProviderImpl(@NonNull WifiNetworkProviderImpl wifiNetworkProvider, @NonNull MobileNetworkProviderImpl mobileNetworkProvider) {
        mWifiNetworkProviderImpl = Preconditions.checkNotNull(wifiNetworkProvider);
        mMobileNetworkProviderImpl = Preconditions.checkNotNull(mobileNetworkProvider);
    }

    @Override
    public void initialize() {
        mWifiNetworkProviderImpl.initialize();
        mMobileNetworkProviderImpl.initialize();
        mWifiNetworkProviderImpl.registerListener(this);
        mMobileNetworkProviderImpl.registerListener(this);
    }

    @Override
    public void deinitialize() {
        mWifiNetworkProviderImpl.unregisterListener(this);
        mMobileNetworkProviderImpl.unregisterListener(this);
        mWifiNetworkProviderImpl.deinitialize();
        mMobileNetworkProviderImpl.deinitialize();
    }

    @Override
	public boolean isNetworkAvailable() {
		return mWifiNetworkProviderImpl.isNetworkAvailable() || mMobileNetworkProviderImpl.isNetworkAvailable();
	}

    @Override
    public void onNetworkConnectivityLost() {
        notifyStateChange();
    }

    @Override
    public void onNetworkConnectivityGained() {
        notifyStateChange();
    }
}
