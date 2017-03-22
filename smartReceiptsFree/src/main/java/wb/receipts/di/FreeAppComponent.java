package wb.receipts.di;

import android.content.Context;

import co.smartreceipts.android.ad.AdManager;
import co.smartreceipts.android.di.BaseAppModule;
import co.smartreceipts.android.di.GlobalBindingModule;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.purchases.wallet.DefaultPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import wb.receipts.SmartReceiptsFreeApplication;
import wb.receipts.ad.FreeAdManager;

// TODO: 20.03.2017 later, with flawors, there will be just one AppComponent, which will use Free/Pro AppModule

@ApplicationScope
@Component (modules = {
        FreeAppComponent.FreeAppModule.class,
        GlobalBindingModule.class,
        BaseAppModule.class
})
public interface FreeAppComponent {

    SmartReceiptsFreeApplication inject(SmartReceiptsFreeApplication application);

    @Module
    class FreeAppModule {
        private final SmartReceiptsFreeApplication application;

        public FreeAppModule(SmartReceiptsFreeApplication application) {
            this.application = application;
        }

        @Provides
        @ApplicationScope
        Context provideContext() {
            return application;
        }

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
    }
}
