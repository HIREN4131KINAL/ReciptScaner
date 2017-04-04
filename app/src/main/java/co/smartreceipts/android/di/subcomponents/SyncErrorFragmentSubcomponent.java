package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.widget.errors.SyncErrorFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface SyncErrorFragmentSubcomponent extends AndroidInjector<SyncErrorFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SyncErrorFragment> {

    }
}
