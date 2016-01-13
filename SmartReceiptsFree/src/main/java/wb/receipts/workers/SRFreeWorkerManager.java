package wb.receipts.workers;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.workers.AdManager;
import co.smartreceipts.android.workers.Logger;
import co.smartreceipts.android.workers.WorkerManager;

public class SRFreeWorkerManager extends WorkerManager {

	public SRFreeWorkerManager(SmartReceiptsApplication application) {
		super(application);
	}

	@Override
	protected Logger instantiateLogger() {
		return new SRFreeLogger(this);
	}

    @Override
    protected AdManager instantiateAdManager() {
        return new SRFreeAdManager(this);
    }
}
