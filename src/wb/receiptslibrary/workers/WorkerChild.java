package wb.receiptslibrary.workers;

class WorkerChild {

	protected final WorkerManager mWorkerManager;
	
	WorkerChild(WorkerManager manager) {
		mWorkerManager = manager;
	}
	
	WorkerManager getWorkerManager() {
		return mWorkerManager;
	}
}
