package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.fragments.ReceiptImageFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface ReceiptImageFragmentSubcomponent extends AndroidInjector<ReceiptImageFragment>{
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ReceiptImageFragment> {
    }
}
