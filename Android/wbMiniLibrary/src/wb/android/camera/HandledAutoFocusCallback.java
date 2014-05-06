package wb.android.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;

final class HandledAutoFocusCallback implements Camera.AutoFocusCallback{

	private static final String TAG = "HandledAutoFocusCallback";
	
	private final Handler _handler;
	
	HandledAutoFocusCallback(final Handler handler) {
		_handler = handler;
	}
	
	@Override
	public final void onAutoFocus(final boolean success, final Camera camera) {
		if (success) {
			_handler.sendEmptyMessage(CameraController.FOCUS_SUCCEEDED);
		}
		else {
			Log.e(TAG,"Failed to Focus the Camera");
			_handler.sendEmptyMessage(CameraController.FOCUS_FAILED);
		}
	}

}
