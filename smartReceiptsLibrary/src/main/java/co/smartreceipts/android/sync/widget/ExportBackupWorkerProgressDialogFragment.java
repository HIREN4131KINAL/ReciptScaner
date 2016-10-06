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

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.sync.manual.ManualBackupAndRestoreTaskCache;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

public class ExportBackupWorkerProgressDialogFragment extends DialogFragment {

    private ManualBackupAndRestoreTaskCache mManualBackupAndRestoreTaskCache;
    private Subscription mSubscription;

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
        mManualBackupAndRestoreTaskCache = new ManualBackupAndRestoreTaskCache(getFragmentManager(), ((SmartReceiptsApplication)getActivity().getApplication()).getPersistenceManager(), getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscription = mManualBackupAndRestoreTaskCache.getManualBackupTask().backupData().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Uri>() {
                    @Override
                    public void call(@Nullable Uri uri) {
                        if (uri != null) {
                            final Intent sentIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sentIntent.setType("application/octet-stream");
                            sentIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            getActivity().startActivity(Intent.createChooser(sentIntent, getString(R.string.export)));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
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
        mSubscription.unsubscribe();
        super.onPause();
    }
}
