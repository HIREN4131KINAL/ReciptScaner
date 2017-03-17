package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.di.ActivityScope;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScope
@Subcomponent
public interface SmartReceiptsActivitySubcomponent extends AndroidInjector<SmartReceiptsActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SmartReceiptsActivity> {
    }
}
