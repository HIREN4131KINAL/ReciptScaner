package wb.receiptslibrary.fragments;

import wb.android.flex.Flex;
import wb.receiptslibrary.SmartReceiptsApplication;
import wb.receiptslibrary.date.DateManager;
import wb.receiptslibrary.persistence.PersistenceManager;
import wb.receiptslibrary.workers.WorkerManager;
import android.app.Application;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;

public class WBFragment extends SherlockFragment {

	private SmartReceiptsApplication mApplication;
	private DateManager mDateManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = getSmartReceiptsApplication();
		setRetainInstance(true);
	}
	
	protected Flex getFlex() {
		return mApplication.getFlex();
	}
	
	protected String getFlexString(int id) {
		return mApplication.getFlex().getString(getSherlockActivity(), id);
	}
	
	protected DateManager getDateManager() {
		if (mDateManager == null) {
			mDateManager = new DateManager(getSherlockActivity(), mApplication.getPersistenceManager().getPreferences());
		}
		return mDateManager;
	}
	
	protected PersistenceManager getPersistenceManager() {
		return mApplication.getPersistenceManager();
	}
	
	protected WorkerManager getWorkerManager() {
		return mApplication.getWorkerManager();
	}
	
	public SmartReceiptsApplication getSmartReceiptsApplication() {
		if (mApplication == null) {
			final Application application = getSherlockActivity().getApplication();
			if (application instanceof SmartReceiptsApplication) {
				mApplication = (SmartReceiptsApplication) application;
			}
			else {
				throw new RuntimeException("The Application must be an instance a SmartReceiptsApplication");
			}
		}
		return mApplication;
	}
	
}