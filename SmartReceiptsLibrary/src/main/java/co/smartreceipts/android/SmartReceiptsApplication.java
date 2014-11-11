package co.smartreceipts.android;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import wb.android.flex.Flex;
import wb.android.flex.Flexable;
import wb.android.google.camera.app.GalleryAppImpl;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;
import wb.android.util.AppRating;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.fragments.Settings;
import co.smartreceipts.android.model.CSVColumns;
import co.smartreceipts.android.model.Columns;
import co.smartreceipts.android.model.PDFColumns;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions;
import co.smartreceipts.android.testutils.WBUncaughtExceptionHandler;
import co.smartreceipts.android.workers.WorkerManager;

/**
 * This extends GalleryAppImpl for the camera, since we can only define a single application in the manifest
 * 
 * @author WRB
 * 
 */
public class SmartReceiptsApplication extends GalleryAppImpl implements Flexable, Preferences.VersionUpgradeListener, DatabaseHelper.TableDefaultsCustomizer {

	public static final String TAG = "SmartReceiptsApplication";

	private WorkerManager mWorkerManager;
	private PersistenceManager mPersistenceManager;
	private Flex mFlex;
	private Activity mCurrentActivity;
	private Settings mSettings;
	private boolean mDeferFirstRunDialog;

	/**
	 * The {@link Application} class is a singleton, so we can cache it here for emergency restoration
	 */
	private static SmartReceiptsApplication sApplication;

	@Override
	public void onCreate() {
		super.onCreate();
		preloadSharedPreferences();
		WBUncaughtExceptionHandler.initialize();
		sApplication = this;
		mDeferFirstRunDialog = false;
		mFlex = instantiateFlex();
		mWorkerManager = instantiateWorkerManager();
		mPersistenceManager = instantiatePersistenceManager();
		mPersistenceManager.getPreferences().setVersionUpgradeListener(this); // Done so mPersistenceManager is not null
																				// in onVersionUpgrade
	}

	public static final SmartReceiptsApplication getInstance() {
		return sApplication;
	}

	@Override
	public void onTerminate() {
		// TODO: Alter this as this method will NEVER be called outside of an emulated environment
		mCurrentActivity = null;
		mPersistenceManager.onDestroy();
		mWorkerManager.onDestroy();
		mPersistenceManager = null;
		mWorkerManager = null;
		super.onTerminate();
	}

