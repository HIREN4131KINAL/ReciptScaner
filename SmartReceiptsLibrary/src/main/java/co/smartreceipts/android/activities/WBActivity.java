package co.smartreceipts.android.activities;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.utils.Utils;

public class WBActivity extends AppCompatActivity {

	private SmartReceiptsApplication mApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) {
        	Utils.enableStrictMode();
        }
		setResult(RESULT_CANCELED); // In case the user backs out
		mApplication = getSmartReceiptsApplication();
        // getSupportActionBar().setDisplayShowTitleEnabled(true); //Use this by default - override as needed
        if (!Utils.ApiHelper.hasICS()) { //Pre-ICS version need this set manually
			// getSupportActionBar().setLogo(R.drawable.logo);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
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
		//mApplication.getPersistenceManager().getPreferences().writeLastActivityTag(getTag());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mApplication = null;
	}

	protected void enableUpNavigation(boolean enable) {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(enable);
            actionBar.setDisplayHomeAsUpEnabled(enable);
        }
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