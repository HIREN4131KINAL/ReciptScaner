package wb.receiptslibrary.persistence;

import wb.android.storage.InternalStorageManager;
import wb.android.storage.SDCardFileManager;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;
import wb.receiptslibrary.SmartReceiptsActivity;

public class PersistenceManager {

	private SmartReceiptsActivity mActivity;
	private DatabaseHelper mDatabase;
	private StorageManager mStorageManager;
	private SDCardFileManager mExternalStorageManager;
	private InternalStorageManager mInternalStorageManager;
	private Preferences mPreferences;
	
	public PersistenceManager(SmartReceiptsActivity activity) {
		mActivity = activity;
		mStorageManager = StorageManager.getInstance(mActivity);
		mDatabase = DatabaseHelper.getInstance(mActivity);
		mPreferences = new Preferences(mActivity);
	}
	
	public void onDestroy() {
		mDatabase.onDestroy();
	}
	
	public DatabaseHelper getDatabase() {
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
				mExternalStorageManager = StorageManager.getExternalInstance(mActivity);
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
				mInternalStorageManager = StorageManager.getInternalInstance(mActivity);
			}
		}
		return mInternalStorageManager;
	}
	
	public Preferences getPreferences() {
		return mPreferences;
	}
	
}