package co.smartreceipts.android.activities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

import co.smartreceipts.android.fragments.LoginFragment;
import co.smartreceipts.android.sync.widget.BackupsFragment;
import co.smartreceipts.android.fragments.ReceiptCreateEditFragment;
import co.smartreceipts.android.fragments.ReceiptImageFragment;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.fragments.TripFragment;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

public interface FragmentProvider {

    /**
     * Creates a {@link co.smartreceipts.android.fragments.TripFragment} instance
     *
     * @return a new trip fragment
     */
    @NonNull
    TripFragment newTripFragmentInstance(boolean navigateToViewLastTrip);

    /**
     * Creates a {@link co.smartreceipts.android.fragments.ReportInfoFragment} instance
     *
     * @param trip the trip to display info for
     * @return a new report info fragment
     */
    @NonNull
    ReportInfoFragment newReportInfoFragment(@NonNull Trip trip);

    /**
     * Creates a {@link co.smartreceipts.android.fragments.ReceiptCreateEditFragment} for a new receipt
     *
     * @param trip the parent trip of this receipt
     * @param file the file associated with this receipt or null if we do not have one
     * @return the new instance of this fragment
     */
    @NonNull
    ReceiptCreateEditFragment newCreateReceiptFragment(@NonNull Trip trip, @Nullable File file);

    /**
     * Creates a {@link co.smartreceipts.android.fragments.ReceiptCreateEditFragment} to edit an existing receipt
     *
     * @param trip the parent trip of this receipt
     * @param receiptToEdit the receipt to edit
     * @return the new instance of this fragment
     */
    @NonNull
    ReceiptCreateEditFragment newEditReceiptFragment(@NonNull Trip trip, @NonNull Receipt receiptToEdit);

    /**
     * Creates a {@link co.smartreceipts.android.fragments.ReceiptImageFragment} instance
     *
     * @param receipt the receipt to show the image for
     * @return a new instance of this fragment
     */
    @NonNull
    ReceiptImageFragment newReceiptImageFragment(@NonNull Receipt receipt);

    /**
     * Creates a {@link BackupsFragment} instance
     *
     * @return a new instance of this fragment
     */
    @NonNull
    BackupsFragment newBackupsFragment();

    /**
     * Creates a {@link LoginFragment} instance
     *
     * @return a new instance of this fragment
     */
    @NonNull
    LoginFragment newLoginFragment();
}
