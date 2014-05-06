package co.smartreceipts.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuItem;
import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.ReceiptPDFFragment;

public class ReceiptPDFActivity extends WBActivity {

	//logging variables
    static final String TAG = "ReceiptImageActivity";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_onepane);

		// savedInstanceState is non-null when there is fragment state saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape). In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it. For more information, see the Fragments API guide at:
		// http://developer.android.com/guide/components/fragments.html
		if (savedInstanceState == null) {
				final ReceiptPDFFragment fragment = ReceiptPDFFragment.newInstance();
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, fragment, ReceiptPDFFragment.TAG).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			final Intent upIntent = new Intent(this, getSmartReceiptsApplication().getTopLevelActivity());
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
	            // This activity is NOT part of this app's task, so create a new task
	            // when navigating up, with a synthesized back stack.
	            TaskStackBuilder.create(this)
	            				.addNextIntentWithParentStack(upIntent) // Add all of this activity's parents to the back stack
	            				.startActivities(); // Navigate up to the closest parent
	        }
			else {
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}
		else if (item.getItemId() == R.id.menu_main_settings) {
			SRNavUtils.showSettings(this);
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_export) {
	    	getSmartReceiptsApplication().getSettings().showExport();
            return true;
	    }/*
		else if (item.getItemId() ==  R.id.menu_main_settings) {
			final Intent intent = new Intent(this, SettingsActivity.class);;
			startActivity(intent);
			return true;
		}*/
		else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		enableUpNavigation(true);
	}

	@Override
    public String getTag() {
    	return TAG;
    }

}