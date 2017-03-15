package co.smartreceipts.android.purchases;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DefaultPurchaseWallet implements PurchaseWallet {

    private final Set<Subscription> mOwnedSubscriptions;

    public DefaultPurchaseWallet(@NonNull Collection<Subscription> ownedSubscriptions) {
        mOwnedSubscriptions = Collections.unmodifiableSet(new HashSet<>(ownedSubscriptions));
    }

    @NonNull
    @Override
    public Collection<Subscription> getOwnedSubscriptions() {
        return mOwnedSubscriptions;
    }

    @Override
    public boolean hasSubscription(@NonNull Subscription subscription) {
        return mOwnedSubscriptions.contains(subscription);
    }
}
