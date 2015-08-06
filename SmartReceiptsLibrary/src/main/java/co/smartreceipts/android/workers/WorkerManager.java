package co.smartreceipts.android.workers;

import co.smartreceipts.android.SmartReceiptsApplication;
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
	
	public final Logger getLogger() {
		if (mLogger == null) {
			mLogger = instantiateLogger();
		}
		return mLogger;
	}
	
	public final ImageGalleryWorker getImageGalleryWorker() {
		if (mImageGalleryWorker == null) {
			mImageGalleryWorker = instantiateImageGalleryWorker();
		}
		return mImageGalleryWorker;
	}
	
	public final AdManager getAdManager() {
		if (mAdManager == null) {
			mAdManager = instantiateAdManager();
		}
		return mAdManager;
	}
	
	public final SmartReceiptsApplication getApplication() {
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
		return new ImageGalleryWorker(this, 
									  mApplication.getPersistenceManager().getStorageManager(), 
									  mApplication.getPersistenceManager().getPreferences(),
									  mApplication.getFlex());
	}
	
	/**
	 * Protected method to enable subclasses to create custom instances
	 * @return a AdManager Instance
	 */
	protected AdManager instantiateAdManager() {
		return new AdManager(this, mApplication.getPersistenceManager().getSubscriptionCache().getSubscriptionWallet());
	}
	
}
