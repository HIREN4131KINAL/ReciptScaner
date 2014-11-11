package co.smartreceipts.android.fragments;

import co.smartreceipts.android.model.Trip;
import wb.android.loaders.SharedPreferencesLoader;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
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
import co.smartreceipts.android.testutils.Utils;



public class ReceiptsFragment extends WBListFragment implements OnNavigationListener,
																LoaderCallbacks<SharedPreferences> {

	public static final String TAG = "ReceiptsFragment";

    //Preferences
	protected static final String PREFERENCES = SharedPreferenceDefinitions.ReceiptFragment_Preferences.toString();
  	private static final String PREFERENCE_TRIP_NAME = "tripName";

	protected Trip mCurrentTrip;
	protected Navigable mNavigator;

	public static ReceiptsListFragment newListInstance() {
		ReceiptsListFragment fragment = new ReceiptsListFragment();
		return fragment;
	}

	public static ReceiptsListFragment newListInstance(Trip currentTrip) {
		if (currentTrip == null) {
			return newListInstance();
		}
		else {
			ReceiptsListFragment fragment = new ReceiptsListFragment();
			Bundle args = new Bundle();
			args.putParcelable(Trip.PARCEL_KEY, currentTrip);
			fragment.setArguments(args);
			return fragment;
		}
	}

	public static ReceiptsChartFragment newChartInstance() {
		ReceiptsChartFragment fragment = new ReceiptsChartFragment();
		return fragment;
	}

	public static ReceiptsChartFragment newChartInstance(Trip currentTrip) {
		if (currentTrip == null) {
			return newChartInstance();
		}
		else {
			ReceiptsChartFragment fragment = new ReceiptsChartFragment();
			Bundle args = new Bundle();
			args.putParcelable(Trip.PARCEL_KEY, currentTrip);
			fragment.setArguments(args);
			return fragment;
		}
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
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onCreate");
		}
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void onPause() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onPause");
		}
		super.onPause();
		if (mCurrentTrip != null) {
			// Save persistent data state
	    	SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES, 0);
	    	SharedPreferences.Editor editor = preferences.edit();
	    	editor.putString(PREFERENCE_TRIP_NAME, mCurrentTrip.getName());
	    	if (Utils.ApiHelper.hasGingerbread()) {
				editor.apply();
			} else {
				editor.commit();
			}
    	}
		getPersistenceManager().getDatabase().unregisterReceiptRowListener();
	}

	@Override
	public void onResume() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "onResume");
		}
		super.onResume();
		ensureValidCurrentTrip();
	}
	
	protected void ensureValidCurrentTrip() {
		if (mCurrentTrip == null) {
			if (getArguments() != null) {
				Parcelable parcel = getArguments().getParcelable(Trip.PARCEL_KEY);
				if (parcel == null || !(parcel instanceof Trip)) {
					restoreData();
				}
				else {
					setTrip((Trip)parcel);
				}
			}
			else {
				restoreData();
			}
		}
	}

	// Restore persistent data
	protected void restoreData() {
		restoreDataHelper(getActivity().getSharedPreferences(PREFERENCES, 0));
	}

	private void restoreDataHelper(SharedPreferences preferences) {
		if (mCurrentTrip == null) {
			final DatabaseHelper db = getPersistenceManager().getDatabase();
			final String tripName = preferences.getString(PREFERENCE_TRIP_NAME, "");
			setTrip(db.getTripByName(tripName));
		}
	}

	@Override
	public Loader<SharedPreferences> onCreateLoader(int id, Bundle args) {
		return new SharedPreferencesLoader(getActivity(), PREFERENCES);
	}

	@Override
	public void onLoadFinished(Loader<SharedPreferences> loader, SharedPreferences prefs) {
		restoreDataHelper(prefs);
	}

	@Override
	public void onLoaderReset(Loader<SharedPreferences> loader) {
		// Unused
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
    	getActivity().setTitle(mCurrentTrip.getCurrencyFormattedPrice() + " - " + mCurrentTrip.getName());
    	if (getPersistenceManager().getPreferences().isShowReceiptID()) {
    		getSupportActionBar().setSubtitle(getString(R.string.next_id, getPersistenceManager().getDatabase().getNextReceiptAutoIncremenetIdSerial()));
    	}
    	else {
            getSupportActionBar().setSubtitle(getString(R.string.daily_total, mCurrentTrip.getCurrencyFormattedDailySubTotal()));
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
