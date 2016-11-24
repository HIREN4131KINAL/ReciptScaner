package co.smartreceipts.android.sync.network;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.utils.log.Logger;

public class NetworkManager extends CompositeNetworkProviderImpl {

    private static final String TAG = NetworkManager.class.getSimpleName();

    private final NetworkProviderFactory mNetworkProviderFactory;
    private final Preferences mPreferences;

    public NetworkManager(@NonNull Context context, @NonNull Preferences preferences) {
        this(new NetworkProviderFactory(context), preferences);
    }

    public NetworkManager(@NonNull NetworkProviderFactory networkProviderFactory, @NonNull Preferences preferences) {
        super(networkProviderFactory.get(preferences.getAutoBackupOnWifiOnly() ? SupportedNetworkType.WifiOnly : SupportedNetworkType.AllNetworks));
        mNetworkProviderFactory = Preconditions.checkNotNull(networkProviderFactory);
        mPreferences = Preconditions.checkNotNull(preferences);
        initialize();
    }

    public synchronized void setAndInitializeNetworkProviderType(@NonNull SupportedNetworkType supportedNetworkType) {
        Logger.info(this, "Updating network provider type to: {}", supportedNetworkType);
        final NetworkProvider newNetworkProvider = mNetworkProviderFactory.get(supportedNetworkType);

        Logger.info(this, "De-initializing the old network type");
        for (final NetworkProvider networkProvider : mNetworkProviders) {
            networkProvider.deinitialize();
            networkProvider.unregisterListener(this);
        }

        // Updating
        Logger.info(this, "Updating network type references");
        mNetworkProviders.clear();
        mNetworkProviders.add(newNetworkProvider);

        Logger.info(this, "Initializing the new provider");
        for (final NetworkProvider networkProvider : mNetworkProviders) {
            networkProvider.initialize();
            networkProvider.registerListener(this);
        }

        checkForConnectionChange();
    }

    @NonNull
    public synchronized SupportedNetworkType getSupportedNetworkType() {
        return mPreferences.getAutoBackupOnWifiOnly() ? SupportedNetworkType.WifiOnly : SupportedNetworkType.AllNetworks;
    }

}
