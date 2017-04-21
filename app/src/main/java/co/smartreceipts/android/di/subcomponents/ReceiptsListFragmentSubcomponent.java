package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.receipts.ReceiptsListFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface ReceiptsListFragmentSubcomponent extends AndroidInjector<ReceiptsListFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ReceiptsListFragment> {

    }
}
