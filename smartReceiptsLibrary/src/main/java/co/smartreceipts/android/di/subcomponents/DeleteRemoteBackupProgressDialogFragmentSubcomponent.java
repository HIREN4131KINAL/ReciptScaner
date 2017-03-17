package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.FragmentScope;
import co.smartreceipts.android.sync.widget.backups.DeleteRemoteBackupProgressDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface DeleteRemoteBackupProgressDialogFragmentSubcomponent
        extends AndroidInjector<DeleteRemoteBackupProgressDialogFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<DeleteRemoteBackupProgressDialogFragment> {

    }
}
