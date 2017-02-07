package co.smartreceipts.android.activities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

import co.smartreceipts.android.identity.widget.LoginFragment;
import co.smartreceipts.android.ocr.info.OcrInformationalFragment;
import co.smartreceipts.android.sync.widget.backups.BackupsFragment;
import co.smartreceipts.android.fragments.ReceiptCreateEditFragment;
import co.smartreceipts.android.fragments.ReceiptImageFragment;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.fragments.TripFragment;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

public class FragmentProvider {

    /**
     * Creates a {@link co.smartreceipts.android.fragments.TripFragment} instance
     *
     * @return a new trip fragment
     */
    @NonNull
    public TripFragment newTripFragmentInstance(boolean navigateToViewLastTrip) {
        return TripFragment.newInstance(navigateToViewLastTrip);
    }

    /**
     * Creates a {@link co.smartreceipts.android.fragments.ReportInfoFragment} instance
     *
     * @param trip the trip to display info for
     * @return a new report info fragment
     */
    @NonNull
    public ReportInfoFragment newReportInfoFragment(@NonNull Trip trip) {
        return ReportInfoFragment.newInstance(trip);
    }

    /**
     * Creates a {@link co.smartreceipts.android.fragments.ReceiptCreateEditFragment} for a new receipt
     *
     * @param trip the parent trip of this receipt
     * @param file the file associated with this receipt or null if we do not have one
     * @return the new instance of this fragment
     */
    @NonNull
    public ReceiptCreateEditFragment newCreateReceiptFragment(@NonNull Trip trip, @Nullable File file) {
        return ReceiptCreateEditFragment.newInstance(trip, file);
    }

    /**
     * Creates a {@link co.smartreceipts.android.fragments.ReceiptCreateEditFragment} to edit an existing receipt
     *
     * @param trip the parent trip of this receipt
     * @param receiptToEdit the receipt to edit
     * @return the new instance of this fragment
     */
    @NonNull
    public ReceiptCreateEditFragment newEditReceiptFragment(@NonNull Trip trip, @NonNull Receipt receiptToEdit) {
        return ReceiptCreateEditFragment.newInstance(trip, receiptToEdit);
    }

    /**
     * Creates a {@link co.smartreceipts.android.fragments.ReceiptImageFragment} instance
     *
     * @param receipt the receipt to show the image for
     * @return a new instance of this fragment
     */
    @NonNull
    public ReceiptImageFragment newReceiptImageFragment(@NonNull Receipt receipt) {
        return ReceiptImageFragment.newInstance(receipt);
    }

    /**
     * Creates a {@link BackupsFragment} instance
     *
     * @return a new instance of this fragment
     */
    @NonNull
    public BackupsFragment newBackupsFragment() {
        return new BackupsFragment();
    }

    /**
     * Creates a {@link LoginFragment} instance
     *
     * @return a new instance of this fragment
     */
    @NonNull
    public LoginFragment newLoginFragment() {
        return LoginFragment.newInstance();
    }

    /**
     * Creates a {@link OcrInformationalFragment} instance
     *
     * @return a new instance of this fragment
     */
    @NonNull
    public OcrInformationalFragment newOcrInformationalFragment() {
        return OcrInformationalFragment.newInstance();
    }

}