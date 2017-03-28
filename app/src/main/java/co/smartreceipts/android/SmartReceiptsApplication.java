package co.smartreceipts.android;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.squareup.leakcanary.LeakCanary;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import co.smartreceipts.android.apis.hosts.BetaSmartReceiptsHostConfiguration;
import co.smartreceipts.android.apis.hosts.ServiceManager;
import co.smartreceipts.android.aws.s3.S3Manager;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.config.DefaultConfigurationManager;
import co.smartreceipts.android.di.AppComponent;
import co.smartreceipts.android.di.BaseAppModule;
import co.smartreceipts.android.di.DaggerAppComponent;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.aws.cognito.CognitoManager;
import co.smartreceipts.android.identity.store.MutableIdentityStore;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.ocr.OcrInteractor;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
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
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasDispatchingActivityInjector;
import dagger.android.support.HasDispatchingSupportFragmentInjector;
import wb.android.storage.SDCardStateException;
import wb.android.storage.StorageManager;

public class SmartReceiptsApplication extends Application implements VersionUpgradedListener,
        HasDispatchingActivityInjector, HasDispatchingSupportFragmentInjector {


    @Inject
    DispatchingAndroidInjector<Activity> activityInjector;
    @Inject
    DispatchingAndroidInjector<Fragment> supportFragmentInjector;

    @Inject
    PersistenceManager persistenceManager;
    @Inject
    ReceiptColumnDefinitions receiptColumnDefinitions;
    @Inject
    PurchaseWallet purchaseWallet;
    @Inject
    ExtraInitializer extraInitializer;
    @Inject
    NetworkManager networkManager;
    @Inject
    Analytics analytics;
    @Inject
    PurchaseManager purchaseManager;
    @Inject
    IdentityManager identityManager;
    @Inject
    PushManager pushManager;


//    private ConfigurationManager mConfigurationManager;
    private TableControllerManager mTableControllerManager;
//    private AnalyticsManager mAnalyticsManager;
    private BackupProvidersManager mBackupProvidersManager;
//    private NetworkManager mNetworkManager;
//    private IdentityManager mIdentityManager;
//    private PushManager pushManager;
    private OcrInteractor ocrInteractor;
//    private PurchaseManager purchaseManager;
    private CognitoManager cognitoManager;
    private AppComponent appComponent;


    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent.builder()
                .baseAppModule(new BaseAppModule(this))
                .build();

        appComponent.inject(this);

        configureLog();
        WBUncaughtExceptionHandler.initialize();

        Logger.debug(this, "\n\n\n\n Launching App...");

        init();

        extraInitializer.init();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return activityInjector;
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return supportFragmentInjector;
    }

    private void init() {
        mTableControllerManager = new TableControllerManager(persistenceManager, analytics,
                receiptColumnDefinitions);

        mBackupProvidersManager = new BackupProvidersManager(this, persistenceManager.getDatabase(),
                getTableControllerManager(), networkManager, analytics);

        final MutableIdentityStore identityStore = new MutableIdentityStore(this);
        ServiceManager serviceManager = new ServiceManager(new BetaSmartReceiptsHostConfiguration(identityStore,
                new SmartReceiptsGsonBuilder(receiptColumnDefinitions)));


//        pushManager = new PushManager(this, identityManager);
        pushManager.initialize();

        purchaseManager.initialize(this);

        cognitoManager = new CognitoManager(this, identityManager);
        cognitoManager.initialize();

        //ocrInteractor = new OcrInteractor(this, serviceManager, pushManager);
        ocrInteractor = new OcrInteractor(this, new S3Manager(this, cognitoManager), mIdentityManager, serviceManager, pushManager, new OcrPurchaseTracker(this, serviceManager, purchaseManager, purchaseWallet));
        ocrInteractor.initialize();

        PDFBoxResourceLoader.init(getApplicationContext());

        // Clear our cache
        new SmartReceiptsTemporaryFileCache(this).resetCache();

        // Check if a new version is available
        new AppVersionManager(this, persistenceManager.getPreferenceManager()).onLaunch(this);

        // Add launch count for rating prompt monitoring
        new AppRatingPreferencesStorage(getApplicationContext()).incrementLaunchCount();

        // LeakCanary initialization
        if (LeakCanary.isInAnalyzerProcess(this)) {
            Logger.debug(this, "Ignoring this process as it's the LeakCanary analyzer one...");
        } else {
            LeakCanary.install(this);
        }
    }

    private void configureLog() {
        final String logDirPath = getFilesDir().getPath();
        System.setProperty("LOG_DIR", logDirPath);
    }

    @NonNull
    public TableControllerManager getTableControllerManager() {
        return mTableControllerManager;
    }

    @NonNull
    public BackupProvidersManager getBackupProvidersManager() {
        return mBackupProvidersManager;
    }

    @NonNull
    public OcrInteractor getOcrInteractor() {
        return ocrInteractor;
    }

    // This is called after _sdCard is available but before _db is
    // This was added after version 78 (version 79 is the first "new" one)
    // Make this a listener
    @Override
    public void onVersionUpgrade(int oldVersion, int newVersion) {
        Logger.debug(this, "Upgrading the app from version {} to {}", oldVersion, newVersion);
        if (oldVersion <= 78) {
            try {
                StorageManager external = persistenceManager.getExternalStorageManager();
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
}
