package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.widget.backups.ExportBackupWorkerProgressDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;


@FragmentScope
@Subcomponent
public interface ExportBackupWorkerProgressDialogFragmentSubcomponent
        extends AndroidInjector<ExportBackupWorkerProgressDialogFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ExportBackupWorkerProgressDialogFragment> {

    }
}
