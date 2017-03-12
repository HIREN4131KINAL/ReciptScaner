package wb.receiptspro.di;

import javax.inject.Singleton;

import co.smartreceipts.android.ad.AdManager;
import co.smartreceipts.android.ad.NoOpAdManager;
import dagger.Module;
import dagger.Provides;

@Module
public class AdModule {
    @Provides
    @Singleton
    public static AdManager provideAdManager(NoOpAdManager noOpAdManager) {
        return noOpAdManager;
    }
}
