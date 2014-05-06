/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wb.android.google.camera;

import android.hardware.Camera.Parameters;
import android.util.Log;
import android.widget.Toast;
import wb.android.google.camera.app.GalleryApp;

public class CameraErrorCallback implements android.hardware.Camera.ErrorCallback {
	
    private static final String TAG = "CameraErrorCallback";
    
    private final PhotoModule mPhotoModule;
    
    public CameraErrorCallback(PhotoModule photoModule) {
    	mPhotoModule = photoModule;
    }

    @Override
    public void onError(int error, android.hardware.Camera camera) {
        Log.e(TAG, "Got camera error callback. error=" + error);
        if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED) {
            // We are not sure about the current state of the app (in preview or
            // snapshot or recording). Closing the app is better than creating a
            // new Camera object.
            //throw new RuntimeException("Media server died.");
        	Toast.makeText(mPhotoModule.getActivity(), mPhotoModule.getActivity().getString(R.string.smartreceipts_error_media_server_died), Toast.LENGTH_LONG).show();
        	((GalleryApp)mPhotoModule.getActivity().getApplication()).uploadError("Media Server Died - State: " + mPhotoModule.getCameraState());
        	mPhotoModule.getActivity().finish();
        }
    }
    
    public void onSRCameraError(Exception e) {
    	//Errors occur with cameraState=1, focusState=3
    	Toast.makeText(mPhotoModule.getActivity(), mPhotoModule.getActivity().getString(R.string.smartreceipts_error_photo_error), Toast.LENGTH_LONG).show();
    	Parameters p = mPhotoModule.getCameraParameters();
    	((GalleryApp)mPhotoModule.getActivity().getApplication()).uploadError(e.toString() + 
    			"; Fl:" + p.getFlashMode() + "; E:" + p.getExposureCompensation() + "; Fo:" + p.getFocusMode() + 
    			"; A:" + p.getAntibanding() + "; C:" + p.getColorEffect() + "; Fl:" + p.getFocalLength() + 
    			"; S:" + p.getSceneMode() + "; W:" + p.getWhiteBalance()); 
    	mPhotoModule.getActivity().finish();
    }
}
