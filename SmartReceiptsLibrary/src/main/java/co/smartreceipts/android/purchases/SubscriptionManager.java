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

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wb.android.google.camera.data.Log;

public final class SubscriptionManager {

    public static final int REQUEST_CODE = 5435;

    private static final String TAG = SubscriptionManager.class.getSimpleName();
    private static final int BILLING_RESPONSE_CODE_OK = 0;
    private static final int API_VERSION = 3;

    private final Context mContext;
    private final SubscriptionCache mSubscriptionCache;
    private final ServiceConnection mServiceConnection;
    private final ExecutorService mExecutorService;
    private final CopyOnWriteArrayList<SubscriptionEventsListener> mListeners;
    private final String mSessionDeveloperPayload;
    private IInAppBillingService mService;

    public SubscriptionManager(@NonNull Context context, @NonNull SubscriptionCache subscriptionCache) {
        this(context, subscriptionCache, Executors.newSingleThreadExecutor());
    }

    public SubscriptionManager(@NonNull Context context, @NonNull SubscriptionCache subscriptionCache, @NonNull ExecutorService backgroundTasksExecutor) {
        mContext = context.getApplicationContext();
        mSubscriptionCache = subscriptionCache;
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }
        };

        // Note: this isn't actually secure at all... but it's okay given that the Pro version is open source anyway
        final byte[] payloadBytes = new byte[32];
        new Random().nextBytes(payloadBytes);
        mSessionDeveloperPayload = new String(payloadBytes);

        mExecutorService = backgroundTasksExecutor;
        mListeners = new CopyOnWriteArrayList<>();
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
        if (mService != null) {
            mContext.unbindService(mServiceConnection);
        }
        mExecutorService.shutdown();
    }

    public void queryBuyIntent(@NonNull final Subscription subscription) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // TODO: Developer payload should be a randomly generated string
                    final String developerPayload = mSessionDeveloperPayload;
                    final Bundle buyIntentBundle = mService.getBuyIntent(3, mContext.getPackageName(), subscription.getSku(), "subs", developerPayload);
                    final PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    if (buyIntentBundle.getInt("RESPONSE_CODE") == BILLING_RESPONSE_CODE_OK) {
                        for (final SubscriptionEventsListener listener : mListeners) {
                            listener.onPurchaseIntentAvailable(subscription, pendingIntent, developerPayload);
                        }
                    }
                    else {
                        Log.w(TAG, "Received an unexpected response code for the buy intent.");
                        for (final SubscriptionEventsListener listener : mListeners) {
                            listener.onPurchaseIntentUnavailable(subscription);
                        }
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to get buy intent", e);
                    for (final SubscriptionEventsListener listener : mListeners) {
                        listener.onPurchaseIntentUnavailable(subscription);
                    }
                }
            }
        });
    }

    /**
     * @return {@code true} if we handled the request. {@code false} otherwise
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data, String expectedDeveloperPayload) {
        if (requestCode == REQUEST_CODE) {
            final int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            final String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            // TODO: Check signature as well
            final String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
            if (resultCode == Activity.RESULT_OK && responseCode == BILLING_RESPONSE_CODE_OK) {
                try {
                    final JSONObject json = new JSONObject(purchaseData);
                    final String actualDeveloperPayload = json.getString("developerPayload");
                    if (expectedDeveloperPayload.equals(actualDeveloperPayload)) {
                        final String sku = json.getString("productId");
                        final Subscription subscription = Subscription.from(sku);
                        if (subscription != null) {
                            for (final SubscriptionEventsListener listener : mListeners) {
                                listener.onPurchaseSuccess(subscription, mSubscriptionCache.getSubscriptionWallet());
                            }
                        } else {
                            for (final SubscriptionEventsListener listener : mListeners) {
                                listener.onPurchaseFailed();
                            }
                            Log.w(TAG, "Retrieved an unknown subscription code following a successful purchase: " + sku);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to find purchase information", e);
                    for (final SubscriptionEventsListener listener : mListeners) {
                        listener.onPurchaseFailed();
                    }
                }
            } else {
                Log.w(TAG, "Unexpected {resultCode, responseCode} pair: {" + resultCode + ", " + responseCode + "}");
                for (final SubscriptionEventsListener listener : mListeners) {
                    listener.onPurchaseFailed();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void querySubscriptions() {
        final Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", Subscription.getSkus());

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // First, let's query what we already own...
                    final List<Subscription> ownedSubscriptions = new ArrayList<>();
                    final Bundle ownedItems = mService.getPurchases(API_VERSION, mContext.getPackageName(), "subs", null);
                    if (ownedItems.getInt("RESPONSE_CODE") == BILLING_RESPONSE_CODE_OK) {
                        final ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        final ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        final ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            final String purchaseData = purchaseDataList.get(i);

                            // TODO: Check signature
                            final String signature = signatureList.get(i);
                            final String sku = ownedSkus.get(i);
                            final Subscription ownedSubscription = Subscription.from(sku);
                            if (ownedSubscription != null) {
                                ownedSubscriptions.add(ownedSubscription);
                            } else {
                                Log.w(TAG, "Unknown sku returned from the owned subscriptions query: " + sku);
                            }
                        }
                    }
                    mSubscriptionCache.addSubscriptionsToWallet(ownedSubscriptions);
                    final SubscriptionWallet subscriptionWallet = mSubscriptionCache.getSubscriptionWallet();

                    // Next, let's figure out what is available for purchase
                    final List<PurchaseableSubscription> availableSubscriptions = new ArrayList<>();
                    final Bundle skuDetails = mService.getSkuDetails(3, mContext.getPackageName(), "subs", querySkus);
                    if (skuDetails.getInt("RESPONSE_CODE") == BILLING_RESPONSE_CODE_OK) {
                        final ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                        for (final String thisResponse : responseList) {
                            try {
                                final JSONObject object = new JSONObject(thisResponse);
                                final String sku = object.getString("productId");
                                final String price = object.getString("price");
                                final Subscription subscription = Subscription.from(sku);
                                if (subscription != null && !mSubscriptionCache.getSubscriptionWallet().hasSubscription(subscription)) {
                                    availableSubscriptions.add(new PurchaseableSubscription(subscription, price));
                                } else {
                                    Log.w(TAG, "Unknown sku returned from the available subscriptions query: " + sku);
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed to parse JSON about available skus for purchase", e);
                            }
                        }
                    }

                    // Lastly, pass this info back to our listeners
                    for (final SubscriptionEventsListener listener : mListeners) {
                        listener.onSubscriptionsAvailable(availableSubscriptions, subscriptionWallet);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Failed to get available skus for purchase", e);
                    for (final SubscriptionEventsListener listener : mListeners) {
                        listener.onSubscriptionsUnavailable();
                    }
                }
            }
        });
    }

}
