package wb.receipts;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.bugsense.trace.BugSenseHandler;
import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.GoogleAnalytics;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.purchases.wallet.DefaultPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasDispatchingActivityInjector;
import dagger.android.support.HasDispatchingSupportFragmentInjector;
import wb.receipts.di.FreeAppComponent;
import wb.receipts.di.DaggerFreeAppComponent;

public class SmartReceiptsFreeApplication extends SmartReceiptsApplication
        implements HasDispatchingActivityInjector, HasDispatchingSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityInjector;

    @Inject
    DispatchingAndroidInjector<Fragment> supportFragmentInjector;

    @Inject PersistenceManager persistenceManager;
    @Inject ReceiptColumnDefinitions receiptColumnDefinitions;
    @Inject DefaultPurchaseWallet purchaseWallet;


    @Override
    public void onCreate() {
        super.onCreate();

        DaggerFreeAppComponent.builder()
                .freeAppModule(new FreeAppComponent.FreeAppModule(this))
                .build()
                .inject(this);

        super.init();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            Logger.debug(this, "Ignoring this process as it's the LeakCanary analyzer one...");
            return;
        } else {
            LeakCanary.install(this);
        }
        BugSenseHandler.initAndStartSession(this, "01de172a");

        getAnalyticsManager().register(new GoogleAnalytics(this));

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

    @Override
    protected PurchaseWallet getPurchaseWalletInternal() {
        return purchaseWallet;
    }
}
