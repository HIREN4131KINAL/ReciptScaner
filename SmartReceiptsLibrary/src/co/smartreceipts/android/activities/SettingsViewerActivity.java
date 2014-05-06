package co.smartreceipts.android.activities;

import android.os.Bundle;
import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.preferences.CSVColumnsListFragment;
import co.smartreceipts.android.fragments.preferences.CategoriesListFragment;
import co.smartreceipts.android.fragments.preferences.PDFColumnsListFragment;

public class SettingsViewerActivity extends WBActivity {
	
	static final String KEY_FLAG = "KeyFlag";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_main_onepane);
        
		// savedInstanceState is non-null when there is fragment state saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape). In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it. For more information, see the Fragments API guide at:
		// http://developer.android.com/guide/components/fragments.html
		if (savedInstanceState == null) {
			String key = getKey();
			if (key.equalsIgnoreCase(getString(R.string.pref_receipt_customize_categories_key))) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, CategoriesListFragment.newInstance(), CategoriesListFragment.TAG).commitAllowingStateLoss();
			}
			else if (key.equalsIgnoreCase(getString(R.string.pref_output_custom_csv_key))) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, CSVColumnsListFragment.newInstance(), CSVColumnsListFragment.TAG).commitAllowingStateLoss();
			}
			else if (key.equalsIgnoreCase(getString(R.string.pref_output_custom_pdf_key))) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, PDFColumnsListFragment.newInstance(), PDFColumnsListFragment.TAG).commitAllowingStateLoss();
			}
			else {
				finish(); //Unknown Key was passed
			}
		}
	}
	
	private String getKey() {
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(KEY_FLAG)) {
			return extras.getString(KEY_FLAG);
		}
		else {
			return new String();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		enableUpNavigation(true);
	}
	
}
