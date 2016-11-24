package co.smartreceipts.android.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.utils.log.Logger;

/**
 * This enables us to handle uncaught exceptions in a customizable manner. This is needed to fix a bug with Google Play
 * Services
 * 
 * @author Will Baumann
 */
public class WBUncaughtExceptionHandler implements UncaughtExceptionHandler {

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
			final File crashFile = new File(SmartReceiptsApplication.getInstance().getExternalFilesDir(null), LOG_FILE);
			final StringWriter stringWriter = new StringWriter();
			final PrintWriter printWriter = new PrintWriter(stringWriter);
			throwable.printStackTrace(printWriter);
			PrintWriter appendWriter = null;
			try {
				appendWriter = new PrintWriter(new BufferedWriter(new FileWriter(crashFile, true)));
				appendWriter.println(stringWriter.toString());
			}
			catch (IOException e) {
				Logger.error(this, "Caught IOException in uncaughtException", e);
			}
			finally {
				if (appendWriter != null) {
					appendWriter.close();
				}
			}
		} catch (Throwable t) {
			// Silently swallow any issues here to avoid a recursive crash loop
		}
		if (thread.getName().startsWith("AdWorker")) {
			// Solves a bug with Google Play Services:
			// http://stackoverflow.com/questions/24457689/google-play-services-5-0-77
			Logger.warn(this, "AdWorker thread threw an exception", throwable);
		}
		else {
			mUncaughtExceptionHandlerParent.uncaughtException(thread, throwable);
		}
	}

}
