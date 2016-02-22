package co.smartreceipts.android.workers;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import wb.android.flex.Flex;
import wb.android.image.ImageUtils;
import wb.android.storage.StorageManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import co.smartreceipts.android.R;
import co.smartreceipts.android.persistence.Preferences;

public class ImageGalleryWorker extends WorkerChild {

    private static final String TAG = ImageGalleryWorker.class.getSimpleName();
	private static final int GALLERY_TIME_DIFF_MILLIS = 15000; //5secs
	
	private final StorageManager mStorageManager;
	private final Preferences mPreferences;
	private final Flex mFlex;
	
	ImageGalleryWorker(WorkerManager manager, StorageManager storageManager, Preferences preferences, Flex flex) {
		super(manager);
		mStorageManager = storageManager;
		mPreferences = preferences;
		mFlex = flex;
	}

    /**
     * Some phones have a bug (feature?) in which a gallery image is created when a photo is taken
     * with the native camera. This happens regardless of whether or not a path is manually specified.
     * This method deletes the first gallery image, which was taken within the last 5 seconds, to resolve
     * for this issue. If no image was added to the gallery in the last 5 seconds (i.e. there are no
     * duplicates), then nothing happens.
     *
     * @return the rotation of this image or {@link android.media.ExifInterface#ORIENTATION_UNDEFINED} if none was set
     */
    private int deleteDuplicateGalleryImage() {
        Cursor c = null;
        try {
            final Context context = mWorkerManager.getApplication();
            final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.ORIENTATION };
            c = MediaStore.Images.Media.query(context.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageColumns,
                    MediaStore.Images.Media.DATE_TAKEN + " > " + (System.currentTimeMillis() - GALLERY_TIME_DIFF_MILLIS),
                    MediaStore.Images.Media.DATE_TAKEN + " desc");
            if(c.moveToFirst()){
                int id = c.getInt(c.getColumnIndex(MediaStore.Images.Media._ID));
                int orientiation = c.getInt(c.getColumnIndex(MediaStore.Images.Media.ORIENTATION));
                context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.Media._ID + "=?",
                        new String[]{ Integer.toString(id) } );
                return orientiation;
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
            if (c != null)
                c.close();
        }
    }

    /**
     * Attempts to write an image file to the proper Smart Receipts folder path.
     * It will also resize the image if it's over 1024x1024.
     * Currently, this method is run in the UI thread... It needs to be re-written
     * but I haven't had a chance to get around to this.
     *
     * @param imageUri - The "cached" image save location. Used if imageDesitnation is null
     * @param data - The Intent data. This will be used to get the Uri location if both other params are null
     * @param imageDestination - The default save location. If this is null, imageUri will be used
     * @return
     */
    public File transformNativeCameraBitmap(final Uri imageUri, final Intent data, Uri imageDestination) {
        // TODO: Move this all to a separate thread
        Log.d(TAG, "Handling image save for: {" + imageUri + ";" + data + ";" + imageDestination + "}");
        System.gc();
        int orientation = this.deleteDuplicateGalleryImage(); //Some devices duplicate the gallery images
        Uri imageUriCopy;
        if (imageUri != null) {
            imageUriCopy = Uri.parse(imageUri.toString());
        }
        else {
            if (data != null) {
                imageUriCopy = data.getData();
            }
            else {
                Log.e(TAG, "Failed to find enough intent data to save. Aborting");
                return null;
            }
        }
        if (imageDestination == null) {
            imageDestination = imageUriCopy;
        }
        final Context context = mWorkerManager.getApplication();
        if (imageDestination == null) {
            Toast.makeText(context, mFlex.getString(context, R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No save destination. Aborting");
            return null;
        }
        File imgFile = new File(imageDestination.getPath());
        final int maxDimension = 1024;
        BitmapFactory.Options fullOpts = new BitmapFactory.Options();
        fullOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageUriCopy.getPath(), fullOpts);
        int fullWidth=fullOpts.outWidth, fullHeight=fullOpts.outHeight;
        fullOpts = null;
        int scale=1;
        while(fullWidth > maxDimension && fullHeight > maxDimension){
            fullWidth>>>=1;
            fullHeight>>>=1;
            scale<<=1;
        }
        BitmapFactory.Options smallerOpts = new BitmapFactory.Options();
        smallerOpts.inSampleSize=scale;
        System.gc();

        // First, just see if we can get the bitmap directly
        Bitmap endBitmap = BitmapFactory.decodeFile(imageUriCopy.getPath(), smallerOpts);

        if (endBitmap == null) {
            // If not, let's try to decode from the media store
            endBitmap = BitmapFactory.decodeFile(getMediaStoreUri(imageUriCopy).getPath(), smallerOpts);
        }

        if (endBitmap == null) {
            // If we still have nothing, see if we can use some KitKat and above fixes
            endBitmap = getKitKatAndAboveBitmap(imageUriCopy);
        }

        if (orientation == ExifInterface.ORIENTATION_UNDEFINED) {
            try {
                ExifInterface exif = new ExifInterface(imageUriCopy.getPath());
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            }
            catch (IOException e) { }
        }
        if (mPreferences.getRotateImages() && orientation != ExifInterface.ORIENTATION_UNDEFINED) {
            endBitmap = ImageUtils.rotateBitmap(endBitmap, orientation);
        }
        if (mPreferences.isCameraGrayScale()) {
            endBitmap = ImageUtils.convertToGrayScale(endBitmap);
        }
        if (!mStorageManager.writeBitmap(imageDestination, endBitmap, CompressFormat.JPEG, 85)) {
            Toast.makeText(context, mFlex.getString(context, R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to write the image data. Aborting");
            imgFile = null;
        }
        return imgFile;
    }

    @NonNull
    private Uri getMediaStoreUri(@NonNull Uri photoUri) {
        final String[] selection = {MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = mWorkerManager.getApplication().getContentResolver().query(photoUri, selection, null, null, null);
            if (cursor!= null && cursor.moveToFirst()) {
                final int columnIndex = cursor.getColumnIndex(selection[0]);
                final String newPath = cursor.getString(columnIndex);
                if (newPath != null) {
                    return Uri.parse(newPath);
                } else {
                    return Uri.EMPTY;
                }
            } else {
                return Uri.EMPTY;
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unabled to find the requested column", e);
            return Uri.EMPTY;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Bitmap getKitKatAndAboveBitmap(@NonNull Uri photoUri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = mWorkerManager.getApplication().getContentResolver().openFileDescriptor(photoUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            return BitmapFactory.decodeFileDescriptor(fileDescriptor);
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                // Close quietly
            }
        }
    }

}