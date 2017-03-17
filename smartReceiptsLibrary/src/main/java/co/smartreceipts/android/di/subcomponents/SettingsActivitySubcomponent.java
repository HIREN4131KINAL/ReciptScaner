package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.ActivityScope;
import co.smartreceipts.android.settings.widget.SettingsActivity;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScope
@Subcomponent
public interface SettingsActivitySubcomponent extends AndroidInjector<SettingsActivity> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SettingsActivity> {

    }
}
