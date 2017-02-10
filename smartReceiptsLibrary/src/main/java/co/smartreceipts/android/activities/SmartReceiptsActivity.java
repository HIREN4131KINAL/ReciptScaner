package co.smartreceipts.android.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.fragments.InformAboutPdfImageAttachmentDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ImportLocalBackupDialogFragment;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.model.Attachment;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.purchases.PurchaseSource;
import co.smartreceipts.android.purchases.PurchaseableSubscriptions;
import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.SubscriptionEventsListener;
import co.smartreceipts.android.purchases.SubscriptionManager;
import co.smartreceipts.android.purchases.SubscriptionWallet;
import co.smartreceipts.android.utils.FeatureFlags;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.rating.AppRating;

public class SmartReceiptsActivity extends WBActivity implements Attachable, SubscriptionEventsListener {

    private static final int STORAGE_PERMISSION_REQUEST = 33;
    private static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    // AppRating (Use a combination of launches and a timer for the app rating
    // to ensure that we aren't prompting new users too soon
    private static final int LAUNCHES_UNTIL_PROMPT = 30;
    private static final int DAYS_UNTIL_PROMPT = 7;

    private volatile PurchaseableSubscriptions mPurchaseableSubscriptions;
    private NavigationHandler mNavigationHandler;
    private SubscriptionManager mSubscriptionManager;
    private Attachment mAttachment;
    private BackupProvidersManager mBackupProvidersManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug(this, "onCreate");

        mNavigationHandler = new NavigationHandler(this, getSupportFragmentManager(), new FragmentProvider());
        mSubscriptionManager = new SubscriptionManager(this, getSmartReceiptsApplication().getPersistenceManager().getSubscriptionCache(), getSmartReceiptsApplication().getAnalyticsManager());
        mSubscriptionManager.onCreate();
        mSubscriptionManager.addEventListener(this);
        mSubscriptionManager.querySubscriptions();

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            Logger.debug(this, "savedInstanceState == null");
            mNavigationHandler.navigateToHomeTripsFragment();
            AppRating.initialize(this).setMinimumLaunchesUntilPrompt(LAUNCHES_UNTIL_PROMPT).setMinimumDaysUntilPrompt(DAYS_UNTIL_PROMPT).hideIfAppCrashed(true).setPackageName(getPackageName()).showDialog(true).onLaunch();
        }
        getSmartReceiptsApplication().getWorkerManager().getAdManager().onActivityCreated(this, mSubscriptionManager);

        mBackupProvidersManager = getSmartReceiptsApplication().getBackupProvidersManager();
        mBackupProvidersManager.initialize(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.debug(this, "onStart");

        if (!getSmartReceiptsApplication().getPersistenceManager().getStorageManager().isExternal()) {
            Toast.makeText(SmartReceiptsActivity.this, getSmartReceiptsApplication().getFlex().getString(this, R.string.SD_WARNING), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Logger.debug(this, "onResumeFragments");

        // Present dialog for viewing an attachment
        final Attachment attachment = new Attachment(getIntent(), getContentResolver());
        setAttachment(attachment);
        if (attachment.isValid()) {
            final boolean hasStoragePermission = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (attachment.requiresStoragePermissions() && !hasStoragePermission) {
                ActivityCompat.requestPermissions(this, new String[] { READ_EXTERNAL_STORAGE }, STORAGE_PERMISSION_REQUEST);
            } else if (attachment.isDirectlyAttachable()) {
                final Preferences preferences = getSmartReceiptsApplication().getPersistenceManager().getPreferences();
                if (InformAboutPdfImageAttachmentDialogFragment.shouldInformAboutPdfImageAttachmentDialogFragment(preferences)) {
                    mNavigationHandler.showDialog(InformAboutPdfImageAttachmentDialogFragment.newInstance(attachment));
                } else {
                    final int stringId = attachment.isPDF() ? R.string.pdf : R.string.image;
                    Toast.makeText(this, getString(R.string.dialog_attachment_text, getString(stringId)), Toast.LENGTH_LONG).show();
                }
            } else if (attachment.isSMR() && attachment.isActionView()) {
                mNavigationHandler.showDialog(ImportLocalBackupDialogFragment.newInstance(attachment.getUri()));
            }
        }
        getSmartReceiptsApplication().getWorkerManager().getAdManager().onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mSubscriptionManager.onActivityResult(requestCode, resultCode, data)) {
            if (!mBackupProvidersManager.onActivityResult(requestCode, resultCode, data)) {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final boolean haveProSubscription = ((SmartReceiptsApplication)getApplication()).getPersistenceManager().getSubscriptionCache().getSubscriptionWallet().hasSubscription(Subscription.SmartReceiptsPlus);
        final boolean proSubscriptionIsAvailable = mPurchaseableSubscriptions != null && mPurchaseableSubscriptions.isSubscriptionAvailableForPurchase(Subscription.SmartReceiptsPlus);

        // If the pro sub is either unavailable or we already have it, don't show the purchase menu option
        if (!proSubscriptionIsAvailable || haveProSubscription) {
            menu.removeItem(R.id.menu_main_pro_subscription);
        }

        // If we disabled settings in our config, let's remove it
        if (!getSmartReceiptsApplication().getConfigurationManager().isSettingsMenuAvailable()) {
            menu.removeItem(R.id.menu_main_settings);
        }

        if (!FeatureFlags.SmartReceiptsLogin.isEnabled()) {
            menu.removeItem(R.id.menu_main_my_account);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_main_settings) {
            mNavigationHandler.navigateToSettings();
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Navigation.SettingsOverflow);
            return true;
        } else if (item.getItemId() == R.id.menu_main_export) {
            mNavigationHandler.navigateToBackupMenu();
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Navigation.BackupOverflow);
            return true;
        } else if (item.getItemId() == R.id.menu_main_pro_subscription) {
            mSubscriptionManager.queryBuyIntent(Subscription.SmartReceiptsPlus, PurchaseSource.OverflowMenu);
            getSmartReceiptsApplication().getAnalyticsManager().record(Events.Navigation.SmartReceiptsPlusOverflow);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (mNavigationHandler.shouldFinishOnBackNaviagtion()) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        Logger.info(this, "onPause");
        getSmartReceiptsApplication().getWorkerManager().getAdManager().onPause();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Logger.debug(this, "pre-onSaveInstanceState");
        super.onSaveInstanceState(outState);
        Logger.debug(this, "post-onSaveInstanceState");
    }

    @Override
    protected void onDestroy() {
        Logger.info(this, "onDestroy");
        getSmartReceiptsApplication().getWorkerManager().getAdManager().onDestroy();
        mSubscriptionManager.removeEventListener(this);
        mSubscriptionManager.onDestroy();
        getSmartReceiptsApplication().getPersistenceManager().getDatabase().onDestroy();
        super.onDestroy();
    }

    @Override
    public Attachment getAttachment() {
        return mAttachment;
    }

    @Override
    public void setAttachment(Attachment attachment) {
        mAttachment = attachment;
    }
    
    @Override
    public void onSubscriptionsAvailable(@NonNull PurchaseableSubscriptions purchaseableSubscriptions, @NonNull SubscriptionWallet subscriptionWallet) {
        Logger.info(this, "The following subscriptions are available: {}", purchaseableSubscriptions);
        mPurchaseableSubscriptions = purchaseableSubscriptions;
        invalidateOptionsMenu(); // To show the subscription option
    }

    @Override
    public void onSubscriptionsUnavailable() {
        Logger.warn(this, "No subscriptions were found for this session");
    }

    @Override
    public void onPurchaseIntentAvailable(@NonNull Subscription subscription, @NonNull PendingIntent pendingIntent, @NonNull String key) {
        try {
            startIntentSenderForResult(pendingIntent.getIntentSender(), SubscriptionManager.REQUEST_CODE, new Intent(), 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SmartReceiptsActivity.this, R.string.purchase_unavailable, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onPurchaseIntentUnavailable(@NonNull Subscription subscription) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SmartReceiptsActivity.this, R.string.purchase_unavailable, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPurchaseSuccess(@NonNull final Subscription subscription, @NonNull final PurchaseSource purchaseSource, @NonNull SubscriptionWallet updatedSubscriptionWallet) {
        getSmartReceiptsApplication().getAnalyticsManager().record(new DefaultDataPointEvent(Events.Purchases.PurchaseSuccess).addDataPoint(new DataPoint("sku", subscription.getSku())).addDataPoint(new DataPoint("source", purchaseSource)));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu(); // To hide the subscription option
                Toast.makeText(SmartReceiptsActivity.this, R.string.purchase_succeeded, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPurchaseFailed(@NonNull final PurchaseSource purchaseSource) {
        getSmartReceiptsApplication().getAnalyticsManager().record(new DefaultDataPointEvent(Events.Purchases.PurchaseFailed).addDataPoint(new DataPoint("source", purchaseSource)));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SmartReceiptsActivity.this, R.string.purchase_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Nullable
    public SubscriptionManager getSubscriptionManager() {
        return mSubscriptionManager;
    }
}
