package co.smartreceipts.android.sync.network;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.Preferences;

public class NetworkManager extends AbstractNetworkProvider implements NetworkStateChangeListener {

    private final NetworkProviderFactory mNetworkProviderFactory;
    private NetworkProvider mNetworkProvider;

    public NetworkManager(@NonNull Context context, @NonNull Preferences preferences) {
        this(new NetworkProviderFactory(context), preferences);
    }

    public NetworkManager(@NonNull NetworkProviderFactory networkProviderFactory, @NonNull Preferences preferences) {
        mNetworkProviderFactory = Preconditions.checkNotNull(networkProviderFactory);
        mNetworkProvider = mNetworkProviderFactory.get(preferences.getAutoBackupOnWifiOnly() ? SupportedNetworkType.WifiOnly : SupportedNetworkType.AllNetworks);
        mNetworkProvider.initialize();
    }

    public synchronized void setAndInitializeNetworkProviderType(@NonNull SupportedNetworkType supportedNetworkType) {
        final NetworkProvider newNetworkProvider = mNetworkProviderFactory.get(supportedNetworkType);
        final boolean isOldProviderConnected = mNetworkProvider.isNetworkAvailable();
        deinitialize();

        mNetworkProvider = newNetworkProvider;
        initialize();
        final boolean isNewProviderConnected = mNetworkProvider.isNetworkAvailable();
        if (isNewProviderConnected != isOldProviderConnected) {
            notifyStateChange();
        }
    }

    @Override
    public synchronized void deinitialize() {
        mNetworkProvider.unregisterListener(this);
        mNetworkProvider.deinitialize();
    }

    @Override
    public synchronized void initialize() {
        mNetworkProvider.initialize();
        mNetworkProvider.registerListener(this);
    }

    @Override
    public synchronized boolean isNetworkAvailable() {
        return mNetworkProvider.isNetworkAvailable();
    }

    @Override
    public synchronized void onNetworkConnectivityLost() {
        notifyStateChange();
    }

    @Override
    public synchronized void onNetworkConnectivityGained() {
        notifyStateChange();
    }
}
