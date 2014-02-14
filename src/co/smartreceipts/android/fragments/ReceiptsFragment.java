package co.smartreceipts.android.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import co.smartreceipts.android.BuildConfig;
import co.smartreceipts.android.activities.Attachable;
import co.smartreceipts.android.activities.Navigable;
import co.smartreceipts.android.model.TripRow;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.utils.Utils;

public class ReceiptsFragment extends WBListFragment {

	public static final String TAG = "ReceiptsFragment";
    
    //Preferences
	protected static final String PREFERENCES = "ReceiptsFragment.xml";
  	private static final String PREFERENCE_TRIP_NAME = "tripName";
	
	protected TripRow mCurrentTrip;
	protected Navigable mNavigator;	
	
	public static ReceiptsListFragment newListInstance() {
		ReceiptsListFragment fragment = new ReceiptsListFragment();
		return fragment;
	}
	
	public static ReceiptsListFragment newListInstance(TripRow currentTrip) {
		if (currentTrip == null) {
			return newListInstance();
		}
		else {
			ReceiptsListFragment fragment = new ReceiptsListFragment();
			Bundle args = new Bundle();
			args.putParcelable(TripRow.PARCEL_KEY, currentTrip);
			fragment.setArguments(args);
			return fragment;
		}
	}
	
	public static ReceiptsChartFragment newChartInstance() {
		ReceiptsChartFragment fragment = new ReceiptsChartFragment();
		return fragment;
	}
	
	public static ReceiptsChartFragment newChartInstance(TripRow currentTrip) {
		if (currentTrip == null) {
			return newChartInstance();
		}
		else {
			ReceiptsChartFragment fragment = new ReceiptsChartFragment();
			Bundle args = new Bundle();
			args.putParcelable(TripRow.PARCEL_KEY, currentTrip);
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
		if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getWorkerManager().getLogger().logInformation("/ReceiptView");
	}
	
	@Override
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void onPause() {
		if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
		super.onPause();
		if (mCurrentTrip != null) {
			// Save persistent data state
	    	SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES, 0);
	    	SharedPreferences.Editor editor = preferences.edit();
	    	editor.putString(PREFERENCE_TRIP_NAME, mCurrentTrip.getName());
	    	if (Utils.ApiHelper.hasGingerbread())
	    		editor.apply();
	    	else
	    		editor.commit();
    	}
		getPersistenceManager().getDatabase().unregisterReceiptRowListener();
	}
	
	@Override
	public void onResume() {
		if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
		super.onResume();
		if (mCurrentTrip == null) {
			if (getArguments() != null) {
				Parcelable parcel = getArguments().getParcelable(TripRow.PARCEL_KEY);
				if (parcel == null || !(parcel instanceof TripRow)) {
					restoreData();
				}
				else {
					setTrip((TripRow)parcel);
				}
			}
			else {
				restoreData();
			}
		}
	}
	
	// Restore persistent data
	protected void restoreData() {
		final DatabaseHelper db = getPersistenceManager().getDatabase();
		SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES, 0);
		final String tripName = preferences.getString(PREFERENCE_TRIP_NAME, "");
		setTrip(db.getTripByName(tripName));
	}
	
	public void setTrip(TripRow trip) {
		if (trip == null) {
			mNavigator.viewTrips();
		}
		else {
			if (BuildConfig.DEBUG) Log.i(TAG, "Setting Trip: " + trip.getName());
			mCurrentTrip = trip;
			getPersistenceManager().getDatabase().getReceiptsParallel(mCurrentTrip);
			this.updateActionBarTitle();
		}
	}
	
	final void updateActionBarTitle() {
    	getSherlockActivity().getSupportActionBar().setTitle(mCurrentTrip.getCurrencyFormattedPrice() + " - " + mCurrentTrip.getName());
	}
	
}
