package co.smartreceipts.android.di;

import co.smartreceipts.android.ExtraInitializer;
import co.smartreceipts.android.ExtraInitializerPlusImpl;
import co.smartreceipts.android.ad.AdManager;
import co.smartreceipts.android.ad.NoOpAdManager;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.purchases.wallet.PlusPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    @ApplicationScope
    public static AdManager provideAdManager(NoOpAdManager noOpAdManager) {
        return noOpAdManager;
    }

    @Provides
    @ApplicationScope
    public static PurchaseWallet providePurchaseWallet (PlusPurchaseWallet plusPurchaseWallet) {
        return plusPurchaseWallet;
    }

    @Provides
    @ApplicationScope
    public static ExtraInitializer provideExtraInitializer (ExtraInitializerPlusImpl plusInitializer) {
        return plusInitializer;
    }
}
