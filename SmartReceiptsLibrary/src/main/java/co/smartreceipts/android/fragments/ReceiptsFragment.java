package co.smartreceipts.android.fragments;

import co.smartreceipts.android.model.Trip;
import wb.android.loaders.SharedPreferencesLoader;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.util.Log;
import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.Attachable;
import co.smartreceipts.android.activities.Navigable;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions;
import co.smartreceipts.android.utils.Utils;



public class ReceiptsFragment extends WBListFragment implements OnNavigationListener {

	public static final String TAG = "ReceiptsFragment";

	protected Trip mCurrentTrip;
	protected Navigable mNavigator;

	public static ReceiptsListFragment newListInstance(@NonNull Trip currentTrip) {
        final ReceiptsListFragment fragment = new ReceiptsListFragment();
        Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, currentTrip);
        fragment.setArguments(args);
        return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof Navigable && activity instanceof Attachable) {
			mNavigator = (Navigable) activity;
		}
		else {
			throw new ClassCastException("The ReceiptFragment's Activity must extend the Navigable interfaces");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onCreate");
		}
		setHasOptionsMenu(true);
        mCurrentTrip = getArguments().getParcelable(Trip.PARCEL_KEY);
	}

    @Override
    public void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onResume");
        }
    }

	@Override
	public void onPause() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onPause");
		}
		super.onPause();
		getPersistenceManager().getDatabase().unregisterReceiptRowListener();
	}

	public void setTrip(Trip trip) {
		if (trip == null) {
			mNavigator.viewTrips();
		}
		else {
			if (BuildConfig.DEBUG) {
				Log.i(TAG, "Setting Trip: " + trip.getName());
			}
			mCurrentTrip = trip;
			getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
			updateActionBarTitle();
		}
	}

	protected void updateActionBarTitle() {
		if (mCurrentTrip == null) {
			return;
		}
    	getActivity().setTitle(mCurrentTrip.getPrice().getCurrencyFormattedPrice() + " - " + mCurrentTrip.getName());
    	if (getPersistenceManager().getPreferences().isShowReceiptID()) {
    		getSupportActionBar().setSubtitle(getString(R.string.next_id, getPersistenceManager().getDatabase().getNextReceiptAutoIncremenetIdSerial()));
    	}
    	else {
            getSupportActionBar().setSubtitle(getString(R.string.daily_total, mCurrentTrip.getDailySubTotal().getCurrencyFormattedPrice()));
    	}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		String item = getResources().getStringArray(R.array.actionbar_subtitles)[itemPosition];
		if (!(this instanceof ReceiptsListFragment) && item.equals(getString(R.string.actionbar_subtitle_list))) {
			mNavigator.viewReceiptsAsList(mCurrentTrip);
			return true;
		}
		else if (!(this instanceof ReceiptsChartFragment) && item.equals(getString(R.string.actionbar_subtitle_chart))) {
			mNavigator.viewReceiptsAsChart(mCurrentTrip);
			return true;
		}
		else {
			return false;
		}
	}

}
