package co.smartreceipts.android;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;

import co.smartreceipts.android.analytics.AnalyticsManager;
import co.smartreceipts.android.analytics.impl.firebase.FirebaseAnalytics;
import co.smartreceipts.android.analytics.impl.logger.AnalyticsLogger;
import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import co.smartreceipts.android.apis.hosts.BetaSmartReceiptsHostConfiguration;
import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.config.DefaultConfigurationManager;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.store.MutableIdentityStore;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.ocr.OcrInteractor;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.TableControllerManager;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.push.PushManager;
import co.smartreceipts.android.rating.data.AppRatingPreferencesStorage;
import co.smartreceipts.android.settings.versions.AppVersionManager;
import co.smartreceipts.android.settings.versions.VersionUpgradedListener;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.android.utils.WBUncaughtExceptionHandler;
import co.smartreceipts.android.utils.cache.SmartReceiptsTemporaryFileCache;
import co.smartreceipts.android.utils.log.Logger;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;

/**
 * This extends GalleryAppImpl for the camera, since we can only define a single application in the manifest
 *
 * @author WRB
 */
public abstract class SmartReceiptsApplication extends Application implements VersionUpgradedListener {

    private Activity mCurrentActivity;
    private ConfigurationManager mConfigurationManager;
    private TableControllerManager mTableControllerManager;
    private AnalyticsManager mAnalyticsManager;
    private BackupProvidersManager mBackupProvidersManager;
    private NetworkManager mNetworkManager;
    private IdentityManager mIdentityManager;
    private PushManager pushManager;
    private OcrInteractor ocrInteractor;
    private PurchaseManager purchaseManager;
    private boolean mDeferFirstRunDialog;

    @Override
    public void onCreate() {
        super.onCreate();
        configureLog();
        WBUncaughtExceptionHandler.initialize();

        Logger.debug(this, "\n\n\n\n Launching App...");
    }

    protected void init() {
        PersistenceManager persistenceManager = getPersistenceManagerInternal();
        ReceiptColumnDefinitions receiptColumnDefinitions = getReceiptColumnDefinitionsInternal();
        PurchaseWallet purchaseWallet = getPurchaseWalletInternal();

        mDeferFirstRunDialog = false;
        mConfigurationManager = instantiateConfigurationManager();

        mAnalyticsManager = new AnalyticsManager(new AnalyticsLogger());
        mAnalyticsManager.register(new FirebaseAnalytics(this));


        mTableControllerManager = new TableControllerManager(persistenceManager, mAnalyticsManager,
                receiptColumnDefinitions);
        mNetworkManager = new NetworkManager(this, persistenceManager.getPreferenceManager());
        mNetworkManager.initialize();
        mBackupProvidersManager = new BackupProvidersManager(this, persistenceManager.getDatabase(), getTableControllerManager(), mNetworkManager, mAnalyticsManager);

        final MutableIdentityStore identityStore = new MutableIdentityStore(this);
        ServiceManager serviceManager = new ServiceManager(new BetaSmartReceiptsHostConfiguration(identityStore, new SmartReceiptsGsonBuilder(receiptColumnDefinitions)));
        mIdentityManager = new IdentityManager(this, identityStore, serviceManager, mAnalyticsManager, persistenceManager.getPreferenceManager());
        pushManager = new PushManager(this, mIdentityManager);
        pushManager.initialize();

        ocrInteractor = new OcrInteractor(this, serviceManager, pushManager);

        purchaseManager = new PurchaseManager(this, purchaseWallet, mAnalyticsManager);
        purchaseManager.initialize(this);

        PDFBoxResourceLoader.init(getApplicationContext());

        // Clear our cache
        new SmartReceiptsTemporaryFileCache(this).resetCache();

        // Check if a new version is available
        new AppVersionManager(this, persistenceManager.getPreferenceManager()).onLaunch(this);

        // Add launch count for rating prompt monitoring
        new AppRatingPreferencesStorage(getApplicationContext()).incrementLaunchCount();
    }


    // TODO: 16.03.2017 temporаry methods, will be removed
    protected abstract PersistenceManager getPersistenceManagerInternal();
    protected abstract ReceiptColumnDefinitions getReceiptColumnDefinitionsInternal();
    protected abstract PurchaseWallet getPurchaseWalletInternal();

    private void configureLog() {
        final String logDirPath = getFilesDir().getPath() ;
        System.setProperty("LOG_DIR", logDirPath);
    }

    @Override
    public void onTerminate() {
        // Note: This is useful for unit tests but never gets called on actual devices
        mCurrentActivity = null;
        getPersistenceManagerInternal().onDestroy();
//        mPersistenceManager = null;
        super.onTerminate();
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
    public IdentityManager getIdentityManager() {
        return mIdentityManager;
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

    @NonNull
    public NetworkManager getNetworkManager() {
        return mNetworkManager;
    }

    @NonNull
    public PushManager getPushManager() {
        return pushManager;
    }

    @NonNull
    public OcrInteractor getOcrInteractor() {
        return ocrInteractor;
    }

    @NonNull
    public PurchaseManager getPurchaseManager() {
        return purchaseManager;
    }

    // This is called after _sdCard is available but before _db is
    // This was added after version 78 (version 79 is the first "new" one)
    // Make this a listener
    @Override
    public void onVersionUpgrade(int oldVersion, int newVersion) {
        Logger.debug(this, "Upgrading the app from version {} to {}", oldVersion, newVersion);
        if (oldVersion <= 78) {
            try {
                StorageManager external = getPersistenceManagerInternal().getExternalStorageManager();
                File db = this.getDatabasePath(DatabaseHelper.DATABASE_NAME); // Internal db file
                if (db != null && db.exists()) {
                    File sdDB = external.getFile("receipts.db");
                    if (sdDB.exists()) {
                        sdDB.delete();
                    }
                    Logger.debug(this, "Copying the database file from {} to {}", db.getAbsolutePath(), sdDB.getAbsolutePath());
                    try {
                        external.copy(db, sdDB, true);
                    } catch (IOException e) {
                        Logger.error(this, "Exception occurred when upgrading app version", e);
                    }
                }
            } catch (SDCardStateException e) {
            }
        }
    }

    // TODO: Update/fix
    public final void onFirstRun() {
        if (mCurrentActivity != null) {
            Logger.debug(this, "Launching first run dialog");
            mDeferFirstRunDialog = false;
            showFirstRunDialog();
        } else {
            if (BuildConfig.DEBUG) {
                Logger.debug(this, "Deferring first run dialog");
            }
            mDeferFirstRunDialog = true;
        }
    }

    protected void showFirstRunDialog() {

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
