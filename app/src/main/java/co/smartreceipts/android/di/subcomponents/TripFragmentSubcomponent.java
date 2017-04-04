package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.trips.TripFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface TripFragmentSubcomponent extends AndroidInjector<TripFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<TripFragment> {
    }
}
