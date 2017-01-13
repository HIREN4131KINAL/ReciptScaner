package co.smartreceipts.android.sync.widget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import java.io.File;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.TableControllerManager;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.utils.IntentUtils;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class DownloadRemoteBackupImagesProgressDialogFragment extends DialogFragment {

    private static final String ARG_BACKUP_METADATA = "arg_backup_metadata";
    private static final String ARG_DOWNLOAD_DEBUG_MODE = "arg_download_debug_mode";

    private RemoteBackupsDataCache mRemoteBackupsDataCache;
    private Analytics mAnalytics;
    private Subscription mSubscription;

    private RemoteBackupMetadata mBackupMetadata;
    private boolean mDebugMode;

    public static DownloadRemoteBackupImagesProgressDialogFragment newInstance(@NonNull RemoteBackupMetadata remoteBackupMetadata) {
        return newInstance(remoteBackupMetadata, false);
    }

    public static DownloadRemoteBackupImagesProgressDialogFragment newInstance(@NonNull RemoteBackupMetadata remoteBackupMetadata, boolean debugMode) {
        final DownloadRemoteBackupImagesProgressDialogFragment fragment = new DownloadRemoteBackupImagesProgressDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_BACKUP_METADATA, remoteBackupMetadata);
        args.putBoolean(ARG_DOWNLOAD_DEBUG_MODE, debugMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        mBackupMetadata = getArguments().getParcelable(ARG_BACKUP_METADATA);
        mDebugMode = getArguments().getBoolean(ARG_DOWNLOAD_DEBUG_MODE);
        Preconditions.checkNotNull(mBackupMetadata, "This class requires that a RemoteBackupMetadata instance be provided");
        Logger.info(this, "Initializing download of [{}] in debug mode == {}", mBackupMetadata, mDebugMode);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setMessage(getString(R.string.dialog_remote_backup_download_progress));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final SmartReceiptsApplication smartReceiptsApplication = ((SmartReceiptsApplication)getActivity().getApplication());
        mRemoteBackupsDataCache = new RemoteBackupsDataCache(getFragmentManager(), getContext(), smartReceiptsApplication.getBackupProvidersManager(), smartReceiptsApplication.getPersistenceManager().getDatabase());
        mAnalytics = smartReceiptsApplication.getAnalyticsManager();
    }

    @Override
    public void onResume() {
        super.onResume();
        final Observable downloadObservable;

        mSubscription = mRemoteBackupsDataCache.downloadBackup(mBackupMetadata, mDebugMode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(@Nullable File zippedDataFile) {
                        if (zippedDataFile != null) {
                            final Intent intent = IntentUtils.getSendIntent(getContext(), zippedDataFile);
                            getActivity().startActivity(Intent.createChooser(intent, getString(R.string.export)));
                        } else {
                            Toast.makeText(getContext(), getString(R.string.EXPORT_ERROR), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(DownloadRemoteBackupImagesProgressDialogFragment.this, throwable));
                        Toast.makeText(getContext(), getString(R.string.EXPORT_ERROR), Toast.LENGTH_LONG).show();
                        mRemoteBackupsDataCache.removeCachedRestoreBackupFor(mBackupMetadata);
                        dismiss();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mRemoteBackupsDataCache.removeCachedRestoreBackupFor(mBackupMetadata);
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
