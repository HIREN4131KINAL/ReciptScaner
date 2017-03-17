package wb.receipts.di;

import android.content.Context;

import javax.inject.Singleton;

import co.smartreceipts.android.di.DataModule;
import co.smartreceipts.android.di.GlobalBindingModule;
import co.smartreceipts.android.di.PresentationModule;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import wb.receipts.SmartReceiptsFreeApplication;

@Singleton
@Component (modules = {
        AppComponent.AppModule.class,
        AdModule.class,
        GlobalBindingModule.class,
        PresentationModule.class,
        DataModule.class,
        SubscriptionCacheModule.class
})
public interface AppComponent {

    SmartReceiptsFreeApplication inject(SmartReceiptsFreeApplication application);

    @Module
    class AppModule {
        private final SmartReceiptsFreeApplication application;

        public AppModule(SmartReceiptsFreeApplication application) {
            this.application = application;
        }

        @Provides
        @Singleton
        Context provideContext() {
            return application;
        }
    }
}
