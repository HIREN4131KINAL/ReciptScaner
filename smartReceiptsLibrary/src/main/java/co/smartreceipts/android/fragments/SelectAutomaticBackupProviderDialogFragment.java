package co.smartreceipts.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.provider.SyncProvider;

public class SelectAutomaticBackupProviderDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private BackupProvidersManager mBackupProvidersManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBackupProvidersManager = ((SmartReceiptsApplication) getActivity().getApplication()).getBackupProvidersManager();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_automatic_backup_provider, null);
        final RadioGroup providerGroup = (RadioGroup) dialogView.findViewById(R.id.automatic_backup_provider_radiogroup);
        final RadioButton noneProviderButton = (RadioButton) providerGroup.findViewById(R.id.automatic_backup_provider_none);
        final RadioButton googleDriveProviderButton = (RadioButton) providerGroup.findViewById(R.id.automatic_backup_provider_google_drive);

        if (mBackupProvidersManager.getSyncProvider() == SyncProvider.GoogleDrive) {
            googleDriveProviderButton.setChecked(true);
        } else {
            noneProviderButton.setChecked(true);
        }
        providerGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                final SyncProvider currentProvider = mBackupProvidersManager.getSyncProvider();
                final SyncProvider newProvider;
                if (id == R.id.automatic_backup_provider_google_drive) {
                    newProvider = SyncProvider.GoogleDrive;
                } else {
                    newProvider = SyncProvider.None;
                }
                if (currentProvider != newProvider) {
                    mBackupProvidersManager.setAndInitializeSyncProvider(newProvider, getActivity());
                }
                dismiss();
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        dismiss();
    }
}
