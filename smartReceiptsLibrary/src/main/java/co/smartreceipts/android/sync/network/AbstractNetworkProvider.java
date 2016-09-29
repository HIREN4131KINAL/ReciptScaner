package co.smartreceipts.android.sync.network;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.concurrent.CopyOnWriteArraySet;

abstract class AbstractNetworkProvider implements NetworkProvider {

    private final CopyOnWriteArraySet<NetworkStateChangeListener> mListeners = new CopyOnWriteArraySet<>();

    @Override
    public final void registerListener(@NonNull NetworkStateChangeListener listener) {
        mListeners.add(Preconditions.checkNotNull(listener));
    }

    @Override
    public final void unregisterListener(@NonNull NetworkStateChangeListener listener) {
        mListeners.remove(Preconditions.checkNotNull(listener));
    }

    @Override
    public void deinitialize() {
        mListeners.clear();
    }

    final void notifyStateChange() {
        if (isNetworkAvailable()) {
            for (final NetworkStateChangeListener listener : mListeners) {
                listener.onNetworkConnectivityGained();
            }
        } else {
            for (final NetworkStateChangeListener listener : mListeners) {
                listener.onNetworkConnectivityLost();
            }
        }
    }
}
