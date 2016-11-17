package co.smartreceipts.android.workers;

class WorkerChild {

	protected final WorkerManager mWorkerManager;
	
	protected WorkerChild(WorkerManager manager) {
		mWorkerManager = manager;
	}
	
	protected WorkerManager getWorkerManager() {
		return mWorkerManager;
	}
}
