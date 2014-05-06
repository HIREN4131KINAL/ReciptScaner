package co.smartreceipts.android.workers;

import android.view.View;

public class AdManager extends WorkerChild {

	protected AdManager(WorkerManager manager) {
		super(manager);
	}

	public final View onCreateAd(View rootView) {
		View ad = rootView.findViewById(getAdId());
		if (ad != null) {
			onAdCreated(ad);
		}
		return ad;
	}

	protected void onAdCreated(View ad) {
		// Stub method. Override in child subclasses
	}

	public void onAdResumed(View ad) {
		// Stub method. Override in child subclasses
	}

	public void onAdPaused(View ad) {
		// Stub method. Override in child subclasses
	}

	public void onAdDestroyed(View ad) {
		// Stub method. Override in child subclasses
	}

	protected int getAdId() {
		return 0;
	}

}
