package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.widget.backups.DownloadRemoteBackupImagesProgressDialogFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;


@FragmentScope
@Subcomponent
public interface DownloadRemoteBackupImagesProgressDialogFragmentSubcomponent
        extends AndroidInjector<DownloadRemoteBackupImagesProgressDialogFragment> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<DownloadRemoteBackupImagesProgressDialogFragment> {

    }
}
