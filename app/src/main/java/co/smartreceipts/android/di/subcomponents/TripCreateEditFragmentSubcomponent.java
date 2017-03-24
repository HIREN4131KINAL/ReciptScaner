package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.trips.editor.TripCreateEditFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface TripCreateEditFragmentSubcomponent extends AndroidInjector<TripCreateEditFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<TripCreateEditFragment> {
    }
}
