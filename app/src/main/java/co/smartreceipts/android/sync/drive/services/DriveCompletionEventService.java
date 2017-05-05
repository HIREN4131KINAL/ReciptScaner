package co.smartreceipts.android.sync.drive.services;

import com.google.android.gms.drive.events.CompletionEvent;
import com.google.android.gms.drive.events.DriveEventService;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ServiceScope;
import dagger.android.AndroidInjection;

@ServiceScope
public class DriveCompletionEventService extends DriveEventService {

    @Inject
    DriveUploadCompleteManager driveUploadCompleteManager;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    public void onCompletion(CompletionEvent event) {
        driveUploadCompleteManager.onCompletion(new DriveCompletionEventWrapper(event));
    }

}
