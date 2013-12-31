package wb.receiptslibrary.activities;

import wb.receiptslibrary.BuildConfig;
import wb.receiptslibrary.SmartReceiptsApplication;
import wb.receiptslibrary.utils.Utils;
import android.app.Application;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class WBActivity extends SherlockFragmentActivity {

	private SmartReceiptsApplication mApplication;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) {
        	Utils.enableStrictMode();
        }
		setResult(RESULT_CANCELED); // In case the user backs out
		mApplication = getSmartReceiptsApplication();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mApplication.setCurrentActivity(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mApplication.setCurrentActivity(null);
		mApplication.getPersistenceManager().getPreferences().writeLastActivityTag(getTag());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mApplication = null;
	}
	
	protected void enableUpNavigation(boolean enable) {
		getSupportActionBar().setHomeButtonEnabled(enable);
		getSupportActionBar().setDisplayHomeAsUpEnabled(enable);
	}
	
	public SmartReceiptsApplication getSmartReceiptsApplication() {
		if (mApplication == null) {
			final Application application = getApplication();
			if (application instanceof SmartReceiptsApplication) {
				mApplication = (SmartReceiptsApplication) application;
			}
			else {
				throw new RuntimeException("The Application must be an instance a SmartReceiptsApplication");
			}
		}
		return mApplication;
	}
	
	public String getTag() {
		return "";
	}

}