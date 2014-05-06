package wb.android.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.WeakReference;

import wb.android.R;
import wb.android.workers.AsyncTask;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;

/**
 * The AppRating class simplifies the process of tracking common rating functionality
 * like the number of launches before prompting for rating, the number of days before
 * prompting for rating, and whether to prompt if the app has previously crashed
 *
 * @author Will Baumann
 */
public class AppRating implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

	/**
	 * Users will be reminded to rate the app again by this many future launches by default
	 */
	private static final int INCREASE_THRESHOLD_ON_REMIND_LATER = 10;

	private static final class Keys {
		/**
		 * Key to track user preference about no longer showing rating window
		 */
		private static final String KEY_DONT_SHOW = "dont_show";

		/**
		 * Key to track how many times the user has launched the application via the
		 * {@link AppRating#onLaunch()} method
		 */
		private static final String KEY_LAUNCH_COUNT = "launches";

		/**
		 * Key to track what the current launch threshold is (this value is not static,
		 * since it may increase if the users wishes to be reminded later)
		 */
		private static final String KEY_LAUNCH_THRESHOLD = "threshold";

		/**
		 * Key to track the first call of {@link AppRating#onLaunch()} method in millis
		 */
		private static final String KEY_INSTALL_TIME_MILLIS = "days";

		/**
		 * Key to track if the application crashed at a prior date
		 */
		private static final String KEY_CRASH_OCCURED = "hide_on_crash";
	}

	private final WeakReference<Context> mContext;
	private String mPackageName;
	private int mMinimumLaunchesTilPrompt, mMinimumDaysUntilPrompt;
	private boolean mHideIfAppCrashed, mShowDialog;
	private int mDialogTitleRes, mDialogMessageRes, mDialogNegativeButtonRes;
	private final int mDialogNeutralButtonRes;
	private int mDialogPositiveButtonRes;
	private final Utilities mUtilities;
	private Listener mListener;

	/**
	 * Nested interface to handle callbacks when a rating event is reached
	 *
	 */
	public interface Listener {
		/**
		 * Called whenever the user receives a rating event
		 *
		 * @param utilities - a {@link AppRating.Utilities} instance to provide useful methods
		 */
		public void onRatingEvent(AppRating.Utilities utilities);
	}

	/**
	 * Nested interface to provide simple utilities for classes the
	 * require additional functionality via the {@link AppRating.Listener}
	 * interface.
	 */
	public interface Utilities {
		/**
		 * Call this method in order to launch the rating intent for the end user.
		 * In practice, this will open the Google Play Store (or equivalent)
		 */
		public void launchRatingIntent();

		/**
		 * Prevents future rating events again
		 */
		public void doNotShowRatingPromptAgain();

		/**
		 * Delays the rating dialog until a future date (during when it is more
		 * amenable to the user).
		 *
		 * @param launchesUntilRating - the number of launches in the future to wait
		 */
		public void rateLater(int launchesUntilRating);
	}

	private class UtiltiesImpl implements Utilities {

		@Override
		public void launchRatingIntent() {
			Context context = getContext();
			if (context != null) {
				context.startActivity(getRatingIntent(context, mPackageName));
				doNotShowRatingPromptAgain();
			}
		}

		@Override
		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		public void doNotShowRatingPromptAgain() {
			final SharedPreferences sharedPreferences = getSharedPreferences();
			if (sharedPreferences != null) {
				final SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean(Keys.KEY_DONT_SHOW, true);
	    	    if (Utils.ApiHelper.hasGingerbread()) {
	    	    	editor.apply();
	    	    }
	        	else {
	        		editor.commit();
	        	}
			}
		}

		@Override
		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		public void rateLater(int launchesUntilRating) {
			final SharedPreferences sharedPreferences = getSharedPreferences();
			if (sharedPreferences != null) {
				final int newLaunchThreshold = sharedPreferences.getInt(Keys.KEY_LAUNCH_THRESHOLD, mMinimumLaunchesTilPrompt) + launchesUntilRating;
				final SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putInt(Keys.KEY_LAUNCH_THRESHOLD, newLaunchThreshold);
				if (Utils.ApiHelper.hasGingerbread()) {
	    	    	editor.apply();
	    	    }
	        	else {
	        		editor.commit();
	        	}
			}
		}

	}

	public AppRating(Context context) {
		mContext = new WeakReference<Context>(context);
		mUtilities = new UtiltiesImpl();
		// Set some reasonable defaults below
		mShowDialog = true;
		mDialogTitleRes = R.string.apprating_dialog_title;
		mDialogMessageRes = R.string.apprating_dialog_message;
		mDialogNegativeButtonRes = R.string.apprating_dialog_negative;
		mDialogNeutralButtonRes = R.string.apprating_dialog_neutral;
		mDialogPositiveButtonRes = R.string.apprating_dialog_positive;
		mPackageName = context.getPackageName();
	}

	/**
	 * Instantiates a {@link AppRating} instance
	 *
	 * @param context - the current {@link Context}
	 * @return a new {@link AppRating} instance
	 */
	public static AppRating initialize(Context context) {
		return new AppRating(context);
	}

	/**
	 * Defines how many times the application must be launched until the user is prompted
	 * to rate the application
	 *
	 * @param minimumLaunchesUntilPrompt - The minimum number of launches until prompt
	 * @return This {@link AppRating} instance
	 */
	public AppRating setMinimumLaunchesUntilPrompt(int minimumLaunchesUntilPrompt) {
		mMinimumLaunchesTilPrompt = minimumLaunchesUntilPrompt;
		return this;
	}

	/**
	 * Defines how many days the user should have the app installed for before prompting
	 * them to rate the application
	 *
	 * @param minimumDaysUntilPrompt - The minimum number of days until prompt
	 * @return This {@link AppRating} instance
	 */
	public AppRating setMinimumDaysUntilPrompt(int minimumDaysUntilPrompt) {
		mMinimumDaysUntilPrompt = minimumDaysUntilPrompt;
		return this;
	}

	/**
	 * Defines if the rating dialog should be shown in the event that the app crashed at
	 * an earlier point for this user
	 *
	 * @param hideIfAppCrashed - <code>true</code> if the rating should not be shown, <code>false</code> otherwise
	 * @return This {@link AppRating} instance
	 */
	public AppRating hideIfAppCrashed(boolean hideIfAppCrashed) {
		mHideIfAppCrashed = hideIfAppCrashed;
		return this;
	}

	/**
	 * Defines the package name for this application (useful if the provided context does not
	 * share the same package name as the application)
	 *
	 * @param packageName - the name of the application package
	 * @return This {@link AppRating} instance
	 */
	public AppRating setPackageName(String packageName) {
		mPackageName = packageName;
		return this;
	}

	/**
	 * Defines whether or not the rating dialog should be shown.
	 *
	 * @param show - <code>true</code> if the rating dialog should be shown
	 * @return This {@link AppRating} instance
	 */
	public AppRating showDialog(boolean show) {
		mShowDialog = show;
		return this;
	}

	/**
	 * Sets the string id that will be used to set the application rating dialog prompt
	 * via the {@link AlertDialog.Builder#setTitle(int)} method.
	 * This method also sets {@link AppRating#showDialog(boolean)} to <code>true</code>
	 *
	 * @param stringId - the string resource Id
	 * @return This {@link AppRating} instance
	 */
	public AppRating setDialogTitle(int stringId) {
		mDialogTitleRes = stringId;
		showDialog(true);
		return this;
	}

	/**
	 * Sets the string id that will be used to set the application rating dialog prompt
	 * via the {@link AlertDialog.Builder#setMessage(int)} method.
	 * This method also sets {@link AppRating#showDialog(boolean)} to <code>true</code>
	 *
	 * @param stringId - the string resource Id
	 * @return This {@link AppRating} instance
	 */
	public AppRating setDialogMessage(int stringId) {
		mDialogMessageRes = stringId;
		showDialog(true);
		return this;
	}

	/**
	 * Sets the string id that will be used to set the application rating dialog prompt
	 * via the {@link AlertDialog.Builder#setPositiveButton(int, android.content.DialogInterface.OnClickListener)} method.
	 * This method also sets {@link AppRating#showDialog(boolean)} to <code>true</code>
	 *
	 * @param stringId - the string resource Id
	 * @return This {@link AppRating} instance
	 */
	public AppRating setDialogNegativeButton(int stringId) {
		mDialogNegativeButtonRes = stringId;
		showDialog(true);
		return this;
	}

	/**
	 * Sets the string id that will be used to set the application rating dialog prompt
	 * via the {@link AlertDialog.Builder#setNegativeButton(int, android.content.DialogInterface.OnClickListener)} method.
	 * This method also sets {@link AppRating#showDialog(boolean)} to <code>true</code>
	 *
	 * @param stringId - the string resource Id
	 * @return This {@link AppRating} instance
	 */
	public AppRating setDialogPostiveButton(int stringId) {
		mDialogPositiveButtonRes = stringId;
		showDialog(true);
		return this;
	}

	/**
	 * Call this to initialize the actual check for application rating. Depending on the
	 * flags set earlier, this may caused a rating dialog to be launched
	 */
	public void onLaunch() {
		new SharedPreferencesLoader(this).execute(new Void[0]);
	}

	/**
	 * Call this to initialize the actual check for application rating. Depending on the
	 * flags set earlier, this may caused a rating dialog to be launched
	 *
	 * @param sharedPreferences - the desired set of {@link SharedPreferences} to use
	 */
	public void onLaunch(SharedPreferences sharedPreferences) {
		onSharedPreferencesLoaded(sharedPreferences);
	}

	/**
	 * Primarily intended for debugging purposes but may also be used to force a rating
	 * event regardless of whether any rating prerequisites have evaluated to true
	 *
	 * @param onlyShowIfUserHasNotDismissedBefore - set to <code>true</code> if this
	 * dialog should not be show if the user has previously dismissed this dialog (either
	 * by rating it or asking to never be shown again). Set to <code>false</code> if the
	 * user should be prompted regardless
	 */
	public void forceRatingEvent(boolean onlyShowIfUserHasNotDismissedBefore) {
		SharedPreferences sharedPreferences = getSharedPreferences();
		if (onlyShowIfUserHasNotDismissedBefore) {
			if (sharedPreferences != null) {
				if (!sharedPreferences.getBoolean(Keys.KEY_DONT_SHOW, false)) {
					onRatingEvent(sharedPreferences.edit());
				}
			}
			else {
				onRatingEvent(null);
			}
		}
		else {
			if (sharedPreferences != null) {
				onRatingEvent(sharedPreferences.edit());
			}
			else {
				onRatingEvent(null);
			}
		}
	}

	/**
	 * Clears all sharedPreference information for this {@link AppRating} instance
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void clear() {
		SharedPreferences sharedPreferences = getSharedPreferences();
		if (sharedPreferences != null) {
			final SharedPreferences.Editor editor = sharedPreferences.edit();
			if (Utils.ApiHelper.hasGingerbread()) {
    	    	editor.clear().apply();
    	    }
        	else {
        		editor.clear().commit();
        	}
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		mUtilities.doNotShowRatingPromptAgain();
		dialog.dismiss();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			mUtilities.launchRatingIntent();
		}
		else if (which == DialogInterface.BUTTON_NEUTRAL) {
			mUtilities.rateLater(INCREASE_THRESHOLD_ON_REMIND_LATER);
		}
		else if (which == DialogInterface.BUTTON_NEGATIVE) {
			mUtilities.doNotShowRatingPromptAgain();
		}
		else {
			return;
		}
		dialog.dismiss();
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void onSharedPreferencesLoaded(SharedPreferences sharedPreferences) {
		if (sharedPreferences == null) {
			return;
		}
		else {
			if (sharedPreferences.getBoolean(Keys.KEY_DONT_SHOW, false)) {
	        	return; //Return now if we're not showing this prompt any more
			}
			if (sharedPreferences.getBoolean(Keys.KEY_CRASH_OCCURED, false) && mHideIfAppCrashed) {
				return; //Return now if a crashed occurred (and we're not showing)
			}

			// Set up some vars
			final long now = System.currentTimeMillis();
			if (mHideIfAppCrashed) {
				UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
				if (!(exceptionHandler instanceof MyUncaughtExceptionHandler)) {
					Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(sharedPreferences, exceptionHandler));
				}
			}

			// Get our current values
			final int launchCount = sharedPreferences.getInt(Keys.KEY_LAUNCH_COUNT, 0) + 1;
			final int launchThreshold = sharedPreferences.getInt(Keys.KEY_LAUNCH_THRESHOLD, mMinimumLaunchesTilPrompt);
			final long installTime = sharedPreferences.getLong(Keys.KEY_INSTALL_TIME_MILLIS, now);

			// Make our updates
			SharedPreferences.Editor editor = sharedPreferences.edit();
			if (installTime == now) {
				editor.putLong(Keys.KEY_INSTALL_TIME_MILLIS, now); // Put in the first launch time
			}
			if (launchCount < launchThreshold) {
				editor.putInt(Keys.KEY_LAUNCH_COUNT, launchCount); // Update our count as long as it's less than our threshold
			}
			if (Utils.ApiHelper.hasGingerbread()) {
	        	editor.apply(); // Same as commit but async
			}
	    	else {
	    		editor.commit();
	    	}

			// Check if we've reached a rating event
			final long daysToMillis = 24 * 60 * 60 * 1000; // 24h/d * 60m/h * 60s/m * 1000millis/s
			if (launchCount > launchThreshold && (now - installTime)/daysToMillis >= mMinimumDaysUntilPrompt) {
				onRatingEvent(editor);
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void onRatingEvent(SharedPreferences.Editor editor) {
		Context context = getContext();
		if (editor == null || context == null) {
			return; // Invalid state. Try next time
		}

		if (mShowDialog) {
			final String appName = getApplicationName(context);
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(context.getString(mDialogTitleRes, appName))
				   .setMessage(context.getString(mDialogMessageRes, appName))
				   .setNegativeButton(context.getString(mDialogNegativeButtonRes, appName), this)
				   .setNeutralButton(context.getString(mDialogNeutralButtonRes, appName), this)
				   .setPositiveButton(context.getString(mDialogPositiveButtonRes, appName), this)
				   .show();
		}

		if (mListener != null) {
			mListener.onRatingEvent(mUtilities);
		}
	}

	private Context getContext() {
		return mContext.get();
	}

	/**
	 * @return - our internal instance of {@link SharedPreferences}. This may return null
	 * if we no longer have a valid {@link Context}
	 */
	private SharedPreferences getSharedPreferences() {
		Context context = getContext();
		if (context == null) {
			return null;
		}
		else {
			return context.getSharedPreferences(getApplicationName(context) + "rating", 0);
		}
	}

	private static class SharedPreferencesLoader extends AsyncTask<Void, Void, SharedPreferences> {

		private final WeakReference<AppRating> sAppRating;

		public SharedPreferencesLoader(AppRating appRating) {
			sAppRating = new WeakReference<AppRating>(appRating);
		}

		@Override
		protected SharedPreferences doInBackground(Void... args) {
			AppRating appRating = sAppRating.get();
			if (appRating == null) {
				return null;
			}
			else {
				return appRating.getSharedPreferences();
			}
		}

		@Override
		protected void onPostExecute(SharedPreferences result) {
			AppRating appRating = sAppRating.get();
			if (appRating != null) {
				appRating.onSharedPreferencesLoaded(result);
			}
		}

	}

	private static final String GOOGLE = "com.android.vending";
	private static final String AMAZON = "com.amazon.venezia";
    public static final Intent getRatingIntent(Context context, String packageName) {
    	String name = context.getPackageManager().getInstallerPackageName(packageName);
    	if (GOOGLE.equals(name)) {
    		return new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
    	}
    	else if (AMAZON.equals(name)) {
    		return new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=" + packageName));
    	}
    	else {
    		//Default to Google... May lead to a crash if user does not have this installed
    		return new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
    	}
    }

	/**
	* @param context - The {@link Context} of the current application
	*
	* @return The name of the current application or <code>null</code> if empty
	*/
	public static final String getApplicationName(Context context) {
		final PackageManager packageManager = context.getPackageManager();
		try {
			ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
			return packageManager.getApplicationLabel(applicationInfo).toString();
		} catch (final NameNotFoundException e) {
			return null;
		}
	}

    private static final class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

    	private final WeakReference<SharedPreferences> sSharedPreferences;
    	private final UncaughtExceptionHandler sUncaughtExceptionHandler;

    	public MyUncaughtExceptionHandler(SharedPreferences sharedPreferences, UncaughtExceptionHandler exceptionHandler) {
			sSharedPreferences = new WeakReference<SharedPreferences>(sharedPreferences);
			sUncaughtExceptionHandler = exceptionHandler;
		}

		@Override
		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		public void uncaughtException(Thread thread, Throwable throwable) {
			SharedPreferences sharedPreferences = sSharedPreferences.get();
			if (sharedPreferences != null) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean(AppRating.Keys.KEY_CRASH_OCCURED, true);
				if (Utils.ApiHelper.hasGingerbread()) {
					editor.apply();
				}
				else {
					editor.commit();
				}
			}
			sUncaughtExceptionHandler.uncaughtException(thread, throwable);
		}

    }

}