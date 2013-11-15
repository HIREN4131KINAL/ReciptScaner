package wb.receiptslibrary.fragments;

import java.io.File;

import wb.android.google.camera.Util;
import wb.android.storage.StorageManager;
import wb.android.ui.PinchToZoomImageView;
import wb.receiptslibrary.BuildConfig;
import wb.receiptslibrary.R;
import wb.receiptslibrary.legacycamera.MyCameraActivity;
import wb.receiptslibrary.model.ReceiptRow;
import wb.receiptslibrary.model.TripRow;
import wb.receiptslibrary.workers.ImageGalleryWorker;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ReceiptImageFragment extends WBFragment {

	private static final String TAG = "ReceiptImageFragment";
	
	//Activity Request ints
	private static final int RETAKE_PHOTO_CAMERA_REQUEST = 1;
	private static final int NATIVE_RETAKE_PHOTO_CAMERA_REQUEST = 2;
	
	//Preferences
	private static final String PREFERENCES = "ReceiptImageFragment.xml";
	private static final String PREFERENCE_RECEIPT_ID = "receiptId";
	private static final String PREFERENCE_RECEIPT_PATH = "receiptPath";
	private static final String PREFERENCE_RECEIPT_IMAGE_URI = "receiptImageUri";
	
	private ReceiptRow mCurrentReceipt;
	private String mReceiptPath;
	private PinchToZoomImageView mImageView;
	private boolean mIsRotateOngoing;
	private Uri mImageUri;
	
	private static final String BUNDLE_RECEIPT_PARCEL_KEY = "wb.receiptslibrary.model.ReceiptRow#Parcel";
	private static final String BUNDLE_RECEIPT_PATH_STRING_KEY = "mReceiptPath#String";
	public static ReceiptImageFragment newInstance(ReceiptRow currentReceipt, TripRow tripRow) {
		ReceiptImageFragment fragment = new ReceiptImageFragment();
		Bundle args = new Bundle();
		args.putParcelable(BUNDLE_RECEIPT_PARCEL_KEY, currentReceipt);
		args.putString(BUNDLE_RECEIPT_PATH_STRING_KEY, tripRow.getDirectoryPath());
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mIsRotateOngoing = false;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(getLayoutId(), container, false);
		mImageView = (PinchToZoomImageView) rootView.findViewById(R.id.receiptimagefragment_imageview);
		return rootView;
	}
	
	public int getLayoutId() {
		return R.layout.receiptimagefragment;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (mCurrentReceipt != null) {
			// Save persistent data state
	    	SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES, 0);
	    	SharedPreferences.Editor editor = preferences.edit();
	    	editor.putInt(PREFERENCE_RECEIPT_ID, mCurrentReceipt.getId());
	    	editor.putString(PREFERENCE_RECEIPT_PATH, mReceiptPath);
	    	final String uriPath = (mImageUri == null) ? null : mImageUri.toString();
	    	editor.putString(PREFERENCE_RECEIPT_IMAGE_URI, uriPath);
	    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
	    		editor.apply();
	    	else
	    		editor.commit();
    	}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mCurrentReceipt == null) {
			if (getArguments() != null) {
				mReceiptPath = getArguments().getString(BUNDLE_RECEIPT_PATH_STRING_KEY);
				Parcelable parcel = getArguments().getParcelable(BUNDLE_RECEIPT_PARCEL_KEY);
				if (parcel == null || !(parcel instanceof ReceiptRow)) {
					restoreData();
				}
				else {
					setCurrentReceipt((ReceiptRow)parcel);
				}
			}
			else {
				restoreData();
			}
		}
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	private void setCurrentReceipt(ReceiptRow receipt) {
		mCurrentReceipt = receipt;
		if (mCurrentReceipt.hasImage()) {
			mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentReceipt.getImage().getAbsolutePath()));
			getSherlockActivity().getSupportActionBar().setTitle(mCurrentReceipt.getName());
		}
		else {
			Toast.makeText(getSherlockActivity(), getFlexString(R.string.IMG_OPEN_ERROR), Toast.LENGTH_SHORT).show();
		}
	}
	
	// Restore persistent data
	private void restoreData() {
		SharedPreferences preferences = getActivity().getSharedPreferences(PREFERENCES, 0);
    	final int receiptId = preferences.getInt(PREFERENCE_RECEIPT_ID, -1);
    	mCurrentReceipt = getPersistenceManager().getDatabase().getReceiptByID(receiptId);
    	mReceiptPath = preferences.getString(PREFERENCE_RECEIPT_PATH, "");
    	final String uriPath = preferences.getString(PREFERENCE_RECEIPT_IMAGE_URI, null);
    	if (uriPath != null) {
    		mImageUri = Uri.parse(uriPath);
    	}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_receiptimage, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getNavigator().naviagteBackwards();
			return true;
		}
		else if (item.getItemId() == R.id.menu_receiptimage_retake) {
    		if (getPersistenceManager().getPreferences().useNativeCamera()) {
    			final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    			mImageUri = Uri.fromFile(new File(mReceiptPath, mCurrentReceipt.getImage().getName()));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(intent, NATIVE_RETAKE_PHOTO_CAMERA_REQUEST);
                return true;
    		}
    		else {
    			if (wb.android.google.camera.common.ApiHelper.NEW_SR_CAMERA_IS_SUPPORTED) {
    				final Intent intent = new Intent(getSherlockActivity(), wb.android.google.camera.CameraActivity.class);
    				mImageUri = Uri.fromFile(new File(mReceiptPath, mCurrentReceipt.getImage().getName()));
    				intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
    				startActivityForResult(intent, RETAKE_PHOTO_CAMERA_REQUEST);
    			}
    			else {
		    		final Intent intent = new Intent(getSherlockActivity(), MyCameraActivity.class);
					String[] strings  = new String[] {mReceiptPath, mCurrentReceipt.getImage().getName()};
					intent.putExtra(MyCameraActivity.STRING_DATA, strings);
					startActivityForResult(intent, RETAKE_PHOTO_CAMERA_REQUEST);
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
    	if (BuildConfig.DEBUG) Log.d(TAG, "Result Code: " + resultCode);
    	if (resultCode == Activity.RESULT_OK) { //-1
    		final ImageGalleryWorker worker = getWorkerManager().getImageGalleryWorker();
    		worker.deleteDuplicateGalleryImage(); //Some devices duplicate the gallery images
			File imgFile = worker.transformNativeCameraBitmap(mImageUri, data, null);
			if (imgFile == null) {
				Toast.makeText(getSherlockActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
				return;
			}
    		switch (requestCode) {
				case NATIVE_RETAKE_PHOTO_CAMERA_REQUEST:
				case RETAKE_PHOTO_CAMERA_REQUEST:
					final ReceiptRow retakeReceipt = getPersistenceManager().getDatabase().updateReceiptImg(mCurrentReceipt, imgFile);
					if (retakeReceipt != null) {
						mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentReceipt.getImage().getAbsolutePath()));
					}
					else {
						Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
						//Add overwrite rollback here
						return;
					}
				break;
				default:
					if (BuildConfig.DEBUG) Log.e(TAG, "Unrecognized Request Code: " + requestCode);
					super.onActivityResult(requestCode, resultCode, data);
				break;
			}
    	}
    	else if (resultCode == MyCameraActivity.PICTURE_SUCCESS) {  //51
	    	switch (requestCode) {
				case RETAKE_PHOTO_CAMERA_REQUEST:
					File retakeImg = new File(data.getStringExtra(MyCameraActivity.IMG_FILE));
					final ReceiptRow retakeReceipt = getPersistenceManager().getDatabase().updateReceiptImg(mCurrentReceipt, retakeImg);
					if (retakeReceipt != null) {
						mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentReceipt.getImage().getAbsolutePath()));
					}
					else {
						Toast.makeText(getSherlockActivity(), getFlexString(R.string.DB_ERROR), Toast.LENGTH_SHORT).show();
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
			if (BuildConfig.DEBUG) Log.e(TAG, "Unrecgonized Result Code: " + resultCode);
			super.onActivityResult(requestCode, resultCode, data);
    	}
    }

	private void rotate(int orientation) {
		if (mIsRotateOngoing) return;
		mIsRotateOngoing = true;
		(new ImageRotater(orientation, mCurrentReceipt.getImage())).execute(new Void[0]);
	}
	
	private void onRotateComplete(boolean success) {
		if (!success)
			Toast.makeText(getSherlockActivity(), "Image Rotate Failed", Toast.LENGTH_SHORT).show();
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
				StorageManager storage = getPersistenceManager().getStorageManager();
				File root = mImg.getParentFile();
				String filename = mImg.getName();
				Bitmap bitmap = storage.getBitmap(root, filename);
				bitmap = Util.rotate(bitmap, mOrientation);
				storage.writeBitmap(root, bitmap, filename, CompressFormat.JPEG, 85);
				return bitmap;
			}
			catch (Exception e) {
				if (BuildConfig.DEBUG) Log.e(TAG, e.toString());
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if (result == null)
				onRotateComplete(false);
			else {
				mImageView.setImageBitmap(result);
				onRotateComplete(true);
			}
		}
		
	}
	
}