package wb.receipts.di;

import javax.inject.Singleton;

import co.smartreceipts.android.ad.AdManager;
import dagger.Module;
import dagger.Provides;
import wb.receipts.ad.FreeAdManager;

@Module
public class AdModule {
    @Provides
    @Singleton
    public static AdManager provideAdManager(FreeAdManager freeAdManager) {
        return freeAdManager;
    }
}
