package co.smartreceipts.android.workers;

import java.io.File;

import wb.android.flex.Flex;
import wb.android.storage.StorageManager;
import co.smartreceipts.android.R;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

public class ImageGalleryWorker extends WorkerChild {

	private static final int GALLERY_TIME_DIFF_MILLIS = 5000; //5secs
	
	private final StorageManager mStorageManager;
	private final Flex mFlex;
	
	ImageGalleryWorker(WorkerManager manager, StorageManager storageManager, Flex flex) {
		super(manager);
		mStorageManager = storageManager;
		mFlex = flex;
	}

	/**
	 * Some phones have a bug (feature?) in which a gallery image is created when a photo is taken
	 * with the native camera. This happens regardless of whether or not a path is manually specified.
	 * This method deletes the first gallery image, which was taken within the last 5 seconds, to resolve
	 * for this issue. If no image was added to the gallery in the last 5 seconds (i.e. there are no
	 * duplicates), then nothing happens
	 */
    public void deleteDuplicateGalleryImage() {
    	Cursor c = null;
    	try {
    		final Context context = mWorkerManager.getContext();
	    	final String[] imageColumns = { MediaStore.Images.Media._ID };
	    	c = MediaStore.Images.Media.query(context.getContentResolver(), 
	    									  MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
	    									  imageColumns, 
	    									  MediaStore.Images.Media.DATE_TAKEN + " > " + (System.currentTimeMillis() - GALLERY_TIME_DIFF_MILLIS), 
	    									  null);
	        if(c.moveToFirst()){
	            int id = c.getInt(c.getColumnIndex(MediaStore.Images.Media._ID));
	            context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
	            												   MediaStore.Images.Media._ID + "=?", 
	            												   new String[]{ Integer.toString(id) } );
	        }
    	}
    	catch (Exception e) { } //Ignore any errors
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
		System.gc();
		Uri imageUriCopy;
		if (imageUri != null)
			imageUriCopy = Uri.parse(imageUri.toString());
		else {
			if (data != null)
				imageUriCopy = data.getData();
			else
				return null;
		}
		if (imageDestination == null)
			imageDestination = imageUriCopy;
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
		Bitmap endBitmap = BitmapFactory.decodeFile(imageUriCopy.getPath(), smallerOpts);
		final Context context = mWorkerManager.getContext();
		if (!mStorageManager.writeBitmap(imageDestination, endBitmap, CompressFormat.JPEG, 85)) {
			Toast.makeText(context, mFlex.getString(context, R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
			imgFile = null;
		}
    	return imgFile;
    }
    
}