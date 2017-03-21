package co.smartreceipts.android.purchases;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.Subscription;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.utils.log.Logger;

public final class PurchaseManager {

    public static final int REQUEST_CODE = 5435;

    private static final int BILLING_RESPONSE_CODE_OK = 0;
    private static final int API_VERSION = 3;

    /**
     * Apparently, this has to be the same across all future sessions to recover this information, so we
     * can't use a random value (unless we build in server-side logic). Adding a hard-coded value instead,
     * since anyone can just download the source for this app anyway
     */
    private static final String HARDCODED_DEVELOPER_PAYLOAD = "1234567890";

    private final Context mContext;
    private final PurchaseWallet purchaseWallet;
    private final Analytics mAnalyticsManager;
    private final ServiceConnection mServiceConnection;
    private final ExecutorService mExecutorService;
    private final CopyOnWriteArrayList<SubscriptionEventsListener> mListeners;
    private final String mSessionDeveloperPayload;
    private final Queue<Runnable> mTaskQueue = new LinkedList<>();
    private final Object mQueueLock = new Object();
    private volatile IInAppBillingService mService;
    private volatile PurchaseSource mPurchaseSource;

    public PurchaseManager(@NonNull Context context, @NonNull PurchaseWallet purchaseWallet, @NonNull Analytics analytics) {
        this(context, purchaseWallet, analytics, Executors.newSingleThreadExecutor());
    }

    public PurchaseManager(@NonNull Context context, @NonNull PurchaseWallet purchaseWallet, @NonNull Analytics analytics, @NonNull ExecutorService backgroundTasksExecutor) {
        mContext = context.getApplicationContext();
        this.purchaseWallet = purchaseWallet;
        mAnalyticsManager = analytics;
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                synchronized (mQueueLock) {
                    mService = IInAppBillingService.Stub.asInterface(service);
                    for (final Runnable task : mTaskQueue) {
                        mExecutorService.execute(task);
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                synchronized (mQueueLock) {
                    mService = null;
                    mTaskQueue.clear();
                }
            }
        };

        mExecutorService = backgroundTasksExecutor;
        mListeners = new CopyOnWriteArrayList<>();
        mSessionDeveloperPayload = HARDCODED_DEVELOPER_PAYLOAD;
    }

    /**
     * Adds an event listener to our stack in order to start receiving callbacks
     *
     * @param listener the listener to register
     * @return {@code true} if it was successfully registered. {@code false} otherwise
     */
    public boolean addEventListener(@NonNull SubscriptionEventsListener listener) {
        return mListeners.add(listener);
    }

    /**
     * Removes an event listener from our stack in order to stop receiving callbacks
     *
     * @param listener the listener to unregister
     * @return {@code true} if it was successfully unregistered. {@code false} otherwise
     */
    public boolean removeEventListener(@NonNull SubscriptionEventsListener listener) {
        return mListeners.remove(listener);
    }

