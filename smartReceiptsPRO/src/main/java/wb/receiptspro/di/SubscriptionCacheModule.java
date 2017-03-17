package wb.receiptspro.di;

import co.smartreceipts.android.purchases.ProSubscriptionCache;
import co.smartreceipts.android.purchases.SubscriptionCache;
import dagger.Module;
import dagger.Provides;

@Module
public class SubscriptionCacheModule {
    @Provides
    public static SubscriptionCache provideSubscriptionCache (ProSubscriptionCache proSubscriptionCache) {
        return proSubscriptionCache;
    }
}
