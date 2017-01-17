package co.smartreceipts.android.sync.drive.services;

import android.support.annotation.NonNull;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.events.CompletionEvent;
import com.google.android.gms.drive.events.DriveEventService;
import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import co.smartreceipts.android.utils.log.Logger;

public class DriveCompletionEventService extends DriveEventService {

    private static final Object sLock = new Object();
    private static final Map<DriveId, DriveIdUploadCompleteCallback> sDriveIdDriveIdCallbackMap = new HashMap<>();
    private static final Map<DriveId, Boolean> sUnhandledEvents = new HashMap<>();

    @Override
    public void onCompletion(CompletionEvent event) {
        final DriveIdUploadCompleteCallback callback;
        synchronized (sLock) {
           callback = sDriveIdDriveIdCallbackMap.remove(event.getDriveId());
            if (callback == null) {
                sUnhandledEvents.put(event.getDriveId(), event.getStatus() == CompletionEvent.STATUS_SUCCESS);
            }
        }

        if (callback != null) {
            if (event.getStatus() == CompletionEvent.STATUS_SUCCESS) {
                Logger.info(DriveCompletionEventService.class, "Calling back with persisted drive resource id: {}", event.getDriveId().getResourceId());
                callback.onSuccess(event.getDriveId());
            } else {
                Logger.error(DriveCompletionEventService.class, "Calling back drive resource id failure");
                callback.onFailure(event.getDriveId());
            }
        } else {
            // Note: doing this outside our lock
            Logger.warn(DriveCompletionEventService.class, "Received an event before a callback was registered. Saving for later");
        }

        event.dismiss();
    }

    /**
     * Google unfortunately didn't make things very flexible here, so we're pretty limited in our design choices.
     * Making this package protected to reduce the exposure of this behavior
     * @param driveId
     * @param callback
     */
    static void registerCallback(@NonNull DriveId driveId, @NonNull DriveIdUploadCompleteCallback callback) {
        Preconditions.checkNotNull(driveId);
        Preconditions.checkNotNull(callback);

        Logger.info(DriveCompletionEventService.class, "Registering for completion of id: {}", driveId.getResourceId());

        // This Boolean is tri-state. null => not used, true => STATUS_SUCCESS, false => STATUS_FAIL
        final Boolean wasIdSuccessfullySaved;

        synchronized (sLock) {
            wasIdSuccessfullySaved = sUnhandledEvents.remove(driveId);
            if (wasIdSuccessfullySaved == null) {
                sDriveIdDriveIdCallbackMap.put(driveId, callback);
            }
        }

        if (wasIdSuccessfullySaved != null) {
            if (wasIdSuccessfullySaved) {
                Logger.info(DriveCompletionEventService.class, "Immediately handling callback for: {}", driveId.getResourceId());
                callback.onSuccess(driveId);
            } else {
                Logger.warn(DriveCompletionEventService.class, "Immediately handling callback failure for: {}", driveId.getResourceId());
                callback.onFailure(driveId);
            }
        }
    }


}
