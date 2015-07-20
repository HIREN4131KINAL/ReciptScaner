package co.smartreceipts.android.fragments;

import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.io.File;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.DefaultFragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.adapters.TripFragmentPagerAdapter;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.LastTripController;

public class ReportInfoFragment extends WBFragment implements DatabaseHelper.TripRowListener {

    public static final String TAG = ReportInfoFragment.class.getSimpleName();

    private NavigationHandler mNavigationHandler;
    private LastTripController mLastTripController;
    private FragmentPagerAdapter mFragmentPagerAdapter;
    private Trip mTrip;

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mPagerSlidingTabStrip;

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
        setHasOptionsMenu(true);
        mNavigationHandler = new NavigationHandler(getActivity(), getFragmentManager(), new DefaultFragmentProvider());
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
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationHandler.navigateToTripsFragment();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onPause() {
        mLastTripController.setLastTrip(mTrip);
        super.onPause();
    }

    @Override
    public void onTripRowsQuerySuccess(Trip[] trips) {

    }

    @Override
    public void onTripRowInsertSuccess(Trip trip) {

    }

    @Override
    public void onTripRowInsertFailure(SQLException ex, File directory) {

    }

    @Override
    public void onTripRowUpdateSuccess(Trip trip) {

    }

    @Override
    public void onTripRowUpdateFailure(Trip newTrip, Trip oldTrip, File directory) {

    }

    @Override
    public void onTripDeleteSuccess(Trip oldTrip) {

    }

    @Override
    public void onTripDeleteFailure() {

    }

    @Override
    public void onSQLCorruptionException() {

    }
}