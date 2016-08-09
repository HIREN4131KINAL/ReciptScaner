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
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.Preferences;
import co.smartreceipts.android.utils.UriUtils;
import rx.Observable;
import rx.Subscriber;
import wb.android.image.ImageUtils;
import wb.android.storage.StorageManager;

public class ImageImportProcessor implements FileImportProcessor {

    private static final String TAG = ImageImportProcessor.class.getSimpleName();
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
                        if (mPreferences.getRotateImages()) {
                            int orientation = getOrientation(uri);
                            if (orientation == ExifInterface.ORIENTATION_UNDEFINED) {
                                try {
                                    final ExifInterface exif = new ExifInterface(uri.getPath());
                                    orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                                }
                                catch (IOException e) {
                                    Log.e(TAG, "" + e);
                                }
                            }
                            if (orientation != ExifInterface.ORIENTATION_UNDEFINED) {
                                bitmap = ImageUtils.rotateBitmap(bitmap, orientation);
                            }
                        }

                        // Save the file
                        final File destination = mStorageManner.getFile(mTrip.getDirectory(), System.currentTimeMillis() + "." + UriUtils.getExtension(uri, mContentResolver));
                        if (!mStorageManner.writeBitmap(Uri.fromFile(destination), bitmap, Bitmap.CompressFormat.JPEG, 85)) {
                            Log.e(TAG, "Failed to write the image data. Aborting");
                            subscriber.onError(new IOException());
                        } else {
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
                final BitmapFactory.Options fullOpts = new BitmapFactory.Options();
                fullOpts.inJustDecodeBounds = true;

                int fullWidth = fullOpts.outWidth, fullHeight = fullOpts.outHeight;
                int scale = 1;
                while (fullWidth > maxDimension && fullHeight > maxDimension) {
                    fullWidth >>>= 1;
                    fullHeight >>>= 1;
                    scale <<= 1;
                }
                return scale;
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to process image scale. " + e);
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
            c = MediaStore.Images.Media.query(mContentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, MediaStore.Images.Media.EXTERNAL_CONTENT_URI + " = ?", new String[] { externalUri.getPath() }, null);
            if(c.moveToFirst()){
                return c.getInt(c.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
            }
            else {
                return ExifInterface.ORIENTATION_UNDEFINED;
            }
        }
        catch (Exception e) {
            Log.e(TAG, "", e);
            return ExifInterface.ORIENTATION_UNDEFINED;
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }



}
