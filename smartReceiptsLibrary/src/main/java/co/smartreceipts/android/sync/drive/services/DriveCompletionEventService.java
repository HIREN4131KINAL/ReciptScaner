package co.smartreceipts.android.sync.drive.services;

import android.support.annotation.NonNull;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.events.CompletionEvent;
import com.google.android.gms.drive.events.DriveEventService;
import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

public class DriveCompletionEventService extends DriveEventService {

    private static final Map<DriveId, DriveIdUploadCompleteCallback> sDriveIdDriveIdCallbackMap = new HashMap<DriveId, DriveIdUploadCompleteCallback>();

    @Override
    public void onCompletion(CompletionEvent event) {
        final DriveIdUploadCompleteCallback callback = sDriveIdDriveIdCallbackMap.remove(event.getDriveId());
        if (callback != null) {
            if (event.getStatus() == CompletionEvent.STATUS_SUCCESS) {
                callback.onSuccess(event.getDriveId());
            } else {
                callback.onFailure(event.getDriveId());
            }
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
        sDriveIdDriveIdCallbackMap.put(driveId, callback);
    }


}
