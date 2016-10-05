package co.smartreceipts.android.sync.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CheckBox;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;

public class ImportLocalBackupDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = ImportLocalBackupDialogFragment.class.getName();

    private static final String ARG_SMR_URI = "arg_smr_uri";

    private Uri mUri;
    private CheckBox mOverwriteCheckBox;

    public static ImportLocalBackupDialogFragment newInstance(@NonNull Uri uri) {
        final ImportLocalBackupDialogFragment fragment = new ImportLocalBackupDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_SMR_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri = getArguments().getParcelable(ARG_SMR_URI);
        Preconditions.checkNotNull(mUri, "ImportBackupDialogFragment requires a valid SMR Uri");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_import_backup, null);
        mOverwriteCheckBox = (CheckBox) dialogView.findViewById(R.id.dialog_import_overwrite);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setTitle(R.string.import_string);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_import_positive, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            final ImportLocalBackupWorkerProgressDialogFragment progressDialogFragment = ImportLocalBackupWorkerProgressDialogFragment.newInstance(mUri, mOverwriteCheckBox.isChecked());
            progressDialogFragment.show(getFragmentManager(), ImportLocalBackupWorkerProgressDialogFragment.TAG);
        }
        dismiss();
    }
}
