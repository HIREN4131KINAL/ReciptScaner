package co.smartreceipts.android.sync.widget.backups;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.utils.log.Logger;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

public class DeleteRemoteBackupProgressDialogFragment extends DialogFragment {

    private static final String ARG_BACKUP_METADATA = "arg_backup_metadata";

    private RemoteBackupsDataCache mRemoteBackupsDataCache;
    private BackupProvidersManager mBackupProvidersManager;
    private Analytics mAnalytics;
    private Subscription mSubscription;

    private RemoteBackupMetadata mBackupMetadata;

    public static DeleteRemoteBackupProgressDialogFragment newInstance() {
        return newInstance(null);
    }

    public static DeleteRemoteBackupProgressDialogFragment newInstance(@Nullable RemoteBackupMetadata remoteBackupMetadata) {
        final DeleteRemoteBackupProgressDialogFragment fragment = new DeleteRemoteBackupProgressDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_BACKUP_METADATA, remoteBackupMetadata);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        mBackupMetadata = getArguments().getParcelable(ARG_BACKUP_METADATA);
        if (mBackupMetadata == null) {
            Logger.info(this, "Deleting the local device backup");
        } else {
            Logger.info(this, "Deleting the backup of another device");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        if (mBackupMetadata != null) {
            dialog.setMessage(getString(R.string.dialog_remote_backup_delete_progress, mBackupMetadata.getSyncDeviceName()));
        } else {
            dialog.setMessage(getString(R.string.dialog_remote_backup_restore_progress));
        }
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final SmartReceiptsApplication smartReceiptsApplication = ((SmartReceiptsApplication)getActivity().getApplication());
        mBackupProvidersManager = smartReceiptsApplication.getBackupProvidersManager();
        mRemoteBackupsDataCache = new RemoteBackupsDataCache(getFragmentManager(), getContext(), mBackupProvidersManager, smartReceiptsApplication.getNetworkManager(), smartReceiptsApplication.getPersistenceManager().getDatabase());
        mAnalytics = smartReceiptsApplication.getAnalyticsManager();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscription = mRemoteBackupsDataCache.deleteBackup(mBackupMetadata).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean deleteSuccess) {
                        if (deleteSuccess) {
                            Logger.info(DeleteRemoteBackupProgressDialogFragment.this, "Successfully handled delete of {}", mBackupMetadata);
                            if (mBackupMetadata != null) {
                                Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_delete_toast_success), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_restore_toast_success), Toast.LENGTH_LONG).show();
                                mBackupProvidersManager.markErrorResolved(SyncErrorType.UserRevokedRemoteRights);
                            }

                            // Note: this is kind of hacky but should work
                            mRemoteBackupsDataCache.clearGetBackupsResults();;
                            final Fragment uncastedBackupsFragment = getFragmentManager().findFragmentByTag(BackupsFragment.class.getName());
                            if (uncastedBackupsFragment instanceof BackupsFragment) {
                                // If we're active, kick off a refresh directly in the fragment
                                final BackupsFragment backupsFragment = (BackupsFragment) uncastedBackupsFragment;
                                backupsFragment.updateViewsForProvider(SyncProvider.GoogleDrive);
                            } else {
                                // Kick off a refresh, so we catch it next time
                                mRemoteBackupsDataCache.getBackups(SyncProvider.GoogleDrive);
                            }
                        } else {
                            Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_delete_toast_failure), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(DeleteRemoteBackupProgressDialogFragment.this, throwable));
                        if (mBackupMetadata != null) {
                            Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_delete_toast_failure), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.dialog_remote_backup_restore_toast_failure), Toast.LENGTH_LONG).show();
                        }

                        dismiss();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        dismiss();
                    }
                });
    }

    @Override
    public void onPause() {
        mSubscription.unsubscribe();
        super.onPause();
    }
}
