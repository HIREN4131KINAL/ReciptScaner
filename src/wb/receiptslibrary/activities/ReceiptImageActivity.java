package wb.receiptslibrary.activities;

import wb.receiptslibrary.R;
import wb.receiptslibrary.fragments.ReceiptImageFragment;
import wb.receiptslibrary.model.ReceiptRow;
import wb.receiptslibrary.model.TripRow;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class ReceiptImageActivity extends WBActivity {

	//logging variables
    static final String TAG = "ReceiptImageActivity";
	
    private boolean mIsDualPane;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_main_onepane);
        mIsDualPane = getResources().getBoolean(R.bool.isTablet);

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
		getSupportMenuInflater().inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			final Intent upIntent = (mIsDualPane) ? new Intent(this, getSmartReceiptsApplication().getTopLevelActivity()) : new Intent(this, ReceiptsActivity.class);
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
	    	getSmartReceiptsApplication().getSettings().showSettingsMenu(this);
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_about) {
            getSmartReceiptsApplication().getSettings().showAbout(this);
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_categories) {
	    	getSmartReceiptsApplication().getSettings().showCategoriesMenu(this);
            return true;
	    }
	    else if (item.getItemId() == R.id.menu_main_csv) {
	    	getSmartReceiptsApplication().getSettings().showCustomCSVMenu();
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
