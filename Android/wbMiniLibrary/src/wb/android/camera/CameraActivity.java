package wb.android.camera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public abstract class CameraActivity extends Activity {
    
	//logging variables
    private static final boolean D = true;
    private static final String TAG = "CameraActivity";
    
    //instance variables
    private CameraController _cameraCont;
    private CameraPreview _preview;
    private CaptureHandler _captureHandler;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
    	if(D) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Build a camera controller and a camera preview
        _captureHandler = new CaptureHandler(this);
		_cameraCont = new CameraController(_captureHandler);
		_preview = new CameraPreview(this);
    }
    
    /**
     * Allow for a user to use a custom preview
     * @param savedInstanceState
     * @param customPreview
     */
    public final void onCreate(final Bundle savedInstanceState, final CameraPreview customPreview) {
    	if(D) Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Build a camera controller and a camera preview
        _captureHandler = new CaptureHandler(this);
		_cameraCont = new CameraController(_captureHandler);
		_preview = customPreview;
    }

    @Override
    protected void onResume() {
    	if(D) Log.d(TAG, "onResume");
        super.onResume();
        this.restartCameraService();
    }
    
    protected void restartCameraService() {
    	System.gc(); //Garage Collect
        this.setContentView(_preview);
        try {
        	_cameraCont.startCamera(_preview); //Opens the default (rear-facing) camera
        }
        catch (RuntimeException e) {
        	Toast.makeText(this, "Error: Another application is currently controlling the camera", Toast.LENGTH_SHORT).show();
        	finish();
		}
        this.postReviewCallback(_cameraCont);
    }

    @Override
    protected void onPause() {
    	if(D) Log.d(TAG, "onPause");
        super.onPause();  
        // Because the Camera object is a shared resource, be sure to release it when the activity is paused
        _cameraCont.stopCamera();
    }
    
    public abstract void postReviewCallback(CameraController controller);
    public abstract void autoFocusSuccessCallback();
    public abstract void autoFocusFailureCallback();
    public abstract void pictureTakenCallback(byte[] jpg);    
    
}