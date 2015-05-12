package co.smartreceipts.android.activities;

import android.support.annotation.NonNull;

import co.smartreceipts.android.fragments.ReceiptsListFragment;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.fragments.TripFragment;
import co.smartreceipts.android.model.Trip;

public interface FragmentProvider {

    /**
     * Builds a {@link co.smartreceipts.android.fragments.TripFragment} instance
     *
     * @return a new trip fragment
     */
    @NonNull
    TripFragment newTripFragmentInstance();

    /**
     * Builds a {@link co.smartreceipts.android.fragments.ReportInfoFragment} instance
     *
     * @return a new report info fragment
     */
    @NonNull
    ReportInfoFragment newReportInfoFragment(@NonNull Trip trip);

    /**
     * Builds a {@link co.smartreceipts.android.fragments.ReceiptsListFragment} instance
     *
     * @return a new receipts list fragment
     */
    @NonNull
    ReceiptsListFragment newReceiptsListFragmentInstance(@NonNull Trip trip);
}
