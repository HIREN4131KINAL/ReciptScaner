package co.smartreceipts.android.activities;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.fragments.TripFragment;
import co.smartreceipts.android.model.Attachment;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.purchases.PurchaseableSubscription;
import co.smartreceipts.android.purchases.Subscription;
import co.smartreceipts.android.purchases.SubscriptionEventsListener;
import co.smartreceipts.android.purchases.SubscriptionManager;
import co.smartreceipts.android.purchases.SubscriptionWallet;
import wb.android.dialog.BetterDialogBuilder;
import wb.android.util.AppRating;

public class SmartReceiptsActivity extends WBActivity implements Attachable, SubscriptionEventsListener {

    // logging variables
    static final String TAG = "SmartReceiptsActivity";

    // Camera Request Extras
    public static final String STRING_DATA = "strData";
    public static final int DIR = 0;
    public static final int NAME = 1;

    // AppRating (Use a combination of launches and a timer for the app rating
    // to ensure that we aren't prompting new users too soon
    private static final int LAUNCHES_UNTIL_PROMPT = 30;
    private static final int DAYS_UNTIL_PROMPT = 7;

    private NavigationHandler mNavigationHandler;
    private SubscriptionManager mSubscriptionManager;
    private Attachment mAttachment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mNavigationHandler = new NavigationHandler(this, getSupportFragmentManager(), new DefaultFragmentProvider());
        setContentView(R.layout.activity_main);
        mSubscriptionManager = new SubscriptionManager(this, ((SmartReceiptsApplication)getApplication()).getPersistenceManager().getSubscriptionCache());

        if (savedInstanceState == null) {
            Log.d(TAG, "savedInstanceState == null");
            mNavigationHandler.navigateToTripsFragment();
            AppRating.initialize(this).setMinimumLaunchesUntilPrompt(LAUNCHES_UNTIL_PROMPT).setMinimumDaysUntilPrompt(DAYS_UNTIL_PROMPT).hideIfAppCrashed(true).setPackageName(getPackageName()).showDialog(true).onLaunch();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        if (!getSmartReceiptsApplication().getPersistenceManager().getStorageManager().isExternal()) {
            Toast.makeText(SmartReceiptsActivity.this, getSmartReceiptsApplication().getFlex().getString(this, R.string.SD_WARNING), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // Present dialog for viewing an attachment
        final Attachment attachment = new Attachment(getIntent(), getContentResolver());
        setAttachment(attachment);
        if (attachment.isValid() && attachment.isDirectlyAttachable()) {
            final Preferences preferences = getSmartReceiptsApplication().getPersistenceManager().getPreferences();
            final int stringId = attachment.isPDF() ? R.string.pdf : R.string.image;
            if (preferences.showActionSendHelpDialog()) {
                BetterDialogBuilder builder = new BetterDialogBuilder(this);
                builder.setTitle(getString(R.string.dialog_attachment_title, getString(stringId))).setMessage(getString(R.string.dialog_attachment_text, getString(stringId))).setPositiveButton(R.string.dialog_attachment_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setNegativeButton(R.string.dialog_attachment_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preferences.setShowActionSendHelpDialog(false);
                        preferences.commit();
                        dialog.cancel();
                    }
                }).show();
            } else {
                Toast.makeText(this, getString(R.string.dialog_attachment_text, getString(stringId)), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mSubscriptionManager.onActivityResult(requestCode, resultCode, data, /* TODO: */ null)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_main_settings) {
            SRNavUtils.showSettings(this);
            return true;
        } else if (item.getItemId() == R.id.menu_main_export) {
            final Fragment tripsFragment = getSupportFragmentManager().findFragmentByTag(TripFragment.TAG);
            getSmartReceiptsApplication().getSettings().showExport(tripsFragment);
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
    protected void onDestroy() {
        getSmartReceiptsApplication().getPersistenceManager().getDatabase().onDestroy();
        super.onDestroy();
    }

    @Override
    public String getTag() {
        return TAG;
    }

    /**
     * Returns the attachment that is generated via the main activity
     *
     * @return
     */
    @Override
    public Attachment getAttachment() {
        return mAttachment;
    }

    /**
     * Stores the main attachment details for later
     */
    @Override
    public void setAttachment(Attachment attachment) {
        mAttachment = attachment;
    }


    @Override
    public void onSubscriptionsAvailable(@NonNull List<PurchaseableSubscription> subscriptions, @NonNull SubscriptionWallet subscriptionWallet) {
        for (final PurchaseableSubscription purchaseableSubscription : subscriptions) {
            if (Subscription.SmartReceiptsPro == purchaseableSubscription.getSubscription()) {
                // TODO: Add to buy menu
            }
        }
    }

    @Override
    public void onSubscriptionsUnavailable() {
        // Intentional no-op
    }

    @Override
    public void onPurchaseIntentAvailable(@NonNull Subscription subscription, @NonNull PendingIntent pendingIntent, @NonNull String key) {
        // TODO: Start for result
    }

    @Override
    public void onPurchaseIntentUnavailable(@NonNull Subscription subscription) {
        Toast.makeText(this, R.string.purchase_unavailable, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPurchaseSuccess(@NonNull Subscription subscription, @NonNull SubscriptionWallet updateSubscriptionWallet) {
        Toast.makeText(this, R.string.purchase_succeeded, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPurchaseFailed() {
        Toast.makeText(this, R.string.purchase_failed, Toast.LENGTH_LONG).show();
    }
}