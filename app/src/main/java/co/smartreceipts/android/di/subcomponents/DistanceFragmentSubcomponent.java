package co.smartreceipts.android.di.subcomponents;


import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.fragments.DistanceFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface DistanceFragmentSubcomponent extends AndroidInjector<DistanceFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<DistanceFragment> {
    }
}
