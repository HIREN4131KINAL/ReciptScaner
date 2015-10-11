package wb.receiptspro;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import co.smartreceipts.android.purchases.ProSubscriptionCache;
import co.smartreceipts.android.purchases.SubscriptionCache;
import wb.android.storage.SDCardFileManager;
import wb.android.storage.SDCardStateException;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.persistence.DatabaseHelper;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SmartReceiptsProApplication extends SmartReceiptsApplication {

	private WeakReference<ProgressDialog> mProgress;

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
		}
		else { // Else, it's probably a new user, show first run dialog
			super.showFirstRunDialog();
		}
	}

	@Override
	public Class<? extends SmartReceiptsActivity> getTopLevelActivity() {
		return SmartReceiptsProActivity.class;
	}

	private boolean containsSmartReceiptsFree() {
		try {
			getPackageManager().getApplicationInfo("wb.receipts", 0);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	private class QuickImport extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			try {
				SDCardFileManager proManager = getPersistenceManager().getExternalStorageManager();
				File freeRoot = new File(proManager.getRootPath().replace("wb.receiptspro", "wb.receipts"));
				File freeDb = proManager.getFile(freeRoot, DatabaseHelper.DATABASE_NAME);
				if (!freeRoot.exists() || !freeDb.exists()) {
					return false;
				}
				else { //If we found the free root
					File[] freeDirs = proManager.listDirs(freeRoot);
					for (File dir : freeDirs) {
						File proDir = proManager.mkdir(dir.getName());
						if (BuildConfig.DEBUG) {
							Log.d(TAG, "Copying From " + dir.getAbsolutePath() + " To " + proDir.getAbsolutePath());
						}
						proManager.copy(dir, proDir, true);
					}
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "Mergeing Databases");
					}
					getPersistenceManager().getDatabase().merge(freeDb.getAbsolutePath(), getPackageName(), true);
					return true;
				}
			} catch (SDCardStateException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, e.toString(), e);
				}
				return false;
			} catch (IOException e) {
				if (BuildConfig.DEBUG) {
					Log.e(TAG, e.toString(), e);
				}
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
    		}
    		else {
    			Toast.makeText(getCurrentActivity(), R.string.IMPORT_ERROR, Toast.LENGTH_LONG).show();
    		}
    		getPersistenceManager().getDatabase().getTripsParallel();
		}

	}

    @Override
    protected SubscriptionCache initiateSubscriptionCache() {
        return new ProSubscriptionCache();
    }

    /*
	@Override
	public int getFleXML() {
		return wb.receiptspro.R.raw.flex;
	}
	*/
}
