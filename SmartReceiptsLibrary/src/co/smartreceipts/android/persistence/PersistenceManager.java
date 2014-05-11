package co.smartreceipts.android.persistence;

import wb.android.storage.InternalStorageManager;
import wb.android.storage.SDCardFileManager;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;
import co.smartreceipts.android.SmartReceiptsApplication;

public class PersistenceManager {

	private SmartReceiptsApplication mApplication;
	private DatabaseHelper mDatabase;
	private StorageManager mStorageManager;
	private SDCardFileManager mExternalStorageManager;
	private InternalStorageManager mInternalStorageManager;
	private Preferences mPreferences;

	public PersistenceManager(SmartReceiptsApplication application) {
		mApplication =  application;
		mStorageManager = StorageManager.getInstance(application);
		mPreferences = new Preferences(application, application.getFlex());
		mDatabase = DatabaseHelper.getInstance(application, this);
	}

	public void onDestroy() {
		mDatabase.onDestroy();
		mApplication = null;
		mStorageManager = null;
		mExternalStorageManager = null;
		mInternalStorageManager = null;
		mDatabase = null;
		mPreferences = null;
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

	public Preferences getPreferences() {
		return mPreferences;
	}

	/**
	 * Intended for robo-electric
	 * @param preferences
	 */
	public void setPreferences(Preferences preferences) {
		mPreferences = preferences;
	}

}