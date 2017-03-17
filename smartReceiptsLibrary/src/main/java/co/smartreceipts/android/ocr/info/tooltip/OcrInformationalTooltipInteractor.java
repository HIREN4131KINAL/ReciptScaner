package co.smartreceipts.android.ocr.info.tooltip;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.utils.log.Logger;
import rx.Observable;
import rx.functions.Action1;
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
                })
                .doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        mAnalytics.record(Events.Ocr.OcrQuestionnaireTooltipShown);
                    }
                });
    }

    public void dismissTooltip() {
        Logger.info(this, "Dismissing OCR Tooltip");
        mStateTracker.setShouldShowPreReleaseQuestions(false);
        mAnalytics.record(Events.Ocr.OcrQuestionnaireTooltipDismiss);
    }

    public void showOcrInformation() {
        Logger.info(this, "Displaying OCR Fragment");
        mNavigationHandler.navigateToOcrInfomationFragment();
        mAnalytics.record(Events.Ocr.OcrQuestionnaireTooltipOpen);
        mStateTracker.setShouldShowPreReleaseQuestions(false);
    }
}
