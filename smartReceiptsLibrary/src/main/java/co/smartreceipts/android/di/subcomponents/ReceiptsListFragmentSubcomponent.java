package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.FragmentScope;
import co.smartreceipts.android.fragments.ReceiptsListFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface ReceiptsListFragmentSubcomponent extends AndroidInjector<ReceiptsListFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ReceiptsListFragment> {

    }
}
