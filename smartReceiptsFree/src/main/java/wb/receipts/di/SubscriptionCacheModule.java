package wb.receipts.di;

import co.smartreceipts.android.purchases.DefaultSubscriptionCache;
import co.smartreceipts.android.purchases.SubscriptionCache;
import dagger.Module;
import dagger.Provides;

@Module
public class SubscriptionCacheModule {
    @Provides
    public static SubscriptionCache provideSubscriptionCache (DefaultSubscriptionCache defaultSubscriptionCache) {
        return defaultSubscriptionCache;
    }
}
