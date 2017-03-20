package wb.receiptspro.di;

import android.content.Context;

import co.smartreceipts.android.ad.AdManager;
import co.smartreceipts.android.ad.NoOpAdManager;
import co.smartreceipts.android.di.BaseAppModule;
import co.smartreceipts.android.di.GlobalBindingModule;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.purchases.wallet.ProPurchaseWallet;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import wb.receiptspro.SmartReceiptsProApplication;

@ApplicationScope
@Component(modules = {
        ProAppComponent.ProAppModule.class,
        GlobalBindingModule.class,
        BaseAppModule.class
})
public interface ProAppComponent {

    SmartReceiptsProApplication inject(SmartReceiptsProApplication application);

    @Module
    class ProAppModule {
        private final SmartReceiptsProApplication application;

        public ProAppModule(SmartReceiptsProApplication application) {
            this.application = application;
        }

        @Provides
        @ApplicationScope
        Context provideContext() {
            return application;
        }

        @Provides
        @ApplicationScope
        public static AdManager provideAdManager(NoOpAdManager noOpAdManager) {
            return noOpAdManager;
        }

        @Provides
        public static PurchaseWallet providePurchaseWallet (ProPurchaseWallet proPurchaseWallet) {
            return proPurchaseWallet;
        }
    }
}