	/**
	 * All SharedPreferences are singletons, so let's go ahead and load all of them as soon as our app starts
	 */
	private void preloadSharedPreferences() {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Preloading Shared Preferences");
		}
		final WeakReference<Context> appContext = new WeakReference<Context>(this);
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						SharedPreferenceDefinitions[] definitions = SharedPreferenceDefinitions.values();
						for (SharedPreferenceDefinitions definition : definitions) {
							Context context = appContext.get();
							if (context != null) {
								context.getSharedPreferences(definition.toString(), 0);
							}
						}
						// Load AppRating Prefs (these are hidden normally)
						Context context = appContext.get();
						if (context != null) {
							context.getSharedPreferences(AppRating.getApplicationName(context) + "rating", 0);
						}
					}
					catch (Exception e) {
						// Bugsense was reporting a large crash count due to an NPE on threaded method
						// The stack trace wasn't valuable at all, so I'm just using an ugly try-catch
						// here (since I think this is the only Thread instance I use anyway).
					}
				}
			}).start();
		}
		catch (Exception e) {
			// Bugsense was reporting a large crash count due to an NPE on threaded method
			// The stack trace wasn't valuable at all, so I'm just using an ugly try-catch
			// here (since I think this is the only Thread instance I use anyway).
		}
	}

	public synchronized void setCurrentActivity(Activity activity) {
		mCurrentActivity = activity;
		if (mDeferFirstRunDialog) {
			onFirstRun();
		}
	}

	public synchronized Activity getCurrentActivity() {
		return mCurrentActivity;
	}

	public WorkerManager getWorkerManager() {
		return mWorkerManager;
	}

	public PersistenceManager getPersistenceManager() {
		return mPersistenceManager;
	}

	public Flex getFlex() {
		return mFlex;
	}

	public Settings getSettings() {
		if (mSettings == null) {
			mSettings = instantiateSettings();
		}
		return mSettings;
	}

	@Override
	public int getFleXML() {
		return Flexable.UNDEFINED;
	}

	// This is called after _sdCard is available but before _db is
	// This was added after version 78 (version 79 is the first "new" one)
	// Make this a listener
	@Override
	public void onVersionUpgrade(int oldVersion, int newVersion) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Upgrading the app from version " + oldVersion + " to " + newVersion);
		}
		if (oldVersion <= 78) {
			try {
				StorageManager external = mPersistenceManager.getExternalStorageManager();
				File db = this.getDatabasePath(DatabaseHelper.DATABASE_NAME); // Internal db file
				if (db != null && db.exists()) {
					File sdDB = external.getFile("receipts.db");
					if (sdDB.exists()) {
						sdDB.delete();
					}
					if (BuildConfig.DEBUG) {
						Log.d(TAG, "Copying the database file from " + db.getAbsolutePath() + " to " + sdDB.getAbsolutePath());
					}
					try {
						external.copy(db, sdDB, true);
					}
					catch (IOException e) {
						if (BuildConfig.DEBUG) {
							Log.e(TAG, e.toString());
						}
					}
				}
			}
			catch (SDCardStateException e) {
			}
			oldVersion++;
		}
	}

	@Override
	public final void onFirstRun() {
		if (mCurrentActivity != null) {
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "Launching first run dialog");
			}
			mDeferFirstRunDialog = false;
			showFirstRunDialog();
		}
		else {
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "Deferring first run dialog");
			}
			mDeferFirstRunDialog = true;
		}
	}

	protected void showFirstRunDialog() {
		/*
		 * final BetterDialogBuilder builder = new BetterDialogBuilder(mCurrentActivity);
		 * builder.setTitle(mFlex.getString(mCurrentActivity, R.string.DIALOG_WELCOME_TITLE))
		 * .setMessage(mFlex.getString(mCurrentActivity, R.string.DIALOG_WELCOME_MESSAGE))
		 * .setPositiveButton(mFlex.getString(mCurrentActivity, R.string.DIALOG_WELCOME_POSITIVE_BUTTON), new
		 * DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) { dialog.cancel(); } });
		 * mCurrentActivity.runOnUiThread(new Runnable() {
		 * 
		 * @Override public void run() { builder.show(); } });
		 */
	}

	@Override
	public void insertCategoryDefaults(final DatabaseHelper db) {
		final Resources resources = getResources();
		db.insertCategoryNoCache(resources.getString(R.string.category_null), resources.getString(R.string.category_null_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_airfare), resources.getString(R.string.category_airfare_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_breakfast), resources.getString(R.string.category_breakfast_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_dinner), resources.getString(R.string.category_dinner_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_entertainment), resources.getString(R.string.category_entertainment_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_gasoline), resources.getString(R.string.category_gasoline_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_gift), resources.getString(R.string.category_gift_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_hotel), resources.getString(R.string.category_hotel_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_laundry), resources.getString(R.string.category_laundry_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_lunch), resources.getString(R.string.category_lunch_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_other), resources.getString(R.string.category_other_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_parking_tolls), resources.getString(R.string.category_parking_tolls_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_postage_shipping), resources.getString(R.string.category_postage_shipping_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_car_rental), resources.getString(R.string.category_car_rental_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_taxi_bus), resources.getString(R.string.category_taxi_bus_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_telephone_fax), resources.getString(R.string.category_telephone_fax_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_tip), resources.getString(R.string.category_tip_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_train), resources.getString(R.string.category_train_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_books_periodicals), resources.getString(R.string.category_books_periodicals_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_cell_phone), resources.getString(R.string.category_cell_phone_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_dues_subscriptions), resources.getString(R.string.category_dues_subscriptions_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_meals_justified), resources.getString(R.string.category_meals_justified_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_stationery_stations), resources.getString(R.string.category_stationery_stations_code));
		db.insertCategoryNoCache(resources.getString(R.string.category_training_fees), resources.getString(R.string.category_training_fees_code));
	}

	@Override
	public void insertCSVDefaults(final DatabaseHelper db) { // Called in onCreate and onUpgrade
		@SuppressWarnings("unused")
		CSVColumns csv = new CSVColumns(getApplicationContext(), this);
		// TODO: Make this not so hacky and use really OOP
		db.insertCSVColumnNoCache(CSVColumns.CATEGORY_CODE(mFlex));
		db.insertCSVColumnNoCache(CSVColumns.NAME(getApplicationContext(), mFlex));
		db.insertCSVColumnNoCache(CSVColumns.PRICE(getApplicationContext(), mFlex));
		db.insertCSVColumnNoCache(CSVColumns.CURRENCY(getApplicationContext(), mFlex));
		db.insertCSVColumnNoCache(CSVColumns.DATE(getApplicationContext(), mFlex));
	}

	@Override
	public void insertPDFDefaults(DatabaseHelper db) {
		@SuppressWarnings("unused")
		PDFColumns pdf = new PDFColumns(getApplicationContext(), this);
		// TODO: Make this not so hacky and use really OOP
		db.insertPDFColumnNoCache(Columns.ColumnName.NAME);
		db.insertPDFColumnNoCache(Columns.ColumnName.PRICE);
		db.insertPDFColumnNoCache(Columns.ColumnName.DATE);
		db.insertPDFColumnNoCache(Columns.ColumnName.CATEGORY_NAME);
		db.insertPDFColumnNoCache(Columns.ColumnName.EXPENSABLE);
		db.insertPDFColumnNoCache(Columns.ColumnName.PICTURED);
	}

	/**
	 * Protected method to enable subclasses to create custom instances
	 * 
	 * @return a WorkerManager Instance
	 */
	protected WorkerManager instantiateWorkerManager() {
		return new WorkerManager(this);
	}

	/**
	 * Protected method to enable subclasses to create custom instances
	 * 
	 * @return a PersistenceManager Instance
	 */
	protected PersistenceManager instantiatePersistenceManager() {
		return new PersistenceManager(this);
	}

	/**
	 * Protected method to enable subclasses to create custom instances
	 * 
	 * @return a Flex Instance
	 */
	protected Flex instantiateFlex() {
		return Flex.getInstance(this, this);
	}

	/**
	 * Protected method to enable subclasses to create custom instances
	 * 
	 * @return a Settings Instance
	 */
	protected Settings instantiateSettings() {
		return new Settings(this);
	}

	public Class<? extends SmartReceiptsActivity> getTopLevelActivity() {
		return SmartReceiptsActivity.class;
	}

	@Override
	public void insertPaymentMethodDefaults(DatabaseHelper db) {
		db.insertPaymentMethodNoCache(getString(R.string.payment_method_default_unspecified));
		db.insertPaymentMethodNoCache(getString(R.string.payment_method_default_corporate_card));
		db.insertPaymentMethodNoCache(getString(R.string.payment_method_default_personal_card));
		db.insertPaymentMethodNoCache(getString(R.string.payment_method_default_cash));
		db.insertPaymentMethodNoCache(getString(R.string.payment_method_default_check));

	}

}