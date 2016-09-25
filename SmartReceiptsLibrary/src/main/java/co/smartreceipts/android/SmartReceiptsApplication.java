package co.smartreceipts.android;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import co.smartreceipts.android.analytics.AnalyticsLogger;
import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.config.DefaultConfigurationManager;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.database.controllers.TableControllerManager;
import co.smartreceipts.android.purchases.DefaultSubscriptionCache;
import co.smartreceipts.android.purchases.SubscriptionCache;
import co.smartreceipts.android.sync.BackupProvidersManager;
import wb.android.flex.Flex;
import wb.android.flex.Flexable;
import wb.android.google.camera.app.GalleryAppImpl;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;
import wb.android.util.AppRating;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.util.Log;

import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.persistence.SharedPreferenceDefinitions;
import co.smartreceipts.android.utils.WBUncaughtExceptionHandler;
import co.smartreceipts.android.workers.WorkerManager;

/**
 * This extends GalleryAppImpl for the camera, since we can only define a single application in the manifest
 * 
 * @author WRB
 * 
 */
public class SmartReceiptsApplication extends GalleryAppImpl implements Flexable, Preferences.VersionUpgradeListener {

	public static final String TAG = "SmartReceiptsApp";

	private WorkerManager mWorkerManager;
	private PersistenceManager mPersistenceManager;
	private Flex mFlex;
	private Activity mCurrentActivity;
	private ConfigurationManager mConfigurationManager;
    private TableControllerManager mTableControllerManager;
    private AnalyticsManager mAnalyticsManager;
    private BackupProvidersManager mBackupProvidersManager;
	private boolean mDeferFirstRunDialog;

	/**
	 * The {@link Application} class is a singleton, so we can cache it here for emergency restoration
	 */
	private static SmartReceiptsApplication sApplication;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
	public void onCreate() {
		super.onCreate();
		preloadSharedPreferences();
		WBUncaughtExceptionHandler.initialize();
		sApplication = this;
		mDeferFirstRunDialog = false;
		mFlex = instantiateFlex();
		mConfigurationManager = instantiateConfigurationManager();
		mWorkerManager = instantiateWorkerManager();
		mPersistenceManager = instantiatePersistenceManager();
        mPersistenceManager.initDatabase(); // TODO: Fix anti-pattern
		mPersistenceManager.getPreferences().setVersionUpgradeListener(this); // Done so mPersistenceManager is not null
																				// in onVersionUpgrade
        mTableControllerManager = new TableControllerManager(mPersistenceManager, new ReceiptColumnDefinitions(this, mPersistenceManager.getDatabase(), mPersistenceManager.getPreferences(), mFlex));
        mAnalyticsManager = new AnalyticsManager(new AnalyticsLogger());
        mBackupProvidersManager = new BackupProvidersManager(this, getPersistenceManager().getDatabase(), getTableControllerManager());
	}

    @Deprecated
	public static SmartReceiptsApplication getInstance() {
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

    @NonNull
	public WorkerManager getWorkerManager() {
		return mWorkerManager;
	}

    @NonNull
	public PersistenceManager getPersistenceManager() {
		return mPersistenceManager;
	}

    @NonNull
	public Flex getFlex() {
		return mFlex;
	}

    @NonNull
	public ConfigurationManager getConfigurationManager() {
		return mConfigurationManager;
	}

    @NonNull
    public TableControllerManager getTableControllerManager() {
        return mTableControllerManager;
    }

    @NonNull
    public AnalyticsManager getAnalyticsManager() {
        return mAnalyticsManager;
    }

    @NonNull
    public BackupProvidersManager getBackupProvidersManager() {
        return mBackupProvidersManager;
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

    // TODO: Update/fix
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
		return new PersistenceManager(this, instantiateSubscriptionCache());
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
     * @return a SubscriptionCache Instance
     */
    protected SubscriptionCache instantiateSubscriptionCache() {
        return new DefaultSubscriptionCache(this);
    }

	/**
	 * Protected method to enable subclasses to create custom instances
	 *
	 * @return a ConfigurationManager Instance
	 */
	protected ConfigurationManager instantiateConfigurationManager() {
		return new DefaultConfigurationManager(this);
	}

}