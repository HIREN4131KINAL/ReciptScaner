package co.smartreceipts.android.sync.drive.services;

import android.support.annotation.NonNull;

import com.google.android.gms.drive.DriveId;

/**
 * Drive's APIs are not RESTful, so we can get the event resource id at any time after an upload completes.
 * Usually if we have network, it's pretty quick. Additionally, Google tied us to using a manifest-registered
 * service for this stuff, so we really have nothing in the means of dynamic controls for this :(...
 * <p>
 * This class services as a more "OO-friendly" wrapper that we can use for unit testing purposes
 * </p>
 */
public class DriveUploadCompleteManager {

    /**
     * Registers a callback to inform us when a specific drive id has a valid resource id, so we can persist
     * this as having fully completed
     *
     * @param driveId - the drive id to watch for
     * @param callback - the callback to inform us when this has occurred
     */
    public void registerCallback(@NonNull DriveId driveId, @NonNull DriveIdUploadCompleteCallback callback) {
        DriveCompletionEventService.registerCallback(driveId, callback);
    }
}
