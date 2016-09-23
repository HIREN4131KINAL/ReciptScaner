package co.smartreceipts.android.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.workers.ExportTask;
import co.smartreceipts.android.workers.ImportTask;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.flex.Flex;

/**
 * This will become a framgent shortly... But for the time being, this utility class will suffice
 *
 * @author WRB
 */
@Deprecated
public class Settings implements ExportTask.Listener {

    private static final String TAG = "Settings";

    private Flex mFlex;
    private SmartReceiptsApplication mApp;
    private PersistenceManager mPersistenceManager;

    public Settings(SmartReceiptsApplication app) {
        mApp = app;
        mPersistenceManager = app.getPersistenceManager();
        mFlex = app.getFlex();
    }

    public void showExport(final Fragment fragment) {
        final BetterDialogBuilder builder = new BetterDialogBuilder(mApp.getCurrentActivity());
        builder.setTitle(R.string.dialog_export_title).setMessage(R.string.dialog_export_text).setCancelable(true).setPositiveButton(R.string.dialog_export_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                (new ExportTask(mApp.getCurrentActivity(), "Exporting your receipts...", mPersistenceManager, Settings.this)).execute();
            }
        }).setNeutralButton(R.string.dialog_import_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    if (fragment.getActivity() != null) {
                        fragment.startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), ImportTask.TASK_ID);
                    }
                } catch (android.content.ActivityNotFoundException ex) {
                    if (fragment.isAdded()) {
                        Toast.makeText(fragment.getActivity(), fragment.getActivity().getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        }).show();
    }

    @Override
    public void onExportComplete(Uri uri) {
        if (uri == null) {
            Toast.makeText(mApp.getCurrentActivity(), mFlex.getString(mApp.getCurrentActivity(), R.string.EXPORT_ERROR), Toast.LENGTH_LONG).show();
            return;
        }
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("application/octet-stream");
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        if (mApp != null) {
            if (mApp.getCurrentActivity() != null) {
                mApp.getCurrentActivity().startActivity(Intent.createChooser(emailIntent, "Export To..."));
            } else {
                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mApp.getApplicationContext().startActivity(Intent.createChooser(emailIntent, "Export To..."));
            }
        }
    }

}
