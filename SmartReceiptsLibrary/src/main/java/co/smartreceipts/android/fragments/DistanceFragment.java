package co.smartreceipts.android.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
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
import co.smartreceipts.android.persistence.database.controllers.TripForeignKeyTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;

public class DistanceFragment extends WBListFragment implements TripForeignKeyTableEventsListener<Distance> {

    public static final String TAG = DistanceFragment.class.getName();

    private Trip mTrip;
    private DistanceAdapter mAdapter;
    private View mProgressDialog;
    private TextView mNoDataAlert;
    private Distance mLastInsertedDistance;
    private DistanceTableController mDistanceTableController;

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
        mAdapter = new DistanceAdapter(getActivity(), getPersistenceManager().getPreferences(), getSmartReceiptsApplication().getBackupProvidersManager());
        mTrip = getArguments().getParcelable(Trip.PARCEL_KEY);
        mDistanceTableController = getSmartReceiptsApplication().getTableControllerManager().getDistanceTableController();
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
        mDistanceTableController.subscribe(this);
        mDistanceTableController.get(mTrip);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null && isVisibleToUser) {
            // Refresh as soon as we're visible
            mDistanceTableController.get(mTrip);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mDistanceTableController.unsubscribe(this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Distance distance = mAdapter.getItem(position);
        final DistanceDialogFragment dialog = DistanceDialogFragment.newInstance(mTrip, distance);
        dialog.show(getFragmentManager(), DistanceDialogFragment.TAG);
        getFragmentManager().executePendingTransactions();
    }

    @Override
    public void onGetSuccess(@NonNull List<Distance> distances, @NonNull Trip trip) {
        if (isAdded()) {
            mAdapter.notifyDataSetChanged(distances);
            mProgressDialog.setVisibility(View.GONE);
            if (distances.isEmpty()) {
                getListView().setVisibility(View.GONE);
                mNoDataAlert.setVisibility(View.VISIBLE);
            } else {
                mNoDataAlert.setVisibility(View.GONE);
                getListView().setVisibility(View.VISIBLE);
            }

            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null && getUserVisibleHint()) {
                if (getPersistenceManager().getPreferences().getShowDistanceAsPriceInSubtotal()) {
                    final Price total = new PriceBuilderFactory().setPriceables(distances, mTrip.getTripCurrency()).build();
                    getSupportActionBar().setSubtitle(getString(R.string.distance_total_item, total.getCurrencyFormattedPrice()));
                } else {
                    BigDecimal distanceTotal = new BigDecimal(0);
                    for (final Distance distance : distances) {
                        distanceTotal = distanceTotal.add(distance.getDistance());
                    }
                    getSupportActionBar().setSubtitle(getString(R.string.distance_total_item, ModelUtils.getDecimalFormattedValue(distanceTotal)));
                }
            }
        }
    }

    @Override
    public void onGetFailure(@Nullable Throwable e, @NonNull Trip trip) {
        // TODO: Respond?
    }

    @Override
    public void onGetSuccess(@NonNull List<Distance> list) {
        // TODO: Respond?
    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {
        // TODO: Respond?
    }

    @Override
    public void onInsertSuccess(@NonNull Distance distance, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            mDistanceTableController.get(mTrip);
        }
        mLastInsertedDistance = distance;
    }

    @Override
    public void onInsertFailure(@NonNull Distance distance, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        showToastMessage(R.string.distance_insert_failed);
    }

    @Override
    public void onUpdateSuccess(@NonNull Distance oldDistance, @NonNull Distance newDistance, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            mDistanceTableController.get(mTrip);
        }
    }

    @Override
    public void onUpdateFailure(@NonNull Distance oldDistance, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        showToastMessage(R.string.distance_update_failed);
    }

    @Override
    public void onDeleteSuccess(@NonNull Distance distance, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            mDistanceTableController.get(mTrip);
        }
    }

    @Override
    public void onDeleteFailure(@NonNull Distance distance, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        showToastMessage(R.string.distance_delete_failed);
    }

    private void showToastMessage(int stringResId) {
        if (isAdded()) {
            Toast.makeText(getActivity(), stringResId, Toast.LENGTH_LONG).show();
        }
    }
}
