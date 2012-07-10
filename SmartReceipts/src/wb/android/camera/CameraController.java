package wb.android.camera;

import java.io.IOException;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * This class will perform all interactions with the device's camera
 * @author WRB
 *
 */
public final class CameraController {
	
	//variables for logging
	private static final String TAG = "CameraController";
	private static final boolean D = true;
	
	//message IDs
	static final int FOCUS_SUCCEEDED = 1001;
	static final int FOCUS_FAILED = 1002;
	static final int REQUEST_PICTURE = 1003;
	
	//instance variables
	private Camera _camera;
	private boolean _isPreviewing, _isStarted;
	private HandledAutoFocusCallback _autoFocusCallback;
	private JPGCallback _jpgCallback;
	private int _rotation = 0;
	
	CameraController(final Handler parentHandler) {
		_camera = null;  
		_isPreviewing = false;
		_isStarted = false;
		_autoFocusCallback = new HandledAutoFocusCallback(parentHandler); 
		_jpgCallback = new JPGCallback(parentHandler);
	}
	
	final void setPreviewDisplay(final SurfaceHolder holder) {
		try {
			if (_camera != null)
				_camera.setPreviewDisplay(holder);
		} 
		catch (IOException e) {
			Log.e(TAG, e.toString());
		}
	}
	
	final void startPreview() {
		if (D) Log.d(TAG, "Starting Camera Preview");
		if (_camera != null && !_isPreviewing) {
			_camera.startPreview();
			_isPreviewing = true;
		}
	}
	
	final void stopPreview() {
		if (D) Log.d(TAG, "Stopping Camera Preview");
		if (_camera != null && _isPreviewing) {
			_camera.setPreviewCallback(null); //Not sure if this is needed
			_camera.stopPreview();
			_isPreviewing = false;
		}
	}
	
	public final boolean startCamera(final CameraPreview preview) {
		if (D) Log.d(TAG, "Starting Camera");
		if (_camera == null) {
			_camera = Camera.open();
			_isStarted = true;
			preview.setCameraController(this);
			preview.requestLayout();
			return true;
		}
		return false;
	}
	
	public final void stopCamera() {
		if (D) Log.d(TAG, "Stopping Camera");
		if (_camera != null) {
			_isStarted = false;
			_camera.stopPreview();
			_camera.setPreviewCallback(null);
			_camera.release(); //Since the camera is a shared resource, you have to release it
			_camera = null;
		}
	}
	
	public final void requestAutoFocus() {
		if (_camera != null && _isPreviewing)
			_camera.autoFocus(_autoFocusCallback);
	}
	
	public final void takePicture() {
		if (_camera != null && _isPreviewing)
			_camera.takePicture(null, null, _jpgCallback);
	}
	
	public final void setCameraRotation(final int degrees) {
		if (_camera == null)
			return;
		if (degrees == 0 || degrees == 90 || degrees == 180 || degrees == 270) {
			_rotation = degrees;
			_camera.setDisplayOrientation(degrees);
		}
		else
			Log.e(TAG, "Invalid Rotation Angle. Only 0, 90, 180, and 270 are allowed");
	}
	
	public final int getCameraRotation() {
		return _rotation;
	}
	
	/**
	 * Use this to set optional camera parameters if desired
	 */
	public final void setCameraParams(final Parameters params) {
		if (_camera != null && params != null) {
			try {
				_camera.setParameters(params);
			}
			catch (StringIndexOutOfBoundsException ex) {} //Android doesn't check if the Parameters HashMap is empty -> http://grepcode.com/file_/repository.grepcode.com/java/ext/com.google.android/android/2.2_r1.1/android/hardware/Camera.java/?v=source
		}
	}
	
	public final Parameters getCameraParams() {
		if (_camera != null)
			return _camera.getParameters();
		else
			return null;
	}
	
	public final boolean isStarted() {
		return _isStarted;
	}

}
