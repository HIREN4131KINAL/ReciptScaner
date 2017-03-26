package co.smartreceipts.android.ad;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.purchases.PurchaseManager;

@ApplicationScope
public class NoOpAdManager implements AdManager {

    @Inject
    public NoOpAdManager() {
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable PurchaseManager purchaseManager) {
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
