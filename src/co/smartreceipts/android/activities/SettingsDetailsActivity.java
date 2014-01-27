package co.smartreceipts.android.activities;

import co.smartreceipts.android.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.view.MenuItem;

public class SettingsDetailsActivity extends WBActivity {
	
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
			Fragment fragment = SettingsActivity.getFragmentFromClassPath(getIntent().getStringExtra(SettingsActivity.ExtraCodes.FRAGMENT_EXTRA));
			getSupportFragmentManager().beginTransaction().replace(R.id.content_list, fragment).commit();
			//TODO: Add fragment tag
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		enableUpNavigation(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				NavUtils.navigateUpTo(this, new Intent(this, SmartReceiptsActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
