package wb.receiptslibrary;

import java.io.File;
import java.io.IOException;

import wb.android.dialog.BetterDialogBuilder;
import wb.android.flex.Flex;
import wb.android.flex.Flexable;
import wb.android.google.camera.app.GalleryAppImpl;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;
import wb.receiptslibrary.activities.SmartReceiptsActivity;
import wb.receiptslibrary.fragments.Settings;
import wb.receiptslibrary.persistence.DatabaseHelper;
import wb.receiptslibrary.persistence.PersistenceManager;
import wb.receiptslibrary.persistence.Preferences;
import wb.receiptslibrary.workers.WorkerManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;

/**
 * This extends GalleryAppImpl for the camera, since we can only define a single
 * application in the manifest
 * @author WRB
 *
 */
public class SmartReceiptsApplication extends GalleryAppImpl implements Flexable, 
																	    Preferences.VersionUpgradeListener,
																	    DatabaseHelper.TableDefaultsCustomizer {
	
	public static final String TAG = "SmartReceiptsApplication";
	
	private WorkerManager mWorkerManager;
    private PersistenceManager mPersistenceManager;
    private Flex mFlex;
    private Activity mCurrentActivity;
    private Settings mSettings;
    private boolean mDeferFirstRunDialog;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mDeferFirstRunDialog = false;
		mFlex = instantiateFlex();
		mWorkerManager = instantiateWorkerManager();
		mPersistenceManager = instantiatePersistenceManager();
		mPersistenceManager.getPreferences().setVersionUpgradeListener(this); // Done so mPersistenceManager is not null in onVersionUpgrade
	}
	
	@Override
	public void onTerminate() {
		mCurrentActivity = null;
		mPersistenceManager.onDestroy();
		mWorkerManager.onDestroy();
		mPersistenceManager = null;
		mWorkerManager = null;
		super.onTerminate();
	}
	
	public void setCurrentActivity(Activity activity) {
		mCurrentActivity = activity;
		if (mDeferFirstRunDialog) {
			onFirstRun();
		}
	}
	
	public Activity getCurrentActivity() {
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

	//This is called after _sdCard is available but before _db is
    //This was added after version 78 (version 79 is the first "new" one)
    //Make this a listener
	@Override
    public void onVersionUpgrade(int oldVersion, int newVersion) {
    	if (BuildConfig.DEBUG) Log.d(TAG, "Upgrading the app from version " + oldVersion + " to " + newVersion);
    	if (oldVersion <= 78) {
			try {
				StorageManager external = mPersistenceManager.getExternalStorageManager();
				File db = this.getDatabasePath(DatabaseHelper.DATABASE_NAME); //Internal db file
				if (db != null && db.exists()) {
					File sdDB = external.getFile("receipts.db"); 
					if (sdDB.exists())
						sdDB.delete();
					if (BuildConfig.DEBUG) Log.d(TAG, "Copying the database file from " + db.getAbsolutePath() + " to " + sdDB.getAbsolutePath());
					try {
						external.copy(db, sdDB, true);
					}
					catch (IOException e) {
						if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
					}
				}
			}
			catch (SDCardStateException e) { }
			oldVersion++;
		}
    }

	@Override
	public void onFirstRun() {
    	if (mCurrentActivity != null) {
    		if (BuildConfig.DEBUG) Log.d(TAG, "Launching first run dialog");
    		mDeferFirstRunDialog = false;
        	final BetterDialogBuilder builder = new BetterDialogBuilder(mCurrentActivity);
        	builder.setTitle(mFlex.getString(R.string.DIALOG_WELCOME_TITLE))
        		   .setMessage(mFlex.getString(R.string.DIALOG_WELCOME_MESSAGE))
        		   .setPositiveButton(mFlex.getString(R.string.DIALOG_WELCOME_POSITIVE_BUTTON), new DialogInterface.OnClickListener() {
    					@Override
    					public void onClick(DialogInterface dialog, int which) {
    						dialog.cancel();
    					}
        		   });
    		mCurrentActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					builder.show();
				}
			});
    	}
    	else {
    		if (BuildConfig.DEBUG) Log.d(TAG, "Deferring first run dialog");
    		mDeferFirstRunDialog = true;
    	}
    }

	@Override
	public void insertCategoryDefaults(final DatabaseHelper db) {
		db.insertCategoryNoCache("<Category>", "NUL");
		db.insertCategoryNoCache("Airfare", "AIRP");
		db.insertCategoryNoCache("Breakfast", "BRFT");
		db.insertCategoryNoCache("Dinner", "DINN");
		db.insertCategoryNoCache("Entertainment", "ENT");
		db.insertCategoryNoCache("Gasoline", "GAS");
		db.insertCategoryNoCache("Gift", "GIFT");
		db.insertCategoryNoCache("Hotel", "HTL");
		db.insertCategoryNoCache("Laundry", "LAUN");
		db.insertCategoryNoCache("Lunch", "LNCH");
		db.insertCategoryNoCache("Other", "MISC");
		db.insertCategoryNoCache("Parking/Tolls", "PARK");
		db.insertCategoryNoCache("Postage/Shipping", "POST");
		db.insertCategoryNoCache("Car Rental", "RCAR");
		db.insertCategoryNoCache("Taxi/Bus", "TAXI");
		db.insertCategoryNoCache("Telephone/Fax", "TELE");
		db.insertCategoryNoCache("Tip", "TIP");
		db.insertCategoryNoCache("Train", "TRN");
		db.insertCategoryNoCache("Books/Periodicals", "ZBKP");
		db.insertCategoryNoCache("Cell Phone", "ZCEL");
		db.insertCategoryNoCache("Dues/Subscriptions", "ZDUE");
		db.insertCategoryNoCache("Meals (Justified)", "ZMEO");
		db.insertCategoryNoCache("Stationery/Stations", "ZSTS");
		db.insertCategoryNoCache("Training Fees", "ZTRN");
    }

	@Override
	public void insertCSVDefaults(final DatabaseHelper db) { //Called in onCreate and onUpgrade
		db.insertCSVColumnNoCache(CSVColumns.CATEGORY_CODE(mFlex));
		db.insertCSVColumnNoCache(CSVColumns.NAME(mFlex));
		db.insertCSVColumnNoCache(CSVColumns.PRICE(mFlex));
		db.insertCSVColumnNoCache(CSVColumns.CURRENCY(mFlex));
		db.insertCSVColumnNoCache(CSVColumns.DATE(mFlex));
	}
	
	/**
	 * Protected method to enable subclasses to create custom instances
	 * @return a WorkerManager Instance
	 */
	protected WorkerManager instantiateWorkerManager() {
		return new WorkerManager(this);
	}
	
	/**
	 * Protected method to enable subclasses to create custom instances
	 * @return a PersistenceManager Instance
	 */
    protected PersistenceManager instantiatePersistenceManager() {
    	return new PersistenceManager(this);
    }
    
    /**
	 * Protected method to enable subclasses to create custom instances
	 * @return a Flex Instance
	 */
    protected Flex instantiateFlex() {
    	return Flex.getInstance(this, this);
    }
    
    /**
	 * Protected method to enable subclasses to create custom instances
	 * @return a Settings Instance
	 */
    protected Settings instantiateSettings() {
    	return new Settings(this);
    }
	
	public Class<? extends SmartReceiptsActivity> getTopLevelActivity() {
		return SmartReceiptsActivity.class;
	}

}