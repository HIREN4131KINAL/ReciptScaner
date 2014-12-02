package co.smartreceipts.android.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import android.util.Log;

/**
 * This enables us to handle uncaught exceptions in a customizable manner. This is needed to fix a bug with Google Play
 * Services
 * 
 * @author Will Baumann
 */
public class WBUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private static final String TAG = WBUncaughtExceptionHandler.class.getSimpleName();

	private static boolean sIsInitialized = false;
	private final UncaughtExceptionHandler mUncaughtExceptionHandlerParent;

	public synchronized static final void initialize() {
		if (!sIsInitialized) {
			sIsInitialized = true;
			final WBUncaughtExceptionHandler uncaughtExceptionHandler = new WBUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
		}
	}

	private WBUncaughtExceptionHandler() {
		mUncaughtExceptionHandlerParent = Thread.getDefaultUncaughtExceptionHandler();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		if (thread.getName().startsWith("AdWorker")) {
			// Solves a bug with Google Play Services:
			// http://stackoverflow.com/questions/24457689/google-play-services-5-0-77
			Log.w(TAG, "AdWorker thread threw an exception", throwable);
		}
		else {
			mUncaughtExceptionHandlerParent.uncaughtException(thread, throwable);
		}
	}

}
