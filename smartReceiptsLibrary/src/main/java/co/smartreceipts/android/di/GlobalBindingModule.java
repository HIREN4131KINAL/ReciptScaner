package co.smartreceipts.android.di;

import android.app.Activity;

import co.smartreceipts.android.activities.SmartReceiptsActivity;
import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(
        subcomponents = {
                SmartReceiptsActivitySubcomponent.class
        }
)
public abstract class GlobalBindingModule {
    @Binds
    @IntoMap
    @ActivityKey(SmartReceiptsActivity.class)
    public abstract AndroidInjector.Factory<? extends Activity> smartReceiptsActivitySubcomponentBuiler(
            SmartReceiptsActivitySubcomponent.Builder builder);
}
