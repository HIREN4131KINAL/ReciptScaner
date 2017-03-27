package co.smartreceipts.android.sync.widget.backups;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.sync.manual.ManualBackupAndRestoreTaskCache;
import dagger.android.support.AndroidSupportInjection;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

public class ExportBackupWorkerProgressDialogFragment extends DialogFragment {

    @Inject
    PersistenceManager persistenceManager;
    @Inject
    AnalyticsManager analyticsManager;

    private ManualBackupAndRestoreTaskCache manualBackupAndRestoreTaskCache;
    private Subscription subscription;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setMessage(getString(R.string.dialog_export_working));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        manualBackupAndRestoreTaskCache = new ManualBackupAndRestoreTaskCache(getFragmentManager(), persistenceManager, getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        subscription = manualBackupAndRestoreTaskCache.getManualBackupTask().backupData().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Uri>() {
                    @Override
                    public void call(@Nullable Uri uri) {
                        if (uri != null) {
                            final Intent sentIntent = new Intent(Intent.ACTION_SEND);
                            sentIntent.setType("application/octet-stream");
                            sentIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            getActivity().startActivity(Intent.createChooser(sentIntent, getString(R.string.export)));
                        } else {
                            Toast.makeText(getContext(), getString(R.string.EXPORT_ERROR), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        analyticsManager.record(new ErrorEvent(ExportBackupWorkerProgressDialogFragment.this, throwable));
                        Toast.makeText(getContext(), getString(R.string.EXPORT_ERROR), Toast.LENGTH_LONG).show();
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
        subscription.unsubscribe();
        super.onPause();
    }
}
