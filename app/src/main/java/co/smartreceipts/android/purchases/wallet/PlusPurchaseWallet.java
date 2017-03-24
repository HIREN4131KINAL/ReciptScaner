package co.smartreceipts.android.purchases.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import javax.inject.Inject;

import co.smartreceipts.android.purchases.model.InAppPurchase;

public final class PlusPurchaseWallet extends DefaultPurchaseWallet {

    @Inject
    public PlusPurchaseWallet(@NonNull Context context) {
        super(context);
    }

    @VisibleForTesting
    protected PlusPurchaseWallet(@NonNull SharedPreferences preferences) {
        super(preferences);
    }

    @Override
    public boolean hasActivePurchase(@NonNull InAppPurchase inAppPurchase) {
        if (inAppPurchase == InAppPurchase.SmartReceiptsPlus) {
            return true;
        } else {
            return super.hasActivePurchase(inAppPurchase);
        }
    }

}
