package co.smartreceipts.android.sync.drive.services.di;

import co.smartreceipts.android.di.scopes.ServiceScope;
import co.smartreceipts.android.sync.drive.services.DriveCompletionEventService;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ServiceScope
@Subcomponent
public interface DriveCompletionEventServiceSubcomponent extends AndroidInjector<DriveCompletionEventService> {

    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<DriveCompletionEventService> {
    }
}
