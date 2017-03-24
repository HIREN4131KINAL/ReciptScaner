package co.smartreceipts.android.di;

import co.smartreceipts.android.ExtraInitializer;
import co.smartreceipts.android.ExtraInitializerFreeImpl;
import co.smartreceipts.android.ad.AdManager;
import co.smartreceipts.android.ad.FreeAdManager;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.purchases.wallet.DefaultPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    @ApplicationScope
    public static AdManager provideAdManager(FreeAdManager freeAdManager) {
        return freeAdManager;
    }

    @Provides
    @ApplicationScope
    public static PurchaseWallet providePurchaseWallet (DefaultPurchaseWallet defaultPurchaseWallet) {
        return defaultPurchaseWallet;
    }

    @Provides
    @ApplicationScope
    public static ExtraInitializer provideExtraInitializer (ExtraInitializerFreeImpl freeInitializer) {
        return freeInitializer;
    }
}
