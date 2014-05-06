package co.smartreceipts.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuItem;
import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.ReceiptImageFragment;
import co.smartreceipts.android.model.ReceiptRow;
import co.smartreceipts.android.model.TripRow;

public class ReceiptImageActivity extends WBActivity {

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
			// Create the detail fragment and add it to the activity using a fragment transaction.
			final Intent intent = getIntent();
			if (intent.hasExtra(TripRow.PARCEL_KEY) && intent.hasExtra(ReceiptRow.PARCEL_KEY)) {
				TripRow trip = (TripRow) intent.getParcelableExtra(TripRow.PARCEL_KEY);
				ReceiptRow receipt = (ReceiptRow) intent.getParcelableExtra(ReceiptRow.PARCEL_KEY);
				final ReceiptImageFragment fragment = ReceiptImageFragment.newInstance(receipt, trip);
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, fragment, ReceiptImageFragment.TAG).commit();
			}
			else {
				final ReceiptImageFragment fragment = ReceiptImageFragment.newInstance();
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, fragment, ReceiptImageFragment.TAG).commit();
			}
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
	    }
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
