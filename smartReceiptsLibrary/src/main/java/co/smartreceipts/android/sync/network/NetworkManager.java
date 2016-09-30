package co.smartreceipts.android.sync.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.Preferences;

public class NetworkManager extends CompositeNetworkProviderImpl {

    private static final String TAG = NetworkManager.class.getSimpleName();

    private final NetworkProviderFactory mNetworkProviderFactory;

    public NetworkManager(@NonNull Context context, @NonNull Preferences preferences) {
        this(new NetworkProviderFactory(context), preferences);
    }

    public NetworkManager(@NonNull NetworkProviderFactory networkProviderFactory, @NonNull Preferences preferences) {
        super(networkProviderFactory.get(preferences.getAutoBackupOnWifiOnly() ? SupportedNetworkType.WifiOnly : SupportedNetworkType.AllNetworks));
        mNetworkProviderFactory = Preconditions.checkNotNull(networkProviderFactory);
        initialize();
    }

    public synchronized void setAndInitializeNetworkProviderType(@NonNull SupportedNetworkType supportedNetworkType) {
        Log.i(TAG, "Updating network provider type to: " + supportedNetworkType);
        final NetworkProvider newNetworkProvider = mNetworkProviderFactory.get(supportedNetworkType);

        Log.i(TAG, "De-initializing the old network type");
        for (final NetworkProvider networkProvider : mNetworkProviders) {
            networkProvider.deinitialize();
            networkProvider.unregisterListener(this);
        }

        // Updating
        Log.i(TAG, "Updating network type references");
        mNetworkProviders.clear();
        mNetworkProviders.add(newNetworkProvider);

        Log.i(TAG, "Initializing the new provider");
        for (final NetworkProvider networkProvider : mNetworkProviders) {
            networkProvider.initialize();
            networkProvider.registerListener(this);
        }

        checkForConnectionChange();
    }

}
