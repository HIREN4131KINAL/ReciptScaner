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
    public AdManager provideAdManager(FreeAdManager freeAdManager) {
        return freeAdManager;
    }
}
