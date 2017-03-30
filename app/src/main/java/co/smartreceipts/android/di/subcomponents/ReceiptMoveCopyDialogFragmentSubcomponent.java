package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.fragments.ReceiptMoveCopyDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface ReceiptMoveCopyDialogFragmentSubcomponent extends AndroidInjector<ReceiptMoveCopyDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ReceiptMoveCopyDialogFragment> {
    }
}
