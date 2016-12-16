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
import co.smartreceipts.android.utils.IntentUtils;
import co.smartreceipts.android.utils.cache.SmartReceiptsTemporaryFileCache;

public class CameraInteractionController {

    private final Context mContext;
    private final WeakReference<Fragment> mFragmentReference;
    private final Preferences mPreferences;

    public CameraInteractionController(@NonNull Fragment fragment, @NonNull PersistenceManager persistenceManager) {
        this(fragment, persistenceManager.getPreferences());
    }

    public CameraInteractionController(@NonNull Fragment fragment, @NonNull Preferences preferences) {
        mContext = Preconditions.checkNotNull(fragment.getContext()).getApplicationContext();
        mFragmentReference = new WeakReference<>(Preconditions.checkNotNull(fragment));
        mPreferences = Preconditions.checkNotNull(preferences);
    }

    /**
     * Takes a photo for a given trip directory
     *
     * @return the Uri result of the photo
     */
    @NonNull
    public Uri takePhoto() {
        return startPhotoIntent(new SmartReceiptsTemporaryFileCache(mContext).getFile(System.currentTimeMillis() + "x.jpg"), RequestCodes.NATIVE_NEW_RECEIPT_CAMERA_REQUEST, RequestCodes.NEW_RECEIPT_CAMERA_REQUEST);
    }

    /**
     * Takes a photo for a given receipt
     *
     * @return the Uri result of the photo
     */
    @NonNull
    public Uri addPhoto() {
        return startPhotoIntent(new SmartReceiptsTemporaryFileCache(mContext).getFile(System.currentTimeMillis() + "x.jpg"), RequestCodes.NATIVE_ADD_PHOTO_CAMERA_REQUEST, RequestCodes.ADD_PHOTO_CAMERA_REQUEST);
    }

    /**
     * Retakes a photo for a given receipt
     *
     * @param receipt the desired {@link Receipt}
     * @return the Uri result of the photo
     */
    @NonNull
    public Uri retakePhoto(@NonNull Receipt receipt) {
        Preconditions.checkNotNull(receipt.getFile());
        return startPhotoIntent(receipt.getFile(), RequestCodes.NATIVE_RETAKE_PHOTO_CAMERA_REQUEST, RequestCodes.RETAKE_PHOTO_CAMERA_REQUEST);
    }

    @NonNull
    private Uri startPhotoIntent(@NonNull File saveLocation, int nativeCameraRequestCode, int localCameraRequestCode) {
        final Fragment fragment = mFragmentReference.get();
        if (fragment == null || !fragment.isResumed()) {
            return Uri.EMPTY;
        }

        final boolean hasCameraPermission = ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        final boolean hasWritePermission = ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (mPreferences.useNativeCamera() || !hasCameraPermission || !hasWritePermission) {
            final Intent intent = IntentUtils.getImageCaptureIntent(fragment.getActivity(), saveLocation);
            fragment.startActivityForResult(intent, nativeCameraRequestCode);
            return intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        } else {
            final Uri saveLocationUri = Uri.fromFile(saveLocation);
            final Intent intent = new Intent(fragment.getActivity(), wb.android.google.camera.CameraActivity.class);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, saveLocationUri);
            fragment.startActivityForResult(intent, localCameraRequestCode);
            return saveLocationUri;
        }
    }
}
