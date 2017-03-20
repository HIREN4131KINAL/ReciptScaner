package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.widget.backups.BackupsFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@dagger.Subcomponent
public interface BackupsFragmentSubcomponent extends AndroidInjector<BackupsFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<BackupsFragment> {

    }
}
