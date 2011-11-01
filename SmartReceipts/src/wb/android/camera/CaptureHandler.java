package wb.android.camera;

import android.os.Handler;
import android.os.Message;

final class CaptureHandler extends Handler {
	
	//Instance vars
	private final CameraActivity _cameraActivity;
	
	CaptureHandler(final CameraActivity activity) {
		_cameraActivity = activity;
	}
	
	@Override
	public final void handleMessage(final Message msg) {
		switch (msg.what) {
			case CameraController.FOCUS_SUCCEEDED:
				_cameraActivity.autoFocusSuccessCallback();
				break;
			case CameraController.FOCUS_FAILED:
				_cameraActivity.autoFocusFailureCallback();
				break;
			case CameraController.REQUEST_PICTURE:
				_cameraActivity.pictureTakenCallback((byte[]) msg.obj);
				break;
		}
		super.handleMessage(msg);
	}
	
}
