package co.smartreceipts.android.ocr.info.tooltip;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class OcrInformationalTooltipInteractor {

    private final NavigationHandler mNavigationHandler;
    private final Analytics mAnalytics;
    private final OcrInformationalTooltipStateTracker mStateTracker;

    public OcrInformationalTooltipInteractor(@NonNull Context context, @NonNull NavigationHandler navigationHandler, @NonNull Analytics analytics) {
        this(navigationHandler, analytics, new OcrInformationalTooltipStateTracker(context));
    }

    public OcrInformationalTooltipInteractor(@NonNull NavigationHandler navigationHandler, @NonNull Analytics analytics, @NonNull OcrInformationalTooltipStateTracker stateTracker) {
        mNavigationHandler = Preconditions.checkNotNull(navigationHandler);
        mAnalytics = Preconditions.checkNotNull(analytics);
        mStateTracker = Preconditions.checkNotNull(stateTracker);
    }

    public Observable<Boolean> getShowQuestionTooltipStream() {
        return mStateTracker.shouldShowPreReleaseQuestions()
                .subscribeOn(Schedulers.io())
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean shouldShow) {
                        return shouldShow;
                    }
                });
                // TODO: Analytics
    }

    public void dismissTooltip() {
        mStateTracker.setShouldShowPreReleaseQuestions(false);
    }

    public void showOcrInformation() {
        mNavigationHandler.navigateToOcrInfomationFragment();
        // TODO: mStateTracker.setShouldShowPreReleaseQuestions(false);
    }
}
