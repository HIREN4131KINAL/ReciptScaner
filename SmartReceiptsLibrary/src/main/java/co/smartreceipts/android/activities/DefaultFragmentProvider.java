package co.smartreceipts.android.activities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

import co.smartreceipts.android.fragments.ReceiptCreateEditFragment;
import co.smartreceipts.android.fragments.ReceiptImageFragment;
import co.smartreceipts.android.fragments.ReceiptPDFFragment;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.fragments.TripFragment;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;

public class DefaultFragmentProvider implements FragmentProvider {

    @NonNull
    @Override
    public TripFragment newTripFragmentInstance(boolean navigateToViewLastTrip) {
        return TripFragment.newInstance(navigateToViewLastTrip);
    }

    @NonNull
    @Override
    public ReportInfoFragment newReportInfoFragment(@NonNull Trip trip) {
        return ReportInfoFragment.newInstance(trip);
    }

    @NonNull
    @Override
    public ReceiptCreateEditFragment newCreateReceiptFragment(@NonNull Trip trip, @Nullable File file) {
        return ReceiptCreateEditFragment.newInstance(trip, file);
    }

    @NonNull
    @Override
    public ReceiptCreateEditFragment newEditReceiptFragment(@NonNull Trip trip, @NonNull Receipt receiptToEdit) {
        return ReceiptCreateEditFragment.newInstance(trip, receiptToEdit);
    }

    @NonNull
    @Override
    public ReceiptImageFragment newReceiptImageFragment(@NonNull Receipt receipt) {
        return ReceiptImageFragment.newInstance(receipt);
    }

    @NonNull
    @Override
    public ReceiptPDFFragment newReceiptPdfFragment(@NonNull Receipt receipt) {
        return ReceiptPDFFragment.newInstance(receipt);
    }


}