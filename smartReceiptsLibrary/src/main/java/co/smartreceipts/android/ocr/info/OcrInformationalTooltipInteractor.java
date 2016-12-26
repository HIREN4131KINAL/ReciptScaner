package co.smartreceipts.android.ocr.info;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.analytics.Analytics;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class OcrInformationalTooltipInteractor {

    private final FragmentManager mFragmentManager;
    private final Analytics mAnalytics;
    private final OcrInformationalTooltipStateTracker mStateTracker;

    public OcrInformationalTooltipInteractor(@NonNull Context context, @NonNull FragmentManager fragmentManager, @NonNull Analytics analytics) {
        this(fragmentManager, analytics, new OcrInformationalTooltipStateTracker(context));
    }

    public OcrInformationalTooltipInteractor(@NonNull FragmentManager fragmentManager, @NonNull Analytics analytics, @NonNull OcrInformationalTooltipStateTracker stateTracker) {
        mAnalytics = Preconditions.checkNotNull(analytics);
        mFragmentManager = Preconditions.checkNotNull(fragmentManager);
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
    }

    public void dismissTooltip() {
        mStateTracker.setShouldShowPreReleaseQuestions(false);
    }

    public void showOcrInformation() {
        // TODO: mStateTracker.setShouldShowPreReleaseQuestions(false);
    }
}
