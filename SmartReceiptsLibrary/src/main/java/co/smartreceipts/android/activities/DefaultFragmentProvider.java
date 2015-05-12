package co.smartreceipts.android.activities;

import android.support.annotation.NonNull;

import co.smartreceipts.android.fragments.ReceiptsFragment;
import co.smartreceipts.android.fragments.ReceiptsListFragment;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.fragments.TripFragment;
import co.smartreceipts.android.model.Trip;

public class DefaultFragmentProvider implements FragmentProvider {

    @NonNull
    @Override
    public TripFragment newTripFragmentInstance() {
        return TripFragment.newInstance();
    }

    @NonNull
    @Override
    public ReportInfoFragment newReportInfoFragment(@NonNull Trip trip) {
        return ReportInfoFragment.newInstance(trip);
    }

    @NonNull
    @Override
    public ReceiptsListFragment newReceiptsListFragmentInstance(@NonNull Trip trip) {
        return ReceiptsFragment.newListInstance(trip);
    }
}
