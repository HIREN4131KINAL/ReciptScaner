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

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.adapters.DistanceAdapter;
import co.smartreceipts.android.model.DistanceRow;
import co.smartreceipts.android.persistence.DatabaseHelper;

public class DistanceFragment extends WBListFragment implements DatabaseHelper.DistanceRowListener {

    public static final String TAG = DistanceFragment.class.getName();

    private DistanceAdapter mAdapter;

    public static DistanceFragment newInstance() {
        return new DistanceFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getPersistenceManager().getDatabase().registerDistanceRowListener(this);
        mAdapter = new DistanceAdapter(getActivity(), getPersistenceManager().getPreferences());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.simple_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(mAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.distance_action_new) {
            final DistanceDialogFragment dialog = DistanceDialogFragment.newInstance();
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
        final DistanceRow distance = mAdapter.getItem(position);
        final DistanceDialogFragment dialog = DistanceDialogFragment.newInstance(distance);
        dialog.show(getFragmentManager(), DistanceDialogFragment.TAG);
        getFragmentManager().executePendingTransactions();
    }

    @Override
    public void onDistanceRowsQuerySuccess(List<DistanceRow> distance) {
        if (isAdded()) {
            mAdapter.notifyDataSetChanged(distance);
        }
    }

    @Override
    public void onDistanceRowInsertSuccess(DistanceRow distance) {
        if (isAdded()) {
            getPersistenceManager().getDatabase().getDistanceParallel();
        }
    }

    @Override
    public void onDistanceRowInsertFailure(SQLException error) {
        // TODO: Add a toast message
    }

    @Override
    public void onDistanceRowUpdateSuccess(DistanceRow distance) {
        if (isAdded()) {
            getPersistenceManager().getDatabase().getDistanceParallel();
        }
    }

    @Override
    public void onDistanceRowUpdateFailure() {
        // TODO: Add a toast message
    }

    @Override
    public void onDistanceDeleteSuccess(DistanceRow distance) {
        if (isAdded()) {
            getPersistenceManager().getDatabase().getDistanceParallel();
        }
    }

    @Override
    public void onDistanceDeleteFailure() {
        // TODO: Add a toast message
    }
}
