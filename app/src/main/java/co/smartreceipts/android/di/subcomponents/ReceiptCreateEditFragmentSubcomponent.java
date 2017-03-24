package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.fragments.ReceiptCreateEditFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface ReceiptCreateEditFragmentSubcomponent extends AndroidInjector<ReceiptCreateEditFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ReceiptCreateEditFragment> {
    }
}
