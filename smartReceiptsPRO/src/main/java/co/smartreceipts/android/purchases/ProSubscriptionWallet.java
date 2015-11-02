package co.smartreceipts.android.purchases;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.Collection;

public final class ProSubscriptionWallet implements SubscriptionWallet {

    @NonNull
    @Override
    public Collection<Subscription> getOwnedSubscriptions() {
        return Arrays.asList(Subscription.values());
    }

    @Override
    public boolean hasSubscription(@NonNull Subscription subscription) {
        return true;
    }
}
