package wb.receiptslibrary.legacycamera;

import java.io.File;
import java.io.IOException;
import java.util.List;

import wb.android.camera.CameraActivity;
import wb.android.camera.CameraController;
import wb.android.storage.StorageManager;
import wb.receiptslibrary.BuildConfig;
import wb.receiptslibrary.SmartReceiptsActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class MyCameraActivity extends CameraActivity {
    
	//logging variables
    private static final boolean D = true;
    private static final String TAG = "MyCameraActivity";
    
    //results codes
    public static final int PICTURE_SUCCESS = 51;
    public static final int AUTOFOCUS_FAILED = 52;
    
    //Extras Filepath
    public static final String IMG_FILE = "bitmap";
    
    //Camera Request Extras
    public static final String STRING_DATA = "strData";
    public static final int DIR = 0;
    public static final int NAME = 1;
    
    //instance variables
    private StorageManager _sdCard;
    private CameraController _cameraCont;
    private CustomPreview _preview;
    private File _dir;
    private String _filename;
    private boolean _photoInProgress;
    private boolean _photoTakenCallback;
    
    private static final int SETTINGS_ID = 1;
    
    private static final String CAMERA_PREFS = "CameraPrefsFile";
    private static final String BOOL_AUTO_FLASH = "autoflash";
    private static final String BOOL_AUTO_FOCUS = "autofocus";
    private static final String BOOL_COLOR = "color";
    List<String> focus, white, flash;
    private boolean _isAutoFlash, _isAutoFocus, _isColor;
    private byte[] _jpg;
    
    @Override
    public final void onCreate(final Bundle savedInstanceState) {
    	if(D) Log.d(TAG, "onCreate");
    	setResult(Activity.RESULT_CANCELED); //In case the user backs out
    	super.onCreate(savedInstanceState);
    	_sdCard = StorageManager.getInstance(this);
        _photoInProgress = false;
        _photoTakenCallback = false;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	_preview = new CustomPreview(this, metrics.widthPixels, metrics.heightPixels);
    	String[] data = getIntent().getExtras().getStringArray(SmartReceiptsActivity.STRING_DATA);
    	_dir = new File(data[SmartReceiptsActivity.DIR]);
    	_filename = data[SmartReceiptsActivity.NAME];
        super.onCreate(savedInstanceState, _preview);
    }
    
    @Override
    //TODO: Fix the problem instead of just hacking around it
    public final void onPause() {
    	super.onPause();
		Intent i = new Intent();
		setResult(Activity.RESULT_CANCELED, i);
		finish();
    }
    
    @Override
    public final void postReviewCallback(final CameraController controller) {
    	_cameraCont = controller;
    	SharedPreferences prefs = getSharedPreferences(CAMERA_PREFS, 0);
    	_isAutoFlash = prefs.getBoolean(BOOL_AUTO_FLASH, true);
    	_isAutoFocus = prefs.getBoolean(BOOL_AUTO_FOCUS, true);
    	_isColor = prefs.getBoolean(BOOL_COLOR, true);
    	Parameters params = controller.getCameraParams();
    	if (params != null) {
			focus = params.getSupportedFocusModes();
			white = params.getSupportedWhiteBalance();
			flash = params.getSupportedFlashModes();
			this.setCameraParams(params);
    	}
    	if (android.os.Build.MODEL.contains("YP-G1CW") || android.os.Build.MODEL.contains("YPG1CW"))
    		controller.setCameraRotation(-90);
    	else
    		controller.setCameraRotation(90); //This could cause issues with different devices
    }
    
    private final void setCameraParams(Parameters params) {
    	SharedPreferences prefs = getSharedPreferences(CAMERA_PREFS, 0);
    	if (android.os.Build.VERSION.SDK_INT >= 14) { //ICS Version
    		if (flash != null && flash.contains(Parameters.FLASH_MODE_OFF))
        		params.setFlashMode(Parameters.FLASH_MODE_OFF);
    	}
    	else {
    		if (_isAutoFlash && flash != null && flash.contains(Parameters.FLASH_MODE_AUTO))
        		params.setFlashMode(Parameters.FLASH_MODE_AUTO);
        	else
        		_isAutoFlash = false;	
    	}
    	if (_isAutoFocus && focus != null && focus.contains(Parameters.FOCUS_MODE_AUTO))
    		params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
    	else
    		_isAutoFocus = false;
    	if (white != null && white.contains(Parameters.WHITE_BALANCE_AUTO))
    		params.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
    	try {
    		_cameraCont.setCameraParams(params);
    	} catch (Exception e) { } //Catches a java.lang.RuntimeException: setParameters failed. Not sure why this is called
    	SharedPreferences.Editor editor = prefs.edit();
    	editor.putBoolean(BOOL_AUTO_FLASH, _isAutoFlash);
    	editor.putBoolean(BOOL_AUTO_FOCUS, _isAutoFocus);
    	editor.putBoolean(BOOL_COLOR, _isColor);
    	editor.commit();
    }

    @Override
    public final void autoFocusSuccessCallback() {
    	if (_photoInProgress)
    		_cameraCont.takePicture();
    }
    
    @Override
    public final void autoFocusFailureCallback() {
    	if (_photoInProgress)
    		_cameraCont.takePicture();
    }
    
    @Override
    public final void pictureTakenCallback(final byte[] jpg) {
		if (BuildConfig.DEBUG) Log.d(TAG, "Picture Taken Callback");
		_photoInProgress = false;
		_photoTakenCallback = true;
		_preview.setPictureTakenCallback(true);
		_jpg = jpg;
    }
    
    @Override
    public final boolean onTouchEvent(MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {
    		if (_photoInProgress)
    			return false;
    		else if (_photoTakenCallback)
	    		return _preview.onTouchEvent(event);
	    	else {
	    		try {
	    			_photoInProgress = true;
	    			_cameraCont.requestAutoFocus();
	    		}
	    		catch (RuntimeException e) { //Occurs if the autofocus failed
	    			Log.e(TAG, "AutoFocus Failed");
	    		}
	    		return true;
	    	}
    	}
    	else
    		return false;
    }
    
    public final void checkCallback() {
    	ProgressDialog progress = ProgressDialog.show(MyCameraActivity.this, "", "Saving Image...", true);
    	progress.show();
    	File img = this.handleImage(_jpg, _isColor, _cameraCont.getCameraRotation());
    	progress.dismiss();
    	try {
        	if (img == null)
        		finish();
        	else {
				Intent i = new Intent();
				i.putExtra(IMG_FILE, img.getCanonicalPath());
				setResult(PICTURE_SUCCESS, i);
				finish();
        	}
    	}
    	catch (IOException e) {
			finish();
		}
    }
    
    public final void xCallback() {
    	_photoTakenCallback = false;
		_preview.setPictureTakenCallback(false);
		this.restartCameraService();
    }

    @Override
    public final boolean onKeyDown(final int keyCode, final KeyEvent event) {
    	if (_photoInProgress)
    		return false;
    	if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
    		if (_photoTakenCallback) {
        		checkCallback();
        		return true;
        	}
    		try {
    			_photoInProgress = true;
    			_cameraCont.requestAutoFocus();
    		}
    		catch (RuntimeException e) { //Occurs if the autofocus failed
    			Log.e(TAG, "AutoFocus Failed");
    		}
    		return true;
    	}
    	else if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (_photoTakenCallback) {
        		xCallback();
        		return true;
        	}
    		Intent i = new Intent();
    		setResult(Activity.RESULT_CANCELED, i);
    		finish();
    		return true;
    	}
		return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
    	MenuItem settings = menu.add(Menu.NONE, SETTINGS_ID, Menu.NONE, "Settings");
    	settings.setIcon(android.R.drawable.ic_menu_preferences);
		return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	if (item.getItemId() == SETTINGS_ID) {
    		final ScrollView scrollView = new ScrollView(this);
    		final LinearLayout layout = new LinearLayout(this);
    		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    		layout.setOrientation(LinearLayout.VERTICAL);
    		layout.setGravity(Gravity.BOTTOM);
    		layout.setPadding(6, 6, 6, 6);
    		final CheckBox autoFlash = new CheckBox(this);
    		autoFlash.setText("Auto Flash");
    		autoFlash.setChecked(_isAutoFlash);
    		final CheckBox autoFocus = new CheckBox(this);
    		autoFocus.setText("Auto Focus");
    		autoFocus.setChecked(_isAutoFocus);
    		final CheckBox color = new CheckBox(this);
    		color.setText("Save as Color Image");
    		color.setChecked(_isColor);
    		layout.addView(autoFlash, params);
    		layout.addView(autoFocus, params);
    		layout.addView(color, params);
    		scrollView.addView(layout);
    		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("Camera Settings")
			 .setCancelable(true)
			 .setView(scrollView)
			 .setPositiveButton("Save", new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int which) {
					 _isAutoFlash = autoFlash.isChecked();
					 _isAutoFocus = autoFocus.isChecked();
					 _isColor = color.isChecked();
					 MyCameraActivity.this.setCameraParams(_cameraCont.getCameraParams());
					 dialog.cancel();   
				 }
			 })
			 .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int which) {
					 dialog.cancel();   
				 }
			 })
			 .create()
			 .show();
    		return true;
    	}
    	return false;
    }
    
    private final File handleImage(byte[] jpg, final boolean isColor, final int rotation) {
    	System.gc(); //Avoid Bitmap memory issues (shrink size first to help memory)
        //Decode image size
    	final int maxDimension = 1024;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(jpg, 0, jpg.length, opts);
        int width_tmp=opts.outWidth, height_tmp=opts.outHeight;
        opts = null;
        int scale=1;
        while(width_tmp > maxDimension && height_tmp > maxDimension){
            width_tmp/=2;
            height_tmp/=2;
            scale*=2;
        }
        //Decode with inSampleSize
        BitmapFactory.Options opts2 = new BitmapFactory.Options();
        opts2.inSampleSize=scale;
        System.gc();
        Bitmap shrink = BitmapFactory.decodeByteArray(jpg, 0, jpg.length, opts2);
    	//jpg = null; //Force Memory free
    	//_jpg = null; //Force Memory free
    	Bitmap bitmap = shrink;
		if (rotation != 0 && shrink != null) {
			Matrix matrix = new Matrix();
			matrix.setRotate(rotation, shrink.getWidth()/2, shrink.getHeight()/2);
			bitmap = Bitmap.createBitmap(shrink, 0, 0, shrink.getWidth(), shrink.getHeight(), matrix, false);
			shrink = null;
		}
		System.gc();
    	if (!isColor)
    		bitmap = this.toGrayscale(bitmap);
    	if (!_sdCard.writeBitmap(_dir, bitmap, _filename, CompressFormat.JPEG, 85)) {
    		Toast.makeText(MyCameraActivity.this, "Error: The Image Failed to Save Properly", Toast.LENGTH_SHORT).show();
    		return null;
    	}
    	else
    		return _sdCard.getFile(_dir, _filename);
    }
    
    private Bitmap toGrayscale(Bitmap bmpOriginal) {
    	int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();    

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
    
}