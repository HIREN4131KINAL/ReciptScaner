package wb.receiptslibrary.activities;

import wb.receiptslibrary.BuildConfig;
import wb.receiptslibrary.R;
import wb.receiptslibrary.fragments.preferences.SettingsList;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

public class SettingsActivity extends WBActivity {
	
	public static final String TAG = "SettingsActivity";
	
	private boolean mIsDualPane;
	
	public static class ExtraCodes {
		public static final String FRAGMENT_EXTRA = "android.support.v4.app.Fragment"; 
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mIsDualPane = getResources().getBoolean(R.bool.isTablet);
        
        setContentView(R.layout.activity_main);
        
    	// savedInstanceState is non-null when there is fragment state saved from previous configurations of this activity
 		// (e.g. when rotating the screen from portrait to landscape). In this case, the fragment will automatically be re-added
 		// to its container so we don't need to manually add it. For more information, see the Fragments API guide at:
 		// http://developer.android.com/guide/components/fragments.html
 		if (savedInstanceState == null) {
 			if (BuildConfig.DEBUG) Log.d(TAG, "Creating new layout");
 			displayLayout();
 		}
	}
	
    private void displayLayout() {
		if (getIntent().hasExtra(ExtraCodes.FRAGMENT_EXTRA)) { //We already have a Fragment
			final String fragmentClassPath = getIntent().getStringExtra(ExtraCodes.FRAGMENT_EXTRA);
			if (mIsDualPane) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, new SettingsList()).commit();
				getSupportFragmentManager().beginTransaction().replace(R.id.content_details, getFragmentFromClassPath(fragmentClassPath)).commit();
			}
			else {
				final Intent intent = new Intent(this, SettingsDetailsActivity.class);
				intent.putExtra(ExtraCodes.FRAGMENT_EXTRA, fragmentClassPath);
				startActivity(intent);
			}
 		}
		else {
			getSupportFragmentManager().beginTransaction().replace(R.id.content_list, new SettingsList()).commit();
		}	
    }
    
    public static final Fragment getFragmentFromClassPath(String classPath) {
    	try {
			Object fragmentObject = Class.forName(classPath).newInstance();
			if (fragmentObject instanceof Fragment) {
				return (Fragment) fragmentObject;
			}
			else {
				throw new RuntimeException("A Fragment instance was not passed to SettingsDetailActivity for reflection.");
			}
		} catch (Exception e) {
			throw new RuntimeException("A Fragment instance was not passed to SettingsDetailActivity for reflection.");
		}
    }

}