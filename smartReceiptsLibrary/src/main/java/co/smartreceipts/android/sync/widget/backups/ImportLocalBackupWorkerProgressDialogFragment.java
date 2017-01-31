package co.smartreceipts.android.sync.widget.backups;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.TableControllerManager;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.sync.manual.ManualBackupAndRestoreTaskCache;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

public class ImportLocalBackupWorkerProgressDialogFragment extends DialogFragment {

    private static final String ARG_SMR_URI = "arg_smr_uri";
    private static final String ARG_OVERWRITE = "arg_overwrite";

    private ManualBackupAndRestoreTaskCache mManualBackupAndRestoreTaskCache;
    private Subscription mSubscription;
    private DatabaseHelper mDatabaseHelper;
    private TableControllerManager mTableControllerManager;
    private Analytics mAnalytics;

    private Uri mUri;
    private boolean mOverwrite;

    public static ImportLocalBackupWorkerProgressDialogFragment newInstance(@NonNull Uri uri, boolean overwrite) {
        final ImportLocalBackupWorkerProgressDialogFragment fragment = new ImportLocalBackupWorkerProgressDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(ARG_SMR_URI, uri);
        args.putBoolean(ARG_OVERWRITE, overwrite);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        mUri = getArguments().getParcelable(ARG_SMR_URI);
        mOverwrite = getArguments().getBoolean(ARG_OVERWRITE);
        mDatabaseHelper = ((SmartReceiptsApplication) getActivity().getApplication()).getPersistenceManager().getDatabase();
        mTableControllerManager = ((SmartReceiptsApplication) getActivity().getApplication()).getTableControllerManager();
        Preconditions.checkNotNull(mUri, "ImportBackupDialogFragment requires a valid SMR Uri");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setMessage(getString(R.string.progress_import));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final SmartReceiptsApplication smartReceiptsApplication = ((SmartReceiptsApplication)getActivity().getApplication());
        mManualBackupAndRestoreTaskCache = new ManualBackupAndRestoreTaskCache(getFragmentManager(), smartReceiptsApplication.getPersistenceManager(), getContext());
        mAnalytics = smartReceiptsApplication.getAnalyticsManager();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscription = mManualBackupAndRestoreTaskCache.getManualRestoreTask().restoreData(mUri, mOverwrite).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(@Nullable Boolean success) {
                        if (success != null && success) {
                            Toast.makeText(getActivity(), R.string.toast_import_complete, Toast.LENGTH_LONG).show();
                            for (final Table table : mDatabaseHelper.getTables()) {
                                table.clearCache();
                            }
                            mTableControllerManager.getTripTableController().get();
                            getActivity().finishAffinity(); // TODO: Fix this hack (for the settings import)
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.IMPORT_ERROR), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mAnalytics.record(new ErrorEvent(ImportLocalBackupWorkerProgressDialogFragment.this, throwable));
                        Toast.makeText(getActivity(), getString(R.string.IMPORT_ERROR), Toast.LENGTH_LONG).show();
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
