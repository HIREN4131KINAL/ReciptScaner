package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.widget.backups.ImportRemoteBackupWorkerProgressDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent
public interface ImportRemoteBackupWorkerProgressDialogFragmentSubcomponent
        extends AndroidInjector<ImportRemoteBackupWorkerProgressDialogFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ImportRemoteBackupWorkerProgressDialogFragment> {

    }
}
