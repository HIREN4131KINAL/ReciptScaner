package co.smartreceipts.android.rating;

import android.support.annotation.VisibleForTesting;

import rx.Single;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AppRatingManager {

    // AppRating (Use a combination of launches and a timer for the app rating
    // to ensure that we aren't prompting new users too soon
    private static final int LAUNCHES_UNTIL_PROMPT = 15;
    private static final int DAYS_UNTIL_PROMPT = 7;

    private static AppRatingManager sInstance;

    private AppRatingStorage mAppRatingStorage;

    public static AppRatingManager getInstance(AppRatingStorage ratingStorage) {
        if (sInstance == null) {
            sInstance = new AppRatingManager(ratingStorage);
        }
        return sInstance;
    }

    private AppRatingManager(AppRatingStorage appRatingStorage) {
        this.mAppRatingStorage = appRatingStorage;

        setCustomUncaughtExceptionHandler();
    }

    @VisibleForTesting
    public static void clearStateForTesting() {
        sInstance = null;
    }

    public Single<Boolean> checkIfNeedToAskRating() {
        return mAppRatingStorage.readAppRatingData()
                .doOnSuccess(new Action1<AppRatingModel>() {
                    @Override
                    public void call(AppRatingModel appRatingModel) {
                        mAppRatingStorage.incrementLaunchCount();
                        if (appRatingModel.getLaunchCount() == 1) {
                            mAppRatingStorage.saveInstallTime();
                        }
                    }
                }).map(new Func1<AppRatingModel, Boolean>() {
            @Override
            public Boolean call(AppRatingModel appRatingModel) {
                if (appRatingModel.canShow() && !appRatingModel.isCrashOccurred()) {
                    // Check if we've reached a rating event
                    final long daysToMillis = 24 * 60 * 60 * 1000; // 24h/d * 60m/h * 60s/m * 1000millis/s
                    if (appRatingModel.getLaunchCount() >= LAUNCHES_UNTIL_PROMPT + appRatingModel.getAdditionalLaunchThreshold() &&
                            (System.currentTimeMillis() - appRatingModel.getInstallTime()) / daysToMillis >= DAYS_UNTIL_PROMPT) {
                        return true;
                    }
                }
                return false;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void dontShowRatingPromptAgain() {
        mAppRatingStorage.setDontShowRatingPromptMore();
    }

    public void prorogueRatingPrompt() {
        mAppRatingStorage.prorogueRatingPrompt(LAUNCHES_UNTIL_PROMPT);
    }

    private void setCustomUncaughtExceptionHandler() {
        Thread.UncaughtExceptionHandler exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (!(exceptionHandler instanceof RatingUncaughtExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new RatingUncaughtExceptionHandler(mAppRatingStorage, exceptionHandler));
        }
    }

    private static final class RatingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private final AppRatingStorage sAppRatingStorage;
        private final Thread.UncaughtExceptionHandler sUncaughtExceptionHandler;

        public RatingUncaughtExceptionHandler(AppRatingStorage appRatingStorage, Thread.UncaughtExceptionHandler exceptionHandler) {
            sAppRatingStorage = appRatingStorage;
            sUncaughtExceptionHandler = exceptionHandler;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            sAppRatingStorage.crashOccurred();
            sUncaughtExceptionHandler.uncaughtException(thread, throwable);
        }

    }
}
