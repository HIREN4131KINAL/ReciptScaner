package co.smartreceipts.android.purchases.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.Subscription;

public class SubscriptionInformationDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = SubscriptionInformationDialogFragment.class.getSimpleName();

    private SmartReceiptsActivity mSmartReceiptsActivity;
    private PurchaseManager mPurchaseManager;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof SmartReceiptsActivity) {
            mSmartReceiptsActivity = (SmartReceiptsActivity) activity;
        } else {
            throw new IllegalArgumentException("SubscriptionInformationDialogFragment must be attached to an instance of SmartReceiptsActivity");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.plus_subscription_info_dialog_title);
        builder.setMessage(R.string.plus_subscription_info_dialog_message);
        builder.setPositiveButton(R.string.plus_subscription_info_dialog_positive_button, this);
        builder.setNegativeButton(R.string.plus_subscription_info_dialog_negative_button, this);
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPurchaseManager = mSmartReceiptsActivity.getSubscriptionManager();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE && mPurchaseManager != null) {
            mPurchaseManager.queryBuyIntent(Subscription.SmartReceiptsPlus, PurchaseSource.UpsellDialog);
        }
        dismiss();
    }
}
