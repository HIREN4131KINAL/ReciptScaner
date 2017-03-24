package co.smartreceipts.android.ad;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.smartreceipts.android.purchases.PurchaseManager;

public interface AdManager {
    void onActivityCreated(@NonNull Activity activity, @Nullable PurchaseManager purchaseManager);

    void onResume();

    void onPause();

    void onDestroy();
}
