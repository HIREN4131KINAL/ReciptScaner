package wb.android.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class JPGCallback implements Camera.PictureCallback {

	private static final boolean D = true;
	private static final String TAG = "JPGCallback";
	
	private final Handler _handler;
	
	JPGCallback(final Handler handler) {
		_handler = handler;
	}
	
	@Override
	public void onPictureTaken(final byte[] data, final Camera camera) {
		if (D) Log.d(TAG, "Beginning Picture Capture");
		if (data == null)
			Log.e(TAG, "JPG data is not present");
		else {
			Message message = _handler.obtainMessage(CameraController.REQUEST_PICTURE, data);
			message.sendToTarget();
		}
	}

}
