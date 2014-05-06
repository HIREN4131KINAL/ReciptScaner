package wb.receipts.fragments;

import wb.receipts.ads.Ads;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import co.smartreceipts.android.fragments.ReceiptsListFragment;
import co.smartreceipts.android.model.TripRow;

import com.google.android.gms.ads.AdView;

public class AdsReceiptsListFragment extends ReceiptsListFragment {

	private AdView mAdView;

	public static AdsReceiptsListFragment newListInstance() {
		AdsReceiptsListFragment fragment = new AdsReceiptsListFragment();
		return fragment;
	}

	public static AdsReceiptsListFragment newListInstance(TripRow currentTrip) {
		if (currentTrip == null) {
			return newListInstance();
		}
		else {
			AdsReceiptsListFragment fragment = new AdsReceiptsListFragment();
			Bundle args = new Bundle();
			args.putParcelable(TripRow.PARCEL_KEY, currentTrip);
			fragment.setArguments(args);
			return fragment;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		mAdView = Ads.onCreateView(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Ads.onResume(mAdView);
	}

	@Override
	public void onPause() {
		Ads.onPause(mAdView);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		Ads.onDestroy(mAdView);
		super.onDestroy();
	}

}
