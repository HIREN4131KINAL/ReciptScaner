package wb.android.licensing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;
//import com.android.vending.licensing.*;

public final class LicensingHandler {
/*	
	//Debugging
	private static final boolean D = true;
	private static final String TAG = "LicensingHandler";
	
	//Licensing state (probably delete these)
	private static boolean licensed = true;
	private static boolean didCheck = false;
	private static boolean checkingLicense = false;
	
	private static final String BASE64_PUBLIC_KEY;
	private static final byte[] SALT;
	private final LicenseCheckerCallback _licenseCheckerCallback;
	private final LicenseChecker _checker;
	SharedPreferences prefs;
	
	public LicensingHandler(LicenseCheckerCallback callback, String base64PublicKey) {
		_licenseCheckerCallback = callback;
		BASE64_PUBLIC_KEY = base64PublicKey;
		// REPLACE WITH YOUR OWN SALT , THIS IS FROM EXAMPLE
		SALT = new byte[] {-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89};
	}
	
	public LicensingHandler(LicenseCheckerCallback callback, String base64PublicKey, byte[] salt) {
		_licenseCheckerCallback = callback;
		BASE64_PUBLIC_KEY = base64PublicKey;
		SALT = salt;
	}
 
	public final void checkLicense(ContentResolver contentResolver) {
		if(D) Log.i(TAG, "Checking License");
		
		// Try to use more data here. ANDROID_ID is a single point of attack.
		String deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
		
		// Library calls this when it's done.
		_licenseCheckerCallback = new MyLicenseCheckerCallback();
		
		// Construct the LicenseChecker with a policy.
		_checker = new LicenseChecker(this, new ServerManagedPolicy(this,new AESObfuscator(SALT, getPackageName(), deviceId)), BASE64_PUBLIC_KEY);
		didCheck = false;
		checkingLicense = true;
		_checker.checkAccess(_licenseCheckerCallback);
		
		
		 // CAN I Destroy the checker here??
		 
	}
	
	public final void onDestroy() {
		if (_checker != null)
			_checker.onDestroy();
	}
 
/*
	protected class MyLicenseCheckerCallback implements LicenseCheckerCallback {
		public void allow() {
		if(D) Log.i(TAG, "allow");
			if (isFinishing()) // Don't update UI if Activity is finishing.
				return;
			
			// Should allow user access.
			displayResult("Allow the user access");
			licensed = true;
			checkingLicense = false;
			didCheck = true;
		}
 
		public void dontAllow() {
			if(D) Log.i(TAG, "dontAllow");
			if (isFinishing()) // Don't update UI if Activity is finishing.
				return;
			
			displayResult("Don\'t allow the user access");
			licensed = false;
			// Should not allow access. In most cases, the app should assume
			// the user has access unless it encounters this. If it does,
			// the app should inform the user of their unlicensed ways
			// and then either shut down the app or limit the user to a
			// restricted set of features.
			// In this example, we show a dialog that takes the user to Market.
			checkingLicense = false;
			didCheck = true;
			showDialog(0);
		}
	 
		public void applicationError(ApplicationErrorCode errorCode) {
			if(D) Log.i(TAG, "error: " + errorCode);
			if (isFinishing()) // Don't update UI if Activity is finishing.
				return;
			
			licensed = false;
			// This is a polite way of saying the developer made a mistake
			// while setting up or calling the license checker library.
			// Please examine the error code and fix the error.
			String result = String.format("Application error: %1$s", errorCode);
			checkingLicense = false;
			didCheck = true;
			//displayResult(result);
			showDialog(0);
		}
	}
*/
}