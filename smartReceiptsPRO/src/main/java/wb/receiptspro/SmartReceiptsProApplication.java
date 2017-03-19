package wb.receiptspro;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.squareup.leakcanary.LeakCanary;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasDispatchingActivityInjector;
import dagger.android.support.HasDispatchingSupportFragmentInjector;
import wb.android.storage.SDCardFileManager;
import wb.android.storage.SDCardStateException;
import wb.receiptspro.di.AppComponent;
import wb.receiptspro.di.DaggerAppComponent;

public class SmartReceiptsProApplication extends SmartReceiptsApplication
        implements HasDispatchingActivityInjector, HasDispatchingSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityInjector;
    @Inject
    DispatchingAndroidInjector<Fragment> supportFragmentInjector;

    @Inject
    PersistenceManager persistenceManager;

    @Inject
    ReceiptColumnDefinitions receiptColumnDefinitions;

    private WeakReference<ProgressDialog> mProgress;

    @Override
    public void onCreate() {
        super.onCreate();

        DaggerAppComponent.builder()
                .appModule(new AppComponent.AppModule(this))
                .build()
                .inject(this);

        super.init();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            Logger.debug(this, "Ignoring this process as it's the LeakCanary analyzer one...");
            return;
        } else {
            LeakCanary.install(this);
        }

    }

    @Override
    protected void showFirstRunDialog() {
        if (containsSmartReceiptsFree()) { //If we already have the free version, show import dialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity());
            builder.setTitle(R.string.dialog_pro_welcome_title)
                    .setMessage(R.string.dialog_pro_welcome_message)
                    .setPositiveButton(R.string.import_string, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ProgressDialog progress = new ProgressDialog(getCurrentActivity());
                            mProgress = new WeakReference<ProgressDialog>(progress);
                            progress.setMessage(getString(R.string.progress_import));
                            progress.show();
                            new QuickImport().execute();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            // Show the dialog
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    builder.show();
                }
            });
        } else { // Else, it's probably a new user, show first run dialog
            super.showFirstRunDialog();
        }
    }

    private boolean containsSmartReceiptsFree() {
        try {
            getPackageManager().getApplicationInfo("wb.receipts", 0);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return activityInjector;
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return supportFragmentInjector;
    }

    @Override
    protected PersistenceManager getPersistenceManagerInternal() {
        return persistenceManager;
    }

    @Override
    protected ReceiptColumnDefinitions getReceiptColumnDefinitionsInternal() {
        return receiptColumnDefinitions;
    }

    private class QuickImport extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                SDCardFileManager proManager = persistenceManager.getExternalStorageManager();
                File freeRoot = new File(proManager.getRootPath().replace("wb.receiptspro", "wb.receipts"));
                File freeDb = proManager.getFile(freeRoot, DatabaseHelper.DATABASE_NAME);
                if (!freeRoot.exists() || !freeDb.exists()) {
                    return false;
                } else { //If we found the free root
                    File[] freeDirs = proManager.listDirs(freeRoot);
                    for (File dir : freeDirs) {
                        File proDir = proManager.mkdir(dir.getName());
                        Logger.debug(this, "Copying From %s To %s", dir.getAbsolutePath(), proDir.getAbsolutePath());
                        proManager.copy(dir, proDir, true);
                    }
                    Logger.debug(this, "Merging Databases");
                    persistenceManager.getDatabase().merge(freeDb.getAbsolutePath(), getPackageName(), true);
                    return true;
                }
            } catch (SDCardStateException e) {
                Logger.error(this, e);
                return false;
            } catch (IOException e) {
                Logger.error(this, e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (mProgress.get() != null) {
                mProgress.get().dismiss();
                mProgress.clear();
            }
            if (success) {
                Toast.makeText(getCurrentActivity(), R.string.toast_import_complete_simple, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getCurrentActivity(), R.string.IMPORT_ERROR, Toast.LENGTH_LONG).show();
            }
            getTableControllerManager().getTripTableController().get();
        }

    }
}
