package co.smartreceipts.android.imports;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.utils.UriUtils;
import rx.Observable;
import rx.Subscriber;
import wb.android.image.ImageUtils;
import wb.android.storage.StorageManager;

public class ImageImportProcessor implements FileImportProcessor {

    private static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    private final Trip mTrip;
    private final StorageManager mStorageManner;
    private final Preferences mPreferences;
    private final Context mContext;
    private final ContentResolver mContentResolver;

    public ImageImportProcessor(@NonNull Trip trip, @NonNull StorageManager storageManager, @NonNull Preferences preferences, @NonNull Context context) {
        this(trip, storageManager, preferences, context, context.getContentResolver());
    }

    public ImageImportProcessor(@NonNull Trip trip, @NonNull StorageManager storageManager, @NonNull Preferences preferences, @NonNull Context context, @NonNull ContentResolver contentResolver) {
        mTrip = Preconditions.checkNotNull(trip);
        mStorageManner = Preconditions.checkNotNull(storageManager);
        mPreferences = Preconditions.checkNotNull(preferences);
        mContext = Preconditions.checkNotNull(context.getApplicationContext());
        mContentResolver = Preconditions.checkNotNull(contentResolver);
    }

    @NonNull
    @Override
    public Observable<File> process(@NonNull final Uri uri) {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                InputStream inputStream = null;
                try {
                    final int scale = getImageScaleFactor(uri);
                    inputStream = mContentResolver.openInputStream(uri);
                    if (inputStream != null) {
                        // Get scaled bitmapt
                        final BitmapFactory.Options smallerOpts = new BitmapFactory.Options();
                        smallerOpts.inSampleSize = scale;
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, smallerOpts);

                        // Perform image processing
                        if (mPreferences.isCameraGrayScale()) {
                            bitmap = ImageUtils.convertToGrayScale(bitmap);
                        }

                        boolean wasRotationHandled = false;
                        if (mPreferences.getRotateImages()) {
                            int orientation = getOrientation(uri);
                            if (orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                                bitmap = ImageUtils.rotateBitmap(bitmap, orientation);
                                wasRotationHandled = true;
                            } else {
                                Logger.warn(this, "Failed to fetch orientation information from the content store");
                            }
                        }

                        // Save the file
                        final File destination = mStorageManner.getFile(mTrip.getDirectory(), System.currentTimeMillis() + "." + UriUtils.getExtension(uri, mContentResolver));
                        if (!mStorageManner.writeBitmap(Uri.fromFile(destination), bitmap, Bitmap.CompressFormat.JPEG, 85)) {
                            Logger.error(this, "Failed to write the image data. Aborting");
                            subscriber.onError(new IOException());
                        } else {
                            if (mPreferences.getRotateImages() && !wasRotationHandled) {
                                int orientation = ExifInterface.ORIENTATION_UNDEFINED;
                                try {
                                    Logger.info(this, "Attempting to fetch orientation information from the exif data");
                                    // Getting exif from the local file that we just wrote to determine if rotation in necessary
                                    final ExifInterface exif = new ExifInterface(destination.getAbsolutePath());
                                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                                } catch (IOException e) {
                                    Logger.error(this, e);
                                }
                                if (orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                                    bitmap = ImageUtils.rotateBitmap(bitmap, orientation);
                                    mStorageManner.writeBitmap(Uri.fromFile(destination), bitmap, Bitmap.CompressFormat.JPEG, 85);
                                }
                            }
                            subscriber.onNext(destination);
                            subscriber.onCompleted();
                        }
                    } else {
                        subscriber.onError(new FileNotFoundException());
                    }
                } catch (IOException e) {
                    subscriber.onError(e);
                } finally {
                    StorageManager.closeQuietly(inputStream);
                }
            }
        });
    }

    private int getImageScaleFactor(@NonNull Uri uri) {
        InputStream inputStream = null;
        try {
            inputStream = mContentResolver.openInputStream(uri);
            if (inputStream != null) {
                final int maxDimension = 1024;
                final BitmapFactory.Options inJustDecodeBoundsOptions = new BitmapFactory.Options();
                inJustDecodeBoundsOptions.inJustDecodeBounds = true;

                // Decode data to our option bounds but don't read the full image
                BitmapFactory.decodeStream(inputStream, null, inJustDecodeBoundsOptions);

                int fullWidth = inJustDecodeBoundsOptions.outWidth;
                int fullHeight = inJustDecodeBoundsOptions.outHeight;
                int scale = 1;
                while (fullWidth > maxDimension && fullHeight > maxDimension) {
                    fullWidth >>>= 1;
                    fullHeight >>>= 1;
                    scale <<= 1;
                }
                return scale;
            }
        } catch (IOException e) {
            Logger.warn(this, "Failed to process image scale", e);
        } finally {
            StorageManager.closeQuietly(inputStream);
        }
        return 1;
    }

    private int getOrientation(@NonNull Uri externalUri) {
        final boolean hasStoragePermission = ContextCompat.checkSelfPermission(mContext, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!hasStoragePermission) {
            return ExifInterface.ORIENTATION_UNDEFINED;
        }

        Cursor c = null;
        try {
            final String[] imageColumns = { MediaStore.Images.Media.ORIENTATION };
            c = MediaStore.Images.Media.query(mContentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns);
            if(c.moveToFirst()){
                return c.getInt(c.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
            }
            else {
                return ExifInterface.ORIENTATION_UNDEFINED;
            }
        }
        catch (Exception e) {
            Logger.error(this, e);
            return ExifInterface.ORIENTATION_UNDEFINED;
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }



}