    public void onCreate() {
        final Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        mContext.bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void onDestroy() {
        synchronized (mQueueLock) {
            mTaskQueue.clear();
        }
        if (mService != null) {
            mContext.unbindService(mServiceConnection);
        }
        mExecutorService.shutdown();
    }

    public void queryBuyIntent(@NonNull final InAppPurchase inAppPurchase, @NonNull final PurchaseSource purchaseSource) {
        Logger.info(PurchaseManager.this, "Initiating purchase of {} from {}.", inAppPurchase, purchaseSource);
        mAnalyticsManager.record(new DefaultDataPointEvent(Events.Purchases.ShowPurchaseIntent).addDataPoint(new DataPoint("sku", inAppPurchase.getSku())).addDataPoint(new DataPoint("source", purchaseSource)));

        this.queueOrExecuteTask(new Runnable() {
            @Override
            public void run() {
                try {
                    final String developerPayload = mSessionDeveloperPayload;
                    final IInAppBillingService service = mService;
                    if (service == null) {
                        Logger.error(PurchaseManager.this, "Failed to purchase subscription due to unbound service");
                        for (final SubscriptionEventsListener listener : mListeners) {
                            listener.onPurchaseIntentUnavailable(inAppPurchase);
                        }
                        return;
                    }

                    final Bundle buyIntentBundle = service.getBuyIntent(API_VERSION, mContext.getPackageName(), inAppPurchase.getSku(), inAppPurchase.getProductType(), developerPayload);
                    final PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    if (buyIntentBundle.getInt("RESPONSE_CODE") == BILLING_RESPONSE_CODE_OK && pendingIntent != null) {
                        mPurchaseSource = purchaseSource;
                        for (final SubscriptionEventsListener listener : mListeners) {
                            listener.onPurchaseIntentAvailable(inAppPurchase, pendingIntent, developerPayload);
                        }
                    } else {
                        Logger.warn(PurchaseManager.this, "Received an unexpected response code for the buy intent.");
                        for (final SubscriptionEventsListener listener : mListeners) {
                            listener.onPurchaseIntentUnavailable(inAppPurchase);
                        }
                    }
                } catch (RemoteException e) {
                    Logger.error(PurchaseManager.this, "Failed to get buy intent", e);
                    for (final SubscriptionEventsListener listener : mListeners) {
                        listener.onPurchaseIntentUnavailable(inAppPurchase);
                    }
                }
            }
        });
    }

    public void sendMockPurchaseRequest(@NonNull InAppPurchase inAppPurchase) {
        try {
            final Intent data = new Intent();
            final JSONObject json = new JSONObject();
            json.put("developerPayload", mSessionDeveloperPayload);
            json.put("productId", inAppPurchase.getSku());

            data.putExtra("RESPONSE_CODE", BILLING_RESPONSE_CODE_OK);
            data.putExtra("INAPP_PURCHASE_DATA", json.toString());
            onActivityResult(REQUEST_CODE, Activity.RESULT_OK, data);
        } catch (JSONException e) {
            Logger.error(PurchaseManager.this, e.toString());
        }
    }

    /**
     * @return {@code true} if we handled the request. {@code false} otherwise
     */
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        final PurchaseSource purchaseSource = mPurchaseSource != null ? mPurchaseSource : PurchaseSource.Unknown;
        mPurchaseSource = null;
        if (data == null) {
            return false;
        }
        if (requestCode == REQUEST_CODE) {
            final int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            final String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            // TODO: Check signature as well
            final String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
            if (resultCode == Activity.RESULT_OK && responseCode == BILLING_RESPONSE_CODE_OK) {
                try {
                    final JSONObject json = new JSONObject(purchaseData);
                    final String actualDeveloperPayload = json.getString("developerPayload");
                    if (mSessionDeveloperPayload.equals(actualDeveloperPayload)) {
                        final String sku = json.getString("productId");
                        final InAppPurchase inAppPurchase = InAppPurchase.from(sku);
                        if (inAppPurchase != null) {
                            purchaseWallet.addPurchaseToWallet(inAppPurchase);
                            for (final SubscriptionEventsListener listener : mListeners) {
                                listener.onPurchaseSuccess(inAppPurchase, purchaseSource, purchaseWallet);
                            }
                        } else {
                            for (final SubscriptionEventsListener listener : mListeners) {
                                listener.onPurchaseFailed(mPurchaseSource);
                            }
                            Logger.warn(PurchaseManager.this, "Retrieved an unknown subscription code following a successful purchase: " + sku);
                        }
                    }
                } catch (JSONException e) {
                    Logger.error(PurchaseManager.this, "Failed to find purchase information", e);
                    for (final SubscriptionEventsListener listener : mListeners) {
                        listener.onPurchaseFailed(purchaseSource);
                    }
                }
            } else {
                Logger.warn(PurchaseManager.this, "Unexpected {resultCode, responseCode} pair: {" + resultCode + ", " + responseCode + "}");
                for (final SubscriptionEventsListener listener : mListeners) {
                    listener.onPurchaseFailed(purchaseSource);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void querySubscriptions() {
        this.queueOrExecuteTask(new Runnable() {
            @Override
            public void run() {
                try {
                    // First, let's double check that we're bound
                    final IInAppBillingService service = mService;
                    if (service == null) {
                        Logger.error(PurchaseManager.this, "Failed to query subscriptions due to unbound service");
                        for (final SubscriptionEventsListener listener : mListeners) {
                            listener.onPurchasesUnavailable();
                        }
                        return;
                    }

                    // Next, let's query what we already own...
                    final Bundle ownedItems = service.getPurchases(API_VERSION, mContext.getPackageName(), Subscription.GOOGLE_PRODUCT_TYPE, null);
                    if (ownedItems.getInt("RESPONSE_CODE") == BILLING_RESPONSE_CODE_OK) {
                        final ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        final ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        final ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                        final List<InAppPurchase> ownedInAppPurchases = new ArrayList<>();
                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            final String purchaseData = purchaseDataList.get(i);

                            // TODO: Check signature
                            final String signature = signatureList.get(i);
                            final String sku = ownedSkus.get(i);
                            final InAppPurchase ownedInAppPurchase = InAppPurchase.from(sku);
                            if (ownedInAppPurchase != null) {
                                ownedInAppPurchases.add(ownedInAppPurchase);
                            } else {
                                Logger.warn(PurchaseManager.this, "Unknown sku returned from the owned subscriptions query: " + sku);
                            }
                        }

                        // Now that we successfully got everything, let's save it
                        purchaseWallet.updatePurchasesInWallet(ownedInAppPurchases);
                    } else {
                        Logger.error(PurchaseManager.this, "Failed to get the user's owned skus");
                        for (final SubscriptionEventsListener listener : mListeners) {
                            listener.onPurchasesUnavailable();
                        }
                        return;
                    }

                    // Next, let's figure out what is available for purchase
                    final List<InAppPurchase> availablePurchases = new ArrayList<>();
                    final Bundle subscriptionsQueryBundle = new Bundle();
                    subscriptionsQueryBundle.putStringArrayList("ITEM_ID_LIST", InAppPurchase.getSubscriptionSkus());
                    final Bundle skuDetails = mService.getSkuDetails(3, mContext.getPackageName(), Subscription.GOOGLE_PRODUCT_TYPE, subscriptionsQueryBundle);
                    if (skuDetails.getInt("RESPONSE_CODE") == BILLING_RESPONSE_CODE_OK) {
                        final ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                        for (final String response : responseList) {
                            try {
                                final JSONObject object = new JSONObject(response);
                                final String sku = object.getString("productId");
                                final InAppPurchase inAppPurchase = InAppPurchase.from(sku);
                                if (inAppPurchase != null && !PurchaseManager.this.purchaseWallet.hasActivePurchase(inAppPurchase)) {
                                    availablePurchases.add(inAppPurchase);
                                } else {
                                    Logger.warn(PurchaseManager.this, "Unknown or already owned sku returned from the available subscriptions query: {}.", sku);
                                }
                            } catch (JSONException e) {
                                Logger.error(PurchaseManager.this, "Failed to parse JSON about available skus for purchase", e);
                            }
                        }
                    } else {
                        Logger.error(PurchaseManager.this, "Failed to get available skus for purchase");
                        for (final SubscriptionEventsListener listener : mListeners) {
                            listener.onPurchasesUnavailable();
                        }
                        return;
                    }

                    // Lastly, pass this info back to our listeners
                    for (final SubscriptionEventsListener listener : mListeners) {
                        listener.onPurchasesAvailable(availablePurchases);
                    }
                } catch (RemoteException e) {
                    Logger.error(PurchaseManager.this, "Failed to get available skus for purchase", e);
                    for (final SubscriptionEventsListener listener : mListeners) {
                        listener.onPurchasesUnavailable();
                    }
                }
            }
        });
    }

    @NonNull
    public PurchaseWallet getPurchaseWallet() {
        return purchaseWallet;
    }

    /**
     * Since we do not know exactly when our {@link com.android.vending.billing.IInAppBillingService} will be bound,
     * we can use this utility method to queue up any tasks that we need until the binding completes. Once bound, all
     * queued tasks will be executed.
     *
     * @param task the {@link java.lang.Runnable} to either queue or execute immediately (if we're bound)
     */
    private void queueOrExecuteTask(@NonNull Runnable task) {
        synchronized (mQueueLock) {
            if (mService == null) {
                mTaskQueue.add(task);
            } else {
                mExecutorService.execute(task);
            }
        }
    }


}
