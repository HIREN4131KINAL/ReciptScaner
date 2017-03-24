package co.smartreceipts.android.di;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import dagger.Component;

@ApplicationScope
@Component(modules = {
        AppModule.class,
        GlobalBindingModule.class,
        BaseAppModule.class
})
public interface AppComponent {

    SmartReceiptsApplication inject(SmartReceiptsApplication application);

}
