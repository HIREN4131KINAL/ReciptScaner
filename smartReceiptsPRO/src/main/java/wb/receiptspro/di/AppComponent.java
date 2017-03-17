package wb.receiptspro.di;

import android.content.Context;

import javax.inject.Singleton;

import co.smartreceipts.android.di.DataModule;
import co.smartreceipts.android.di.GlobalBindingModule;
import co.smartreceipts.android.di.PresentationModule;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import wb.receiptspro.SmartReceiptsProApplication;

@Singleton
@Component(modules = {
        AppComponent.AppModule.class,
        AdModule.class,
        GlobalBindingModule.class,
        PresentationModule.class,
        DataModule.class,
        SubscriptionCacheModule.class
})
public interface AppComponent {

    SmartReceiptsProApplication inject(SmartReceiptsProApplication application);

    @Module
    class AppModule {
        private final SmartReceiptsProApplication application;

        public AppModule(SmartReceiptsProApplication application) {
            this.application = application;
        }

        @Provides
        @Singleton
        Context provideContext() {
            return application;
        }
    }
}
