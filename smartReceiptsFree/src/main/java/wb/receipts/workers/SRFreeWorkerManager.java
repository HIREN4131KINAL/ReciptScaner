package wb.receipts.workers;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.workers.AdManager;
import co.smartreceipts.android.workers.WorkerManager;

public class SRFreeWorkerManager extends WorkerManager {

	public SRFreeWorkerManager(SmartReceiptsApplication application) {
		super(application);
	}

    @Override
    protected AdManager instantiateAdManager() {
        return new SRFreeAdManager(this);
    }
}
