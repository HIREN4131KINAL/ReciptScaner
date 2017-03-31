package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.widget.backups.DeleteRemoteBackupDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface DeleteRemoteBackupDialogFragmentSubcomponent extends AndroidInjector<DeleteRemoteBackupDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<DeleteRemoteBackupDialogFragment> {

    }
}
