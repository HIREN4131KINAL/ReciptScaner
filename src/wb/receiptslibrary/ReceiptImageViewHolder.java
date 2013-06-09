package wb.receiptslibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import wb.android.google.camera.Util;
import wb.android.google.camera.data.Exif;
import wb.android.google.camera.exif.ExifData;
import wb.android.google.camera.exif.ExifReader;
import wb.android.storage.StorageManager;
import wb.android.ui.PinchToZoomImageView;

import android.R.anim;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.wb.navigation.ViewHolder;

public class ReceiptImageViewHolder extends ViewHolder<SmartReceiptsActivity>  {

	private static final boolean D = SmartReceiptsActivity.D;
	private static final String TAG = "ReceiptImageViewHolder";
	
	//Activity Request ints
	private static final int RETAKE_PHOTO_CAMERA_REQUEST = 1;
	private static final int NATIVE_RETAKE_PHOTO_CAMERA_REQUEST = 2;
	
	private ReceiptRow currentReceipt;
	private TripRow currentTrip;
	private PinchToZoomImageView imageView;
	private boolean mIsRotateOngoing;
	private Uri _imageUri;
	
	public ReceiptImageViewHolder(final SmartReceiptsActivity activity, final TripRow currentTrip, final ReceiptRow currentReceipt) {
		super(activity);
		this.currentTrip = currentTrip;
		this.currentReceipt = currentReceipt;
		mIsRotateOngoing = false;
	}
	
