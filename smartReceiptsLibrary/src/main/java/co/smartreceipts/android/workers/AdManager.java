package co.smartreceipts.android.workers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.SubscriptionManager;
import co.smartreceipts.android.purchases.SubscriptionWallet;

public class AdManager extends WorkerChild {

    private SubscriptionManager mSubscriptionManager;

	public AdManager(@NonNull WorkerManager manager) {
		super(manager);
	}

    public void onActivityCreated(@NonNull Activity activity, @Nullable SubscriptionManager subscriptionManager) {
        mSubscriptionManager = subscriptionManager;
    }

    public void onResume() {
        // Stub method. Override in child subclasses
    }

    public void onPause() {
        // Stub method. Override in child subclasses
    }

    public void onDestroy() {
        mSubscriptionManager = null;
    }

    @Nullable
    protected final SubscriptionManager getSubscriptionManager() {
        return mSubscriptionManager;
    }

}