package co.smartreceipts.android.workers;

import android.support.annotation.NonNull;
import android.view.View;

import co.smartreceipts.android.purchases.SubscriptionWallet;

public class AdManager extends WorkerChild {

    private final SubscriptionWallet mSubscriptionWallet;

	public AdManager(@NonNull WorkerManager manager, @NonNull SubscriptionWallet subscriptionWallet) {
		super(manager);
        mSubscriptionWallet = subscriptionWallet;
	}

    public void onViewCreated(@NonNull View rootView) {
        // Stub method. Override in child subclasses
    }

    public void onResume() {
        // Stub method. Override in child subclasses
    }

    public void onPause() {
        // Stub method. Override in child subclasses
    }

    public void onDestroy() {
        // Stub method. Override in child subclasses
    }

    protected final SubscriptionWallet getSubscriptionWallet() {
        return mSubscriptionWallet;
    }

}
