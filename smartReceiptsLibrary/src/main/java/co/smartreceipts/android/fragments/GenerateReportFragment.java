package co.smartreceipts.android.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import java.util.EnumSet;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.DefaultFragmentProvider;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.EmailAssistant;

public class GenerateReportFragment extends WBFragment implements View.OnClickListener {

    private CheckBox mPdfFullCheckbox;
    private CheckBox mPdfImagesCheckbox;
    private CheckBox mCsvCheckbox;
    private CheckBox mZipStampedImagesCheckbox;

    private Trip mTrip;

    private NavigationHandler mNavigationHandler;

    @NonNull
    public static GenerateReportFragment newInstance(@NonNull Trip currentTrip) {
        final GenerateReportFragment fragment = new GenerateReportFragment();
        Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, currentTrip);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTrip = getArguments().getParcelable(Trip.PARCEL_KEY);
        mNavigationHandler = new NavigationHandler(getActivity(), getFragmentManager(), new DefaultFragmentProvider());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.generate_report_layout, container, false);
        mPdfFullCheckbox = (CheckBox) getFlex().getSubView(getActivity(), root, R.id.DIALOG_EMAIL_CHECKBOX_PDF_FULL);
        mPdfImagesCheckbox = (CheckBox) getFlex().getSubView(getActivity(), root, R.id.DIALOG_EMAIL_CHECKBOX_PDF_IMAGES);
        mCsvCheckbox = (CheckBox) getFlex().getSubView(getActivity(), root, R.id.DIALOG_EMAIL_CHECKBOX_CSV);
        mZipStampedImagesCheckbox = (CheckBox) getFlex().getSubView(getActivity(), root, R.id.DIALOG_EMAIL_CHECKBOX_ZIP_IMAGES_STAMPED);
        root.findViewById(R.id.receipt_action_send).setOnClickListener(this);
        root.findViewById(R.id.generate_report_tooltip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSmartReceiptsApplication().getAnalyticsManager().record(Events.Informational.ConfigureReport);
                mNavigationHandler.navigateToSettingsScrollToReportSection();
            }
        });
        return root;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(null);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Logger.debug(this, "pre-onSaveInstanceState");
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
    }

    @Override
    public void onClick(View v) {
        if (!mPdfFullCheckbox.isChecked() && !mPdfImagesCheckbox.isChecked() && !mCsvCheckbox.isChecked() && !mZipStampedImagesCheckbox.isChecked()) {
            Toast.makeText(getActivity(), getFlex().getString(getActivity(), R.string.DIALOG_EMAIL_TOAST_NO_SELECTION), Toast.LENGTH_SHORT).show();
            return;
        }

        getSmartReceiptsApplication().getAnalyticsManager().record(Events.Generate.GenerateReports);
        if (mPdfFullCheckbox.isChecked()) {
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Generate.FullPdfReport);
        }
        if (mPdfImagesCheckbox.isChecked()) {
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Generate.ImagesPdfReport);
        }
        if (mCsvCheckbox.isChecked()) {
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Generate.CsvReport);
        }
        if (mZipStampedImagesCheckbox.isChecked()) {
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Generate.StampedZipReport);
        }

        // TODO: Off the UI thread :/
        if (getPersistenceManager().getDatabase().getReceiptsTable().getBlocking(mTrip, true).isEmpty()) {
            if (getPersistenceManager().getDatabase().getDistanceTable().getBlocking(mTrip, true).isEmpty() || !mPdfFullCheckbox.isChecked()) {
                // Only allow report processing to continue with no reciepts if we're doing a full pdf report with distances
                Toast.makeText(getActivity(), getFlex().getString(getActivity(), R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS), Toast.LENGTH_SHORT).show();
                return;
            } else {
                // Uncheck "Illegal" Items
                mPdfImagesCheckbox.setChecked(false);
                mCsvCheckbox.setChecked(false);
                mZipStampedImagesCheckbox.setChecked(false);
            }
        }
        EnumSet<EmailAssistant.EmailOptions> options = EnumSet.noneOf(EmailAssistant.EmailOptions.class);
        if (mPdfFullCheckbox.isChecked()) {
            options.add(EmailAssistant.EmailOptions.PDF_FULL);
        }
        if (mPdfImagesCheckbox.isChecked()) {
            options.add(EmailAssistant.EmailOptions.PDF_IMAGES_ONLY);
        }
        if (mCsvCheckbox.isChecked()) {
            options.add(EmailAssistant.EmailOptions.CSV);
        }
        if (mZipStampedImagesCheckbox.isChecked()) {
            options.add(EmailAssistant.EmailOptions.ZIP_IMAGES_STAMPED);
        }

        final EmailAssistant emailAssistant = new EmailAssistant(getActivity(), getFlex(), getPersistenceManager(), mTrip);
        emailAssistant.emailTrip(options);
    }
}
