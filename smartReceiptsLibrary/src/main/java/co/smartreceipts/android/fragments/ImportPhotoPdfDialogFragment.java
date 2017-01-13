package co.smartreceipts.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import co.smartreceipts.android.R;
import co.smartreceipts.android.imports.RequestCodes;

public class ImportPhotoPdfDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String TAG = ImportPhotoPdfDialogFragment.class.getSimpleName();

    private final int WHICH_IMAGE = 0;
    private final int WHICH_PDF = 1;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String image = getString(R.string.image);
        final String pdf = getString(R.string.pdf);
        final CharSequence[] choices = new CharSequence[] {image, pdf};
        builder.setItems(choices, this);
        builder.setCancelable(true);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        // @see https://developer.android.com/guide/topics/providers/document-provider.html#client
        // Use ACTION_GET_CONTENT instead of ACTION_OPEN_DOCUMENT as this is simply a read/import
        try {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (which == WHICH_IMAGE) {
                intent.setType("image/*");
                getParentFragment().startActivityForResult(intent, RequestCodes.IMPORT_GALLERY_IMAGE);
            } else if (which == WHICH_PDF) {
                intent.setType("application/pdf");
                getParentFragment().startActivityForResult(intent, RequestCodes.IMPORT_GALLERY_PDF);
            }
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }
}
