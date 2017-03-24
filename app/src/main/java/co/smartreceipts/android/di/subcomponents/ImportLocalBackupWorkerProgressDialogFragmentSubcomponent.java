package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.widget.backups.ImportLocalBackupWorkerProgressDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;


@FragmentScope
@Subcomponent
public interface ImportLocalBackupWorkerProgressDialogFragmentSubcomponent
        extends AndroidInjector<ImportLocalBackupWorkerProgressDialogFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ImportLocalBackupWorkerProgressDialogFragment> {

    }
}
