package co.smartreceipts.android.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.util.Log;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.persistence.PersistenceManager;
import wb.android.storage.StorageManager;

/**
 * This enables us to handle uncaught exceptions in a customizable manner. This is needed to fix a bug with Google Play
 * Services
 * 
 * @author Will Baumann
 */
public class WBUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private static final String TAG = WBUncaughtExceptionHandler.class.getSimpleName();
	private static final String LOG_FILE = "crash_log.txt";

	private static boolean sIsInitialized = false;
	private final UncaughtExceptionHandler mUncaughtExceptionHandlerParent;

	public synchronized static void initialize() {
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
		try {
			final StorageManager storageManager = SmartReceiptsApplication.getInstance().getPersistenceManager().getStorageManager();
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			throwable.printStackTrace(printWriter);
			storageManager.appendTo(LOG_FILE, stringWriter.toString());
		} catch (Throwable t) {
			// Silently swallow any issues here to avoid a recursive crash loop
		}
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
