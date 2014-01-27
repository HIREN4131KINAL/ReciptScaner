package co.smartreceipts.android.activities;

import co.smartreceipts.android.model.TripRow;
import wb.android.ui.UpNavigationSlidingPaneLayout;

public interface Navigable extends UpNavigationSlidingPaneLayout.PanelSlideListener {

	/**
	 * Inflates a ReceiptFragment for this particular trip. If null in for the trip,
	 * this trip should be restored from persistent settings if possible.
	 * @param trip - The trip to specify (or null if it should be restored from prefs)
	 */
	public void viewReceipts(TripRow trip);
	
	/**
	 * Inflates a ReceiptFragment for this particular trip. If null in for the trip,
	 * this trip should be restored from persistent settings if possible.
	 * @param trip - The trip to specify (or null if it should be restored from prefs)
	 * @param displayTrip - defines if the panel closed in order to display this trip immediately
	 *
	public void viewReceipts(TripRow trip, boolean displayTrip);
	 */
	
	/**
	 * Displays the list of trips (which should already be inflated during the 
	 * initial creation process of the app. 
	 */
	public void viewTrips();
	
}
