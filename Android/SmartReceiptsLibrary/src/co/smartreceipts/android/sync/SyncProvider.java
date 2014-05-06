package co.smartreceipts.android.sync;

import java.sql.Date;

import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.model.TripRow;

public interface SyncProvider {

	/**
	 * Registers a {@link SyncListener} instance with this {@link SyncProvider} implementation
	 * 
	 * @param listener
	 *            - the {@link SyncListener} to alert when certain events complete
	 */
	public void registerListener(SyncListener listener);

	/**
	 * Queries all trips that are currently available for the current {@link SyncUser}
	 */
	public void queryTrips();

	/**
	 * Queries all trips that occur after a given date for the current {@link SyncUser}
	 * 
	 * @param - The {@link Date} to search after
	 */
	public void queryTripsAfter(Date date);

	/**
	 * Uploads a trip to the sync provider for the current {@link SyncUser}
	 * 
	 * @param trip
	 *            - the {@link TripRow} to insert
	 */
	public void insertTrip(TripRow trip);

	/**
	 * Updates a trip that current exists in the current {@link SyncProvider} implementation or uploads if it does not
	 * already exist.
	 * 
	 * @param trip
	 *            - the {@link TripRow} to update
	 */
	public void updateTrip(TripRow trip);

	/**
	 * Removes a trip from trip the current {@link SyncProvider} implementation
	 * 
	 * @param trip
	 *            - the {@link TripRow} to delete
	 */
	public void deleteTrip(TripRow trip);

	/**
	 * Queries all receipts that are currently available for the current {@link SyncUser}
	 */
	public void queryReceipts();

	/**
	 * Uploads a receipt to the sync provider for the current {@link SyncUser}
	 * 
	 * @param receipt
	 *            - the {@link ReceiptRow} to insert
	 */
	public void insertReceipt(ReceiptRow receipt);

	/**
	 * Updates a receipt that current exists in the current {@link SyncProvider} implementation or uploads if it does
	 * not already exist.
	 * 
	 * @param receipt
	 *            - the {@link ReceiptRow} to update
	 */
	public void updateReceipt(ReceiptRow receipt);

	/**
	 * Removes a receipt from receipt the current {@link SyncProvider} implementation
	 * 
	 * @param receipt
	 *            - the {@link ReceiptRow} to delete
	 */
	public void deleteReceipt(ReceiptRow receipt);

}
