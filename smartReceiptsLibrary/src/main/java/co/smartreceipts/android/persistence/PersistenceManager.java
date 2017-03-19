package co.smartreceipts.android.persistence;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import co.smartreceipts.android.settings.UserPreferenceManager;
import wb.android.storage.InternalStorageManager;
import wb.android.storage.SDCardFileManager;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;

@Singleton
public class PersistenceManager {

	@Inject
	Context context;
	@Inject
	UserPreferenceManager preferenceManager;
    @Inject
    StorageManager storageManager;
	@Inject
	DatabaseHelper database;

	private SDCardFileManager mExternalStorageManager;
	private InternalStorageManager mInternalStorageManager;

	@Inject
	public PersistenceManager() {
	}

	public void onDestroy() {
		database.onDestroy();
		storageManager = null;
		mExternalStorageManager = null;
		mInternalStorageManager = null;
		database = null;
	}

	public DatabaseHelper getDatabase() {
		// TODO: 15.03.2017 check is it necessary
//		if (database == null || !database.isOpen()) {
//			database = DatabaseHelper.getInstance(mApplication, this);
//		}
		return database;
	}

	public StorageManager getStorageManager() {
		return storageManager;
	}

	public SDCardFileManager getExternalStorageManager() throws SDCardStateException {
		if (mExternalStorageManager == null) {
			if (storageManager == null) {
				getStorageManager();
			}
			if (storageManager instanceof SDCardFileManager) {
				mExternalStorageManager = (SDCardFileManager) storageManager;
			}
			else {
				mExternalStorageManager = StorageManager.getExternalInstance(context);
			}
		}
		return mExternalStorageManager;
	}

	public InternalStorageManager getInternalStorageManager() {
		if (mInternalStorageManager == null) {
			if (storageManager == null) {
				getStorageManager();
			}
			if (storageManager instanceof InternalStorageManager) {
				mInternalStorageManager = (InternalStorageManager) storageManager;
			}
			else {
				mInternalStorageManager = StorageManager.getInternalInstance(context);
			}
		}
		return mInternalStorageManager;
	}

    @NonNull
    public UserPreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

}