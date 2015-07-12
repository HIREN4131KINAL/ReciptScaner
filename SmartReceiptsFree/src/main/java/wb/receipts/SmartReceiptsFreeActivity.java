package wb.receipts;

import wb.receipts.fragments.AdsReceiptsListFragment;
import wb.receipts.fragments.AdsTripFragment;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.fragments.ReceiptsListFragment;
import co.smartreceipts.android.fragments.TripFragment;
import co.smartreceipts.android.model.Trip;

public class SmartReceiptsFreeActivity extends SmartReceiptsActivity {

	protected TripFragment getTripsFragment() {
		return AdsTripFragment.newInstance();
	}

	protected ReceiptsListFragment getReceiptsListFragment(Trip trip) {
		return AdsReceiptsListFragment.newListInstance(trip);
	}

}