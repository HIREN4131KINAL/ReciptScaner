package co.smartreceipts.android.purchases;

import android.support.annotation.NonNull;

import co.smartreceipts.android.purchases.model.InAppPurchase;

public class PurchaseableSubscription {

    private final InAppPurchase mInAppPurchase;
    private final String mPriceString;

    public PurchaseableSubscription(@NonNull InAppPurchase inAppPurchase, @NonNull String priceString) {
        mInAppPurchase = inAppPurchase;
        mPriceString = priceString;
    }

    @NonNull
    public InAppPurchase getSubscription() {
        return mInAppPurchase;
    }

    @NonNull
    public String getPriceString() {
        return mPriceString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PurchaseableSubscription that = (PurchaseableSubscription) o;

        if (!mPriceString.equals(that.mPriceString)) return false;
        if (mInAppPurchase != that.mInAppPurchase) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mInAppPurchase.hashCode();
        result = 31 * result + mPriceString.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PurchaseableSubscription{" +
                "mSubscription=" + mInAppPurchase +
                ", mPriceString='" + mPriceString + '\'' +
                '}';
    }
}
