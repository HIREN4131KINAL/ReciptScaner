package co.smartreceipts.android.activities;

import android.os.Bundle;
import android.view.MenuItem;

import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.DistanceFragment;

/**
 * Serves as the entry point for showing what is available in terms of distance line item entries
 */
public class DistanceActiity extends WBActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_onepane);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_list, DistanceFragment.newInstance(), DistanceFragment.TAG).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableUpNavigation(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            finish();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }
}
