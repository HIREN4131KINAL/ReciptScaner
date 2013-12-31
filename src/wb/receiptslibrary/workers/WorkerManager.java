package wb.receiptslibrary.workers;

import wb.receiptslibrary.SmartReceiptsApplication;
import android.content.Context;

public class WorkerManager {

	private SmartReceiptsApplication mApplication;
	private Logger mLogger;
	private ImageGalleryWorker mImageGalleryWorker;
	private AdManager mAdManager;
	
	public WorkerManager(SmartReceiptsApplication application) {
		mApplication = application;
	}
	
	public void onDestroy() {
		mApplication = null;
		mLogger = null;
		mImageGalleryWorker = null;
		mAdManager = null;
	}
	
	public Logger getLogger() {
		if (mLogger == null) {
			mLogger = instantiateLogger();
		}
		return mLogger;
	}
	
	public ImageGalleryWorker getImageGalleryWorker() {
		if (mImageGalleryWorker == null) {
			mImageGalleryWorker = instantiateImageGalleryWorker();
		}
		return mImageGalleryWorker;
	}
	
	public AdManager getAdManager() {
		if (mAdManager == null) {
			mAdManager = instantiateAdManager();
		}
		return mAdManager;
	}
	
	public Context getContext() {
		return mApplication;
	}
	
	/**
	 * Protected method to enable subclasses to create custom instances
	 * @return a Logger Instance
	 */
	protected Logger instantiateLogger() {
		return new Logger(this);
	}
	
	/**
	 * Protected method to enable subclasses to create custom instances
	 * @return a ImageGalleryWorker Instance
	 */
	protected ImageGalleryWorker instantiateImageGalleryWorker() {
		return new ImageGalleryWorker(this, mApplication.getPersistenceManager().getStorageManager(), mApplication.getFlex());
	}
	
	/**
	 * Protected method to enable subclasses to create custom instances
	 * @return a AdManager Instance
	 */
	protected AdManager instantiateAdManager() {
		return new AdManager(this);
	}
	
}
