package wb.receipts.fragments;

import wb.receipts.ads.Ads;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import co.smartreceipts.android.fragments.TripFragment;

import com.google.android.gms.ads.AdView;

public class AdsTripFragment extends TripFragment {

	private AdView mAdView;

	public static AdsTripFragment newInstance() {
		return new AdsTripFragment();
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