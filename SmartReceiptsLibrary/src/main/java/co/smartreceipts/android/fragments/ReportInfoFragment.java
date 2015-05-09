package co.smartreceipts.android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import co.smartreceipts.android.R;
import co.smartreceipts.android.adapters.TripFragmentPagerAdapter;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.LastTripController;

public class ReportInfoFragment extends WBFragment {

    public static final String TAG = ReportInfoFragment.class.getSimpleName();

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;

    private LastTripController mLastTripController;
    private FragmentPagerAdapter mFragmentPagerAdapter;
    private Trip mTrip;

    @NonNull
    public static ReportInfoFragment newInstance(@NonNull Trip currentTrip) {
        final ReportInfoFragment fragment = new ReportInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, currentTrip);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTrip = getArguments().getParcelable(Trip.PARCEL_KEY);
        mLastTripController = new LastTripController(getActivity(), getPersistenceManager().getDatabase());
        if (mTrip == null) {
            mTrip = mLastTripController.getLastTrip();
            // TODO: What happens if this is still null?
        }
        mFragmentPagerAdapter = new TripFragmentPagerAdapter(getChildFragmentManager(), mTrip);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.report_info_view_pager, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mPagerSlidingTabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        mViewPager.setAdapter(mFragmentPagerAdapter);
        mPagerSlidingTabStrip.setViewPager(mViewPager);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (final Fragment fragment : getChildFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPause() {
        mLastTripController.setLastTrip(mTrip);
        super.onPause();
    }
}