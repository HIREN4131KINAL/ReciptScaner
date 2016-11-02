package co.smartreceipts.android.sync.network;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.concurrent.CopyOnWriteArraySet;

class NetworkStateChangeListenerTracker {

    private final CopyOnWriteArraySet<NetworkStateChangeListener> mListeners = new CopyOnWriteArraySet<>();

    public final void registerListener(@NonNull NetworkStateChangeListener listener) {
        mListeners.add(Preconditions.checkNotNull(listener));
    }

    public final void unregisterListener(@NonNull NetworkStateChangeListener listener) {
        mListeners.remove(Preconditions.checkNotNull(listener));
    }

    public void clear() {
        mListeners.clear();
    }

    public final void notifyNetworkConnectivityGained() {
        for (final NetworkStateChangeListener listener : mListeners) {
            listener.onNetworkConnectivityGained();
        }
    }

    public final void notifyNetworkConnectivityLost() {
        for (final NetworkStateChangeListener listener : mListeners) {
            listener.onNetworkConnectivityLost();
        }
    }
}
