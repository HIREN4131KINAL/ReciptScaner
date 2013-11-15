package wb.receiptslibrary.workers;

import wb.receiptslibrary.SmartReceiptsActivity;

public class WorkerManager {

	private SmartReceiptsActivity mSmartReceiptsActivity;
	private Logger mLogger;
	private Toaster mToaster;
	private ImageGalleryWorker mImageGalleryWorker;
	
	public WorkerManager(SmartReceiptsActivity activity) {
		mSmartReceiptsActivity = activity;
	}
	
	public Logger getLogger() {
		if (mLogger == null) {
			mLogger = new Logger(this);
		}
		return mLogger;
	}
	
	public Toaster getToaster() {
		if (mToaster == null){
			mToaster = new Toaster(this);
		}
		return mToaster;
	}
	
	public ImageGalleryWorker getImageGalleryWorker() {
		if (mImageGalleryWorker == null) {
			mImageGalleryWorker = new ImageGalleryWorker(this);
		}
		return mImageGalleryWorker;
	}
	
	SmartReceiptsActivity getSmartReceiptsActivity() {
		return mSmartReceiptsActivity;
	}
	
}
