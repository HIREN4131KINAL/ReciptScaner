package co.smartreceipts.android.activities;

import co.smartreceipts.android.model.Trip;
import wb.android.ui.UpNavigationSlidingPaneLayout;

public interface Navigable {

	/**
	 * Inflates a ReceiptListFragment for this particular trip. If null in for the trip,
	 * this trip should be restored from persistent settings if possible.
	 * @param trip - The trip to specify (or null if it should be restored from prefs)
	 */
	public void viewReceiptsAsList(Trip trip);

}
