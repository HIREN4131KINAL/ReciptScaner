package co.smartreceipts.android.imports;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.google.common.base.Preconditions;

import java.io.File;
import java.lang.ref.WeakReference;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.Preferences;

public class CameraInteractionController {

    private final WeakReference<Fragment> mFragmentReference;
    private final Preferences mPreferences;

    public CameraInteractionController(@NonNull Fragment fragment, @NonNull PersistenceManager persistenceManager) {
        this(fragment, persistenceManager.getPreferences());
    }

    public CameraInteractionController(@NonNull Fragment fragment, @NonNull Preferences preferences) {
        mFragmentReference = new WeakReference<>(Preconditions.checkNotNull(fragment));
        mPreferences = Preconditions.checkNotNull(preferences);
    }

    /**
     * Takes a photo for a given trip directory
     *
     * @param trip the desired {@link Trip}
     * @return the Uri result of the photo
     */
    @NonNull
    public Uri takePhoto(@NonNull Trip trip) {
        return startPhotoIntent(Uri.fromFile(new File(trip.getDirectory(), System.currentTimeMillis() + "x.jpg")), RequestCodes.NATIVE_NEW_RECEIPT_CAMERA_REQUEST, RequestCodes.NEW_RECEIPT_CAMERA_REQUEST);
    }

    /**
     * Takes a photo for a given receipt
     *
     * @param receipt the desired {@link Receipt}
     * @return the Uri result of the photo
     */
    @NonNull
    public Uri addPhoto(@NonNull Receipt receipt) {
        return startPhotoIntent(Uri.fromFile(new File(receipt.getTrip().getDirectory(), System.currentTimeMillis() + "x.jpg")), RequestCodes.NATIVE_ADD_PHOTO_CAMERA_REQUEST, RequestCodes.ADD_PHOTO_CAMERA_REQUEST);
    }

    /**
     * Retakes a photo for a given receipt
     *
     * @param receipt the desired {@link Receipt}
     * @return the Uri result of the photo
     */
    @NonNull
    public Uri retakePhoto(@NonNull Receipt receipt) {
        return startPhotoIntent(Uri.fromFile(receipt.getFile()), RequestCodes.NATIVE_RETAKE_PHOTO_CAMERA_REQUEST, RequestCodes.RETAKE_PHOTO_CAMERA_REQUEST);
    }

    @NonNull
    private Uri startPhotoIntent(@NonNull Uri saveLocation, int nativeCameraRequestCode, int localCameraRequestCode) {
        final Fragment fragment = mFragmentReference.get();
        if (fragment != null && fragment.isResumed()) {
            return Uri.EMPTY;
        }

        final boolean hasCameraPermission = ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        final boolean hasWritePermission = ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (mPreferences.useNativeCamera() || !hasCameraPermission || !hasWritePermission) {
            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, saveLocation);
            fragment.startActivityForResult(intent, nativeCameraRequestCode);
            return saveLocation;
        } else {
            final Intent intent = new Intent(fragment.getActivity(), wb.android.google.camera.CameraActivity.class);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, saveLocation);
            fragment.startActivityForResult(intent, localCameraRequestCode);
            return saveLocation;
        }
    }
}
