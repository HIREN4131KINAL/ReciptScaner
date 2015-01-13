package co.smartreceipts.android.fragments;

import android.database.SQLException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.adapters.DistanceAdapter;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.utils.ModelUtils;
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
        setHasOptionsMenu(true);
        getPersistenceManager().getDatabase().registerDistanceRowListener(this);
        mAdapter = new DistanceAdapter(getActivity(), getPersistenceManager().getPreferences());
        mTrip = getArguments().getParcelable(Trip.PARCEL_KEY);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.simple_card_list, container, false);
        mProgressDialog = view.findViewById(R.id.progress);
        mNoDataAlert = (TextView) view.findViewById(R.id.no_data);
        mNoDataAlert.setText(R.string.distance_no_data);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(mAdapter);
    }


    @Override
    public void onResume() {
        super.onResume();
        getPersistenceManager().getDatabase().getDistanceParallel(mTrip);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.distance_action_new) {
            final DistanceDialogFragment dialog = (mLastInsertedDistance == null) ? DistanceDialogFragment.newInstance(mTrip) : DistanceDialogFragment.newInstance(mTrip, mLastInsertedDistance.getDate());
            dialog.show(getFragmentManager(), DistanceDialogFragment.TAG);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_distance, menu);
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
            final Price total = new PriceBuilderFactory().setPriceables(distances).build();
            getSupportActionBar().setSubtitle(getString(R.string.total_item, total.getCurrencyFormattedPrice()));
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
