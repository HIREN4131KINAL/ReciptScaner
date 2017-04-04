package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.fragments.SelectAutomaticBackupProviderDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface SelectAutomaticBackupProviderDialogFragmentSubcomponent extends AndroidInjector<SelectAutomaticBackupProviderDialogFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SelectAutomaticBackupProviderDialogFragment> {

    }
}
