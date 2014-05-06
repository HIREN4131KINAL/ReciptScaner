package co.smartreceipts.android.activities;

import wb.android.ui.UpNavigationSlidingPaneLayout;
import co.smartreceipts.android.model.TripRow;

public interface Navigable extends UpNavigationSlidingPaneLayout.PanelSlideListener {

	/**
	 * Inflates a ReceiptListFragment for this particular trip. If null in for the trip,
	 * this trip should be restored from persistent settings if possible.
	 * @param trip - The trip to specify (or null if it should be restored from prefs)
	 */
	public void viewReceiptsAsList(TripRow trip);
	
	/**
	 * Inflates a ReceiptChartFragment for this particular trip. If null in for the trip,
	 * this trip should be restored from persistent settings if possible.
	 * @param trip - The trip to specify (or null if it should be restored from prefs)
	 */
	public void viewReceiptsAsChart(TripRow trip);
	
	/**
	 * Gets the ActionBarController that is used to navigate within the ActionBar menus
	 * @return the ActionBarController
	 
	public ActionBarController getActionBarController();
	*/
	
	/**
	 * Displays the list of trips (which should already be inflated during the 
	 * initial creation process of the app. 
	 */
	public void viewTrips();
	
}