	@Override
	public void onCreate() {
    	try {
    		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    		imageView = new PinchToZoomImageView(activity);
    		imageView.setScaleType(ScaleType.FIT_CENTER);
    		if (!currentReceipt.img.exists())
    			Toast.makeText(activity, activity.getFlex().getString(R.string.IMG_OPEN_ERROR), Toast.LENGTH_SHORT).show();
    		System.gc(); 
    		imageView.setImageBitmap(BitmapFactory.decodeFile(currentReceipt.img.getCanonicalPath()));
    		activity.getSupportActionBar().setTitle(currentReceipt.name);
    		activity.setContentView(imageView, params);    		
    	}
    	catch (IOException e) {
    		Toast.makeText(activity, activity.getFlex().getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
    		activity.naviagateBackwards();
    	}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = activity.getSupportMenuInflater();
		inflater.inflate(R.menu.menu_receiptimage, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			activity.naviagateBackwards();
			return true;
		}
		else if (item.getItemId() == R.id.menu_receiptimage_retake) {
    		String dirPath;
    		try {
				if (currentTrip.dir.exists())
					dirPath = currentTrip.dir.getCanonicalPath();
				else
					dirPath = activity.getStorageManager().mkdir(currentTrip.dir.getName()).getCanonicalPath();
    		} catch (IOException e) {
				Toast.makeText(activity, activity.getFlex().getString(R.string.SD_ERROR), Toast.LENGTH_LONG).show();
				return false;
			}
    		if (activity.getPreferences().useNativeCamera()) {
    			final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    			_imageUri = Uri.fromFile(new File(dirPath, currentReceipt.img.getName()));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
                activity.startActivityForResult(intent, NATIVE_RETAKE_PHOTO_CAMERA_REQUEST);
                return true;
    		}
    		else {
    			if (wb.android.google.camera.common.ApiHelper.NEW_SR_CAMERA_IS_SUPPORTED) {
    				final Intent intent = new Intent(activity, wb.android.google.camera.CameraActivity.class);
    				_imageUri = Uri.fromFile(new File(dirPath, currentReceipt.img.getName()));
    				intent.putExtra(MediaStore.EXTRA_OUTPUT, _imageUri);
    				activity.startActivityForResult(intent, RETAKE_PHOTO_CAMERA_REQUEST);
    			}
    			else {
		    		final Intent intent = new Intent(activity, MyCameraActivity.class);
					String[] strings  = new String[] {dirPath, currentReceipt.img.getName()};
					intent.putExtra(MyCameraActivity.STRING_DATA, strings);
					activity.startActivityForResult(intent, RETAKE_PHOTO_CAMERA_REQUEST);
    			}
				return true;
    		}
    	}
		else if (item.getItemId() == R.id.menu_receiptimage_rotate_ccw) {
			rotate(-90);
			return true;
		}
		else if (item.getItemId() == R.id.menu_receiptimage_rotate_cw) {
			rotate(90);
			return true;
		}
		else
			return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    	if (D) Log.d(TAG, "Result Code: " + resultCode);
    	if (resultCode == Activity.RESULT_OK) { //-1
    		activity.deleteDuplicateGalleryImage(); //Some devices duplicate the gallery images
			File imgFile = activity.transformNativeCameraBitmap(_imageUri, data, null);
			if (imgFile == null) {
				Toast.makeText(activity, activity.getFlex().getString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
				return;
			}
    		switch (requestCode) {
				case NATIVE_RETAKE_PHOTO_CAMERA_REQUEST:
				case RETAKE_PHOTO_CAMERA_REQUEST:
					final ReceiptRow retakeReceipt = activity.getDB().updateReceiptImg(currentReceipt, imgFile);
					if (retakeReceipt != null) {
						imageView.setImageBitmap(BitmapFactory.decodeFile(currentReceipt.img.getAbsolutePath()));
					}
					else {
						Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
						//Add overwrite rollback here
						return;
					}
				break;
				default:
					if (D) Log.e(TAG, "Unrecognized Request Code: " + requestCode);
					super.onActivityResult(requestCode, resultCode, data);
				break;
			}
    	}
    	else if (resultCode == MyCameraActivity.PICTURE_SUCCESS) {  //51
	    	switch (requestCode) {
				case RETAKE_PHOTO_CAMERA_REQUEST:
					File retakeImg = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
					final ReceiptRow retakeReceipt = activity.getDB().updateReceiptImg(currentReceipt, retakeImg);
					if (retakeReceipt != null) {
						imageView.setImageBitmap(BitmapFactory.decodeFile(currentReceipt.img.getAbsolutePath()));
					}
					else {
						Toast.makeText(activity, activity.getFlex().getString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
						//Add overwrite rollback here
						return;
					}
				break;
				default:
					Log.e(TAG, "Unrecognized Request Code: " + requestCode);
					super.onActivityResult(requestCode, resultCode, data);
				break;
			}
    	}
    	else {
			if (D) Log.e(TAG, "Unrecgonized Result Code: " + resultCode);
			super.onActivityResult(requestCode, resultCode, data);
    	}
    }

	private void rotate(int orientation) {
		if (mIsRotateOngoing) return;
		mIsRotateOngoing = true;
		(new ImageRotater(orientation, currentReceipt.img)).execute(new Void[0]);
	}
	
	private void onRotateComplete(boolean success) {
		if (!success)
			Toast.makeText(activity, "Image Rotate Failed", Toast.LENGTH_SHORT).show();
		mIsRotateOngoing = false;
	}
	
	private class ImageRotater extends AsyncTask<Void, Void, Bitmap> {

		private final int mOrientation;
		private final File mImg;
		
		public ImageRotater(int orientation, File img) {
			mOrientation = orientation;
			mImg = img;
		}
		
		@Override
		protected Bitmap doInBackground(Void... params) {
			try {
				StorageManager storage = activity.getStorageManager();
				File root = mImg.getParentFile();
				String filename = mImg.getName();
				Bitmap bitmap = storage.getBitmap(root, filename);
				bitmap = Util.rotate(bitmap, mOrientation);
				storage.writeBitmap(root, bitmap, filename, CompressFormat.JPEG, 85);
				return bitmap;
			}
			catch (Exception e) {
				if (D) Log.e(TAG, e.toString());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null)
				onRotateComplete(false);
			else {
				imageView.setImageBitmap(result);
				onRotateComplete(true);
			}
		}
		
	}
}