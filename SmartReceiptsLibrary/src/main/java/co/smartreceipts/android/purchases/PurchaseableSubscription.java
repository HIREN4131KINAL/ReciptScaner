package co.smartreceipts.android.purchases;

import android.support.annotation.NonNull;

public class PurchaseableSubscription {

    private final Subscription mSubscription;
    private final String mPriceString;

    public PurchaseableSubscription(@NonNull Subscription subscription, @NonNull String priceString) {
        mSubscription = subscription;
        mPriceString = priceString;
    }

    @NonNull
    public Subscription getSubscription() {
        return mSubscription;
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
        if (mSubscription != that.mSubscription) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mSubscription.hashCode();
        result = 31 * result + mPriceString.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PurchaseableSubscription{" +
                "mSubscription=" + mSubscription +
                ", mPriceString='" + mPriceString + '\'' +
                '}';
    }
}
