package wb.receiptslibrary;

import wb.receiptslibrary.model.ReceiptRow;
import wb.receiptslibrary.model.TripRow;

public interface Navigable {

	public void naviagteBackwards();
	public void viewTrips();
	public void viewReceipts(TripRow trip);
	public void viewReceiptImage(ReceiptRow receipt, TripRow trip);
	public void viewSettings();
	public void viewCategories();
	public void viewAbout();
	public void viewExport();
	
}
