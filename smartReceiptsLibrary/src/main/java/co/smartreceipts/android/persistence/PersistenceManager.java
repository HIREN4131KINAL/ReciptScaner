package co.smartreceipts.android.persistence;

import android.support.annotation.NonNull;

import co.smartreceipts.android.purchases.SubscriptionCache;
import co.smartreceipts.android.settings.UserPreferenceManager;
import wb.android.storage.InternalStorageManager;
import wb.android.storage.SDCardFileManager;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;
import co.smartreceipts.android.SmartReceiptsApplication;

public class PersistenceManager {

	private final SmartReceiptsApplication mApplication;
	private DatabaseHelper mDatabase;
	private StorageManager mStorageManager;
	private SDCardFileManager mExternalStorageManager;
	private InternalStorageManager mInternalStorageManager;
    private final UserPreferenceManager preferenceManager;
    private final SubscriptionCache mSubscriptionCache;

	public PersistenceManager(SmartReceiptsApplication application, SubscriptionCache subscriptionCache) {
		mApplication =  application;
		mStorageManager = StorageManager.getInstance(application);
        mSubscriptionCache = subscriptionCache;

        this.preferenceManager = new UserPreferenceManager(application);
	}

    public void initialize() {
        preferenceManager.initialize();

        // TODO: Fix this anti-pattern with proper dependency injection
        mDatabase = DatabaseHelper.getInstance(mApplication, this);
    }

	public void onDestroy() {
		mDatabase.onDestroy();
		mStorageManager = null;
		mExternalStorageManager = null;
		mInternalStorageManager = null;
		mDatabase = null;
	}

	public DatabaseHelper getDatabase() {
		if (mDatabase == null || !mDatabase.isOpen()) {
			mDatabase = DatabaseHelper.getInstance(mApplication, this);
		}
		return mDatabase;
	}

	public StorageManager getStorageManager() {
		return mStorageManager;
	}

	public SDCardFileManager getExternalStorageManager() throws SDCardStateException {
		if (mExternalStorageManager == null) {
			if (mStorageManager == null) {
				getStorageManager();
			}
			if (mStorageManager instanceof SDCardFileManager) {
				mExternalStorageManager = (SDCardFileManager) mStorageManager;
			}
			else {
				mExternalStorageManager = StorageManager.getExternalInstance(mApplication);
			}
		}
		return mExternalStorageManager;
	}

	public InternalStorageManager getInternalStorageManager() {
		if (mInternalStorageManager == null) {
			if (mStorageManager == null) {
				getStorageManager();
			}
			if (mStorageManager instanceof InternalStorageManager) {
				mInternalStorageManager = (InternalStorageManager) mStorageManager;
			}
			else {
				mInternalStorageManager = StorageManager.getInternalInstance(mApplication);
			}
		}
		return mInternalStorageManager;
	}

    @NonNull
    public UserPreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    public SubscriptionCache getSubscriptionCache() {
        return mSubscriptionCache;
    }

}