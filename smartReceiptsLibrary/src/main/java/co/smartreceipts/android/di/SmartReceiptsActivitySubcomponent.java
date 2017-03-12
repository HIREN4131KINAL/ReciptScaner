package co.smartreceipts.android.di;

import co.smartreceipts.android.activities.SmartReceiptsActivity;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScope
@Subcomponent(modules = SmartReceiptsActivitySubcomponent.SmartReceiptsActivityModule.class)
public interface SmartReceiptsActivitySubcomponent extends AndroidInjector<SmartReceiptsActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SmartReceiptsActivity> {
    }

    @Module
    class SmartReceiptsActivityModule {

    }
}
