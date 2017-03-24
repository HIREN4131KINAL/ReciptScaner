package co.smartreceipts.android.rating;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.workers.EmailAssistant;

/**
 * Dialog Fragment which asks if user wants to leave feedback
 */
public class FeedbackDialogFragment extends DialogFragment {

    private Analytics analytics;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        analytics.record(Events.Ratings.UserAcceptedSendingFeedback);
                        openEmailAssistant();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        analytics.record(Events.Ratings.UserDeclinedSendingFeedback);
                        dismiss();
                    }
                })
                .setMessage(R.string.leave_feedback_text);
        return builder.create();
    }

    private void openEmailAssistant() {
        final Intent intent = EmailAssistant.getEmailDeveloperIntent(getString(R.string.feedback, getString(R.string.sr_app_name)));
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initAnalytics();
    }

    private void initAnalytics() {
        final SmartReceiptsApplication smartReceiptsApplication = ((SmartReceiptsApplication)getActivity().getApplication());
        analytics = smartReceiptsApplication.getAnalyticsManager();
    }

}
