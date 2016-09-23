package co.smartreceipts.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import co.smartreceipts.android.R;

public class ImportBackupDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = ImportBackupDialogFragment.class.getName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View dialogView = getLayoutInflater(savedInstanceState).inflate(R.layout.dialog_import_backup, null, false);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.import_string);
        builder.setView(dialogView);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_import_positive, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // TODO: Kick off import
            // TODO: Use 'overwrite' pointer
        }
        dismiss();
    }
}
