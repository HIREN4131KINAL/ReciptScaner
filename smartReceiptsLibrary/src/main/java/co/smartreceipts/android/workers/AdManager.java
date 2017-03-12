package co.smartreceipts.android.workers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.smartreceipts.android.purchases.PurchaseManager;

public class AdManager extends WorkerChild {

    private PurchaseManager mPurchaseManager;

	public AdManager(@NonNull WorkerManager manager) {
		super(manager);
	}

    public void onActivityCreated(@NonNull Activity activity, @Nullable PurchaseManager purchaseManager) {
        mPurchaseManager = purchaseManager;
    }

    public void onResume() {
        // Stub method. Override in child subclasses
    }

    public void onPause() {
        // Stub method. Override in child subclasses
    }

    public void onDestroy() {
        mPurchaseManager = null;
    }

    @Nullable
    protected final PurchaseManager getSubscriptionManager() {
        return mPurchaseManager;
    }
}