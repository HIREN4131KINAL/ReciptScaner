package co.smartreceipts.android.ad;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.smartreceipts.android.purchases.SubscriptionManager;

public interface AdManager {
    void onActivityCreated(@NonNull Activity activity, @Nullable SubscriptionManager subscriptionManager);

    void onResume();

    void onPause();

    void onDestroy();
}
