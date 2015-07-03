package co.smartreceipts.android.activities;

import android.os.Bundle;
import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.preferences.CSVColumnsListFragment;
import co.smartreceipts.android.fragments.preferences.CategoriesListFragment;
import co.smartreceipts.android.fragments.preferences.PDFColumnsListFragment;
import co.smartreceipts.android.fragments.preferences.PaymentMethodsListFragment;

public class SettingsViewerActivity extends WBActivity {
	
	static final String KEY_FLAG = "KeyFlag";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_main_onepane);
        
		if (savedInstanceState == null) {
			final String key = getKey();
			if (key.equalsIgnoreCase(getString(R.string.pref_receipt_customize_categories_key))) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, CategoriesListFragment.newInstance(), CategoriesListFragment.TAG).commitAllowingStateLoss();
			}
			else if (key.equalsIgnoreCase(getString(R.string.pref_output_custom_csv_key))) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, CSVColumnsListFragment.newInstance(), CSVColumnsListFragment.TAG).commitAllowingStateLoss();
			}
			else if (key.equalsIgnoreCase(getString(R.string.pref_output_custom_pdf_key))) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, PDFColumnsListFragment.newInstance(), PDFColumnsListFragment.TAG).commitAllowingStateLoss();
			}
			else if (key.equals(getString(R.string.pref_receipt_payment_methods_key))) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, PaymentMethodsListFragment.newInstance(), PaymentMethodsListFragment.TAG).commitAllowingStateLoss();
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
			return "";
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		enableUpNavigation(true);
	}
	
}
