package co.smartreceipts.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MenuItem;

import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.DistanceFragment;
import co.smartreceipts.android.fragments.TripFragment;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

/**
 * Serves as the entry point for showing what is available in terms of distance line item entries
 */
public class DistanceActivity extends WBActivity {

    private static final String TAG = DistanceActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_onepane);

        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            if (intent.hasExtra(Trip.PARCEL_KEY)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_list, DistanceFragment.newInstance((Trip) intent.getParcelableExtra(Trip.PARCEL_KEY)), DistanceFragment.TAG).commit();
            }
            else {
                Log.e(TAG, "Killing DistanceActivity, since no parent trip was provided");
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableUpNavigation(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.menu_main_settings) {
            SRNavUtils.showSettings(this);
            return true;
        }
        else if (item.getItemId() == R.id.menu_main_export) {
            final Fragment distanceFragment = getSupportFragmentManager().findFragmentByTag(DistanceFragment.TAG);
            getSmartReceiptsApplication().getSettings().showExport(distanceFragment);
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }
}
