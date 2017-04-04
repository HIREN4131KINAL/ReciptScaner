package wb.android.camera;

import android.annotation.SuppressLint;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * This code is adapted from the google camera APIs. Check it online for more details
 */
@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    
	//logging variables
	private static final String TAG = "Preview";
	private static final boolean D = true;
	
	//Constants
	public static final String LANDSCAPE = "Landscape";
	public static final String PORTRAIT = "Portrait";
	
	//instance variables
    final SurfaceHolder _holder;
    private Size _previewSize, _pictureSize;
    private CameraController _controller;
    private boolean _isPortraitOreintation;

    @SuppressWarnings("deprecation")
	public CameraPreview(final CameraActivity cameraActivity) {
        super(cameraActivity);
        _holder = this.getHolder();
        _holder.addCallback(this);
        _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        _isPortraitOreintation = true;
        _previewSize = null;
        _pictureSize = null;
    }
    
    public final void changeOrientation() {
    	_isPortraitOreintation ^= _isPortraitOreintation;
    }
    
    public final String orientation() {
    	if (_isPortraitOreintation)
    		return PORTRAIT;
    	else
    		return LANDSCAPE;
    }
    
    final void setCameraController(final CameraController controller) {
    	_controller = controller;
    }


/********************************************************************************************************
 * Below is code from google (though partly adapted to suit my needs)
 ********************************************************************************************************/
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
    	if (D) Log.d(TAG, "onMeasure");
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (!_controller.isStarted()) return; 
        //Find the Optimal Preview and Camera Sizes for this device
        Parameters params = _controller.getCameraParams();
        if (android.os.Build.MODEL.equalsIgnoreCase("DROIDX") || android.os.Build.MODEL.equalsIgnoreCase("DROID X")) {//Droid X seems to have issues with preview sizes that are too large
        	_previewSize = params.getPreviewSize();
        }
        if (params == null) return;
        List<Size> previewSizes = params.getSupportedPreviewSizes();
        List<Size> pictureSizes = params.getSupportedPictureSizes();
        final float ASPECT_TOLERANCE = 0.1f;
        float targetRatio = (float) width / height;
        if (previewSizes == null || pictureSizes == null) return;
        int num;
        Size size;
        // ====== Determine Preview Size ======= \\
        float minDiff = Float.MAX_VALUE;
        num = previewSizes.size();
        // Try to find an preview size match aspect ratio and size
        if (_previewSize == null) {
	        for (int i=0; i<num; i++) {
	        	size = previewSizes.get(i);
	            float ratio = (float) size.width / size.height;
	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
	            if (Math.abs(size.height - height) < minDiff) {
	                _previewSize = size;
	                minDiff = Math.abs(size.height - height);
	            }
	        }
        }
        // Cannot find the one match the aspect ratio for the preview size, ignore the requirement
        if (_previewSize == null) {
            minDiff = Float.MAX_VALUE;
            for (int i=0; i<num; i++) {
            	size = previewSizes.get(i);
                if (Math.abs(size.height - height) < minDiff) {
                    _previewSize = size;
                    minDiff = Math.abs(size.height - height);
                }
            }
        }
        // ====== Determine Picture Size ======= \\
        minDiff = Float.MAX_VALUE;
        num = pictureSizes.size();
        targetRatio = (float) _previewSize.width / _previewSize.height;
        // Try to find an preview size match aspect ratio and size - Try to find with dimensions smaller than 1024
        if (_pictureSize == null) {
	        for (int i=0; i<num; i++) {
	        	size = pictureSizes.get(i);
	            float ratio = (float) size.width / size.height;
	            int maxDim = (ratio > 1.0) ? size.width : size.height;
	            if (maxDim > 1440) continue;
	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
	            if (Math.abs(size.height - _previewSize.height) < minDiff) {
	            	Log.e(TAG, "Thjis");
	                _pictureSize = size;
	                minDiff = Math.abs(size.height - _previewSize.height);
	            }
	        }
        }
        // Try to find the max picture size that matches the preview size's aspect ratio
        if (_pictureSize == null) {
	        for (int i=0; i<num; i++) {
	        	size = pictureSizes.get(i);
	            float ratio = (float) size.width / size.height;
	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
	            if (Math.abs(size.height - _previewSize.height) < minDiff) {
	                _pictureSize = size;
	                minDiff = Math.abs(size.height - _previewSize.height);
	            }
	        }
        }
        // Try to find the closet picture size to the preview size
        if (_pictureSize == null) {
            minDiff = Float.MAX_VALUE;
            for (int i=0; i<num; i++) {
            	size = pictureSizes.get(i);
                if (Math.abs(size.height - _previewSize.height) < minDiff) {
                	_pictureSize = size;
                    minDiff = Math.abs(size.height - _previewSize.height);
                }
            }
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
    	super.onLayout(changed, l, t, r, b);
    	/*
    	if (D) Log.d(TAG, "onLayout");
        if (changed) {
            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (_previewSize != null) {
            	if (_isPortraitOreintation) {
            		previewWidth = _previewSize.height;
                	previewHeight = _previewSize.width;
            	}
            	else {
            		previewWidth = _previewSize.width;
                	previewHeight = _previewSize.height;
            	}
            }

            if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                this.layout((width - scaledChildWidth) / 2, 0, (width + scaledChildWidth) / 2, height);
            } 
            else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                this.layout(l, (height - scaledChildHeight) / 2, width+l, (height + scaledChildHeight) / 2);
            }
        }*/
    }

    /*
    private final Size getOptimalPreviewSize(final int w, final int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        Log.e(TAG, "oW: " + optimalSize.width + ", oH: " + optimalSize.height);
        return optimalSize;
    }*/

/********************************************************************************************************
 * SurfaceHolder.Callback Methods
 ********************************************************************************************************/
 // The Surface has been created, acquire the camera and tell it where to draw.
    public final void surfaceCreated(final SurfaceHolder holder) {
    	if (D) Log.d(TAG, "surfaceCreated");   	
    	_controller.setPreviewDisplay(holder);
    	Parameters params = _controller.getCameraParams();
    	if (params != null) {
	    	if (_previewSize != null) params.setPreviewSize(_previewSize.width, _previewSize.height);
	    	if (_pictureSize != null) params.setPictureSize(_pictureSize.width, _pictureSize.height);
	    	_controller.setCameraParams(params);
    	}
    	this.setWillNotDraw(false);
    }

    // Surface will be destroyed when we return, so stop the preview
    public final void surfaceDestroyed(final SurfaceHolder holder) {
    	if (D) Log.d(TAG, "surfaceDestroyed");
    	this.setWillNotDraw(true);
    	_controller.stopPreview(); //Need a safe way to stop the preview when the activity isn't killed
    	_controller.stopCamera();
    	
    }
    
    // Now that the size is known, set up the camera parameters and begin
    public final void surfaceChanged(final SurfaceHolder holder, final int format, final int w, final int h) {
    	if (D) Log.d(TAG, "surfaceChanged");
    	_controller.startPreview();
    }

}