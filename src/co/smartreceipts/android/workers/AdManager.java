package co.smartreceipts.android.workers;

import android.view.View;

public class AdManager extends WorkerChild {

	protected AdManager(WorkerManager manager) {
		super(manager);
	}
	
	public final void handleAd(View rootView) {
		View ad = rootView.findViewById(getAdId());
		if (ad != null) {
			onAdReceived(rootView, ad);
		}
	}
	
	protected void onAdReceived(View rootView, View ad) {
		// Stub method. Override in child subclasses
	}
	
	protected int getAdId() {
		return 0;
	}

}
