package co.smartreceipts.android.rating;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.Callable;

import rx.Single;

public class AppRatingStorageImpl implements AppRatingStorage {

    private Context mAppContext;

    private static final class Keys {
        /**
         * Key to get rating preferences
         */
        private static final String RATING_PREFERENCES = "Smart Receipts rating";
        /**
         * Key to track user preference about no longer showing rating window
         */
        private static final String DONT_SHOW = "dont_show";

        /**
         * Key to track how many times the user has launched the application via the {@link AppRating#onLaunch()} method
         */
        private static final String LAUNCH_COUNT = "launches";

        /**
         * Key to track if the users wishes to be reminded later
         */
        private static final String ADDITIONAL_LAUNCH_THRESHOLD = "threshold";

        /**
         * Key to track the first call of {@link AppRating#onLaunch()} method in millis
         */
        private static final String INSTALL_TIME_MILLIS = "days";

        /**
         * Key to track if the application crashed at a prior date
         */
        private static final String CRASH_OCCURRED = "hide_on_crash";
    }

    // will be called from Activity
    public AppRatingStorageImpl(Context context) {
        mAppContext = context.getApplicationContext();
    }

    @Override
    // next -> Rx
    public Single<AppRatingModel> readAppRatingData() {
        return Single.fromCallable(new Callable<AppRatingModel>() {
            @Override
            public AppRatingModel call() throws Exception {
                SharedPreferences sharedPreferences = getSharedPreferences();

                // Set up some vars
                long now = System.currentTimeMillis();
                // Get our current values

                boolean canShow = !sharedPreferences.getBoolean(Keys.DONT_SHOW, false);
                boolean crashOccurred = sharedPreferences.getBoolean(Keys.CRASH_OCCURRED, false);
                int launchCount = sharedPreferences.getInt(Keys.LAUNCH_COUNT, 0) + 1;
                int additionalLaunchThreshold = sharedPreferences.getInt(Keys.ADDITIONAL_LAUNCH_THRESHOLD, 0);
                long installTime = sharedPreferences.getLong(Keys.INSTALL_TIME_MILLIS, now);
                boolean justInstalled = installTime == now;

                return new AppRatingModel(canShow, crashOccurred, launchCount, additionalLaunchThreshold,
                        installTime, justInstalled);
            }
        });
    }

    @Override
    public void incrementLaunchCount() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Keys.LAUNCH_COUNT, sharedPreferences.getInt(Keys.LAUNCH_COUNT, 0) + 1);
        editor.apply();
    }

    @Override
    public void saveInstallTime() {
        SharedPreferences.Editor editor = getPreferencesEditor();
        editor.putLong(Keys.INSTALL_TIME_MILLIS, System.currentTimeMillis());
        editor.apply();
    }

    @Override
    public void setDontShowRatingPromptMore() {
        SharedPreferences.Editor editor = getPreferencesEditor();
        editor.putBoolean(Keys.DONT_SHOW, true);
        editor.apply();
    }

    @Override
    public void crashOccurred() {
        SharedPreferences.Editor editor = getPreferencesEditor();
        editor.putBoolean(Keys.CRASH_OCCURRED, true);
        editor.apply();
    }

    private SharedPreferences.Editor getPreferencesEditor() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        return sharedPreferences.edit();
    }

    private SharedPreferences getSharedPreferences() {
        return mAppContext.getSharedPreferences(Keys.RATING_PREFERENCES, Context.MODE_PRIVATE);
    }

}
