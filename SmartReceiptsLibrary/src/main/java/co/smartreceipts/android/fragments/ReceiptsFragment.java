package co.smartreceipts.android.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;

import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Trip;

public class ReceiptsFragment extends WBListFragment {

    public static final String TAG = "ReceiptsFragment";

    protected Trip mCurrentTrip;

    public static ReceiptsListFragment newListInstance(@NonNull Trip currentTrip) {
        final ReceiptsListFragment fragment = new ReceiptsListFragment();
        Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, currentTrip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentTrip = getArguments().getParcelable(Trip.PARCEL_KEY);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void updateActionBarTitle(boolean updateSubtitle) {
        if (mCurrentTrip == null) {
            return;
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && getUserVisibleHint()) {
            if (updateSubtitle) {
                if (getPersistenceManager().getPreferences().isShowReceiptID()) {
                    actionBar.setSubtitle(getString(R.string.next_id, getPersistenceManager().getDatabase().getNextReceiptAutoIncremenetIdSerial()));
                } else {
                    actionBar.setSubtitle(getString(R.string.daily_total, mCurrentTrip.getDailySubTotal().getCurrencyFormattedPrice()));
                }
            }
        }
    }

}
