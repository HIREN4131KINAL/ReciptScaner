package co.smartreceipts.android.fragments;

import android.database.SQLException;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.adapters.DistanceAdapter;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;

public class DistanceFragment extends WBListFragment implements DatabaseHelper.DistanceRowListener {

    public static final String TAG = DistanceFragment.class.getName();

    private Trip mTrip;
    private DistanceAdapter mAdapter;
    private View mProgressDialog;
    private TextView mNoDataAlert;
    private Distance mLastInsertedDistance;

    public static DistanceFragment newInstance(final Trip trip) {
        final DistanceFragment fragment = new DistanceFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, trip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        mAdapter = new DistanceAdapter(getActivity(), getPersistenceManager().getPreferences());
        mTrip = getArguments().getParcelable(Trip.PARCEL_KEY);
        if (savedInstanceState ==  null) {
            getWorkerManager().getLogger().logEvent(this, "Edit_Mileage");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final View view = inflater.inflate(R.layout.report_distance_list, container, false);
        mProgressDialog = view.findViewById(R.id.progress);
        mNoDataAlert = (TextView) view.findViewById(R.id.no_data);
        mNoDataAlert.setText(R.string.distance_no_data);
        view.findViewById(R.id.distance_action_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWorkerManager().getLogger().logEvent(DistanceFragment.this, "Add_Mileage");
                final DistanceDialogFragment dialog = (mLastInsertedDistance == null) ? DistanceDialogFragment.newInstance(mTrip) : DistanceDialogFragment.newInstance(mTrip, mLastInsertedDistance.getDate());
                dialog.show(getFragmentManager(), DistanceDialogFragment.TAG);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        setListAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        getPersistenceManager().getDatabase().registerDistanceRowListener(this);
        getPersistenceManager().getDatabase().getDistanceParallel(mTrip);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Refresh as soon as we're visible
            getPersistenceManager().getDatabase().getDistanceParallel(mTrip);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        getPersistenceManager().getDatabase().unregisterDistanceRowListener();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Distance distance = mAdapter.getItem(position);
        final DistanceDialogFragment dialog = DistanceDialogFragment.newInstance(mTrip, distance);
        dialog.show(getFragmentManager(), DistanceDialogFragment.TAG);
        getFragmentManager().executePendingTransactions();
    }

    @Override
    public void onDistanceRowsQuerySuccess(List<Distance> distances) {
        if (isAdded()) {
            mAdapter.notifyDataSetChanged(distances);
            mProgressDialog.setVisibility(View.GONE);
            if (distances == null || distances.size() == 0) {
                getListView().setVisibility(View.GONE);
                mNoDataAlert.setVisibility(View.VISIBLE);
                return;
            } else {
                mNoDataAlert.setVisibility(View.GONE);
                getListView().setVisibility(View.VISIBLE);
            }
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null && getUserVisibleHint()) {
                final Price total = new PriceBuilderFactory().setPriceables(distances, mTrip.getTripCurrency()).build();
                getSupportActionBar().setSubtitle(getString(R.string.total_item, total.getCurrencyFormattedPrice()));
            }
            // Fetch trips in the background to ensure this info is up to date
            getPersistenceManager().getDatabase().getTripsParallel();
        }
    }

    @Override
    public void onDistanceRowInsertSuccess(Distance distance) {
        if (isAdded()) {
            getPersistenceManager().getDatabase().getDistanceParallel(mTrip);
        }
        mLastInsertedDistance = distance;
    }

    @Override
    public void onDistanceRowInsertFailure(SQLException error) {
        showToastMessage(R.string.distance_insert_failed);
    }

    @Override
    public void onDistanceRowUpdateSuccess(Distance distance) {
        if (isAdded()) {
            getPersistenceManager().getDatabase().getDistanceParallel(mTrip);
        }
    }

    @Override
    public void onDistanceRowUpdateFailure() {
        showToastMessage(R.string.distance_update_failed);
    }

    @Override
    public void onDistanceDeleteSuccess(Distance distance) {
        if (isAdded()) {
            getPersistenceManager().getDatabase().getDistanceParallel(mTrip);
        }
    }

    @Override
    public void onDistanceDeleteFailure() {
        showToastMessage(R.string.distance_delete_failed);
    }

    private void showToastMessage(int stringResId) {
        if (isAdded()) {
            Toast.makeText(getActivity(), stringResId, Toast.LENGTH_LONG).show();
        }
    }
}
