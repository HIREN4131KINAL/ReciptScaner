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
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasDispatchingActivityInjector;
import dagger.android.support.HasDispatchingSupportFragmentInjector;
import wb.receipts.di.AppComponent;
import wb.receipts.di.DaggerAppComponent;

public class SmartReceiptsFreeApplication extends SmartReceiptsApplication
        implements HasDispatchingActivityInjector, HasDispatchingSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityInjector;

    @Inject
    DispatchingAndroidInjector<Fragment> supportFragmentInjector;

    @Inject PersistenceManager persistenceManager;
    @Inject ReceiptColumnDefinitions receiptColumnDefinitions;


    @Override
    public void onCreate() {
        super.onCreate();

        DaggerAppComponent.builder()
                .appModule(new AppComponent.AppModule(this))
                .build()
                .inject(this);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            Logger.debug(this, "Ignoring this process as it's the LeakCanary analyzer one...");
            return;
        } else {
            LeakCanary.install(this);
        }
        BugSenseHandler.initAndStartSession(this, "01de172a");
        getAnalyticsManager().register(new GoogleAnalytics(this));

        super.init();
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
}
