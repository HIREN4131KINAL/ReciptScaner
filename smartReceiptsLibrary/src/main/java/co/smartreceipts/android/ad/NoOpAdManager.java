package co.smartreceipts.android.ad;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import co.smartreceipts.android.purchases.SubscriptionManager;

@Singleton
public class NoOpAdManager implements AdManager {

    @Inject
    public NoOpAdManager() {
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable SubscriptionManager subscriptionManager) {
        /* no-op */
    }

    @Override
    public void onResume() {
        /* no-op */
    }

    @Override
    public void onPause() {
        /* no-op */
    }

    @Override
    public void onDestroy() {
        /* no-op */
    }
}
