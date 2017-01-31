package co.smartreceipts.android.ocr.info;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;

public class OcrInformationalInteractor {

    private final Analytics mAnalytics;
    private final NavigationHandler mNavigationHandler;

    public OcrInformationalInteractor(@NonNull Analytics analytics, @NonNull NavigationHandler navigationHandler) {
        mAnalytics = Preconditions.checkNotNull(analytics);
        mNavigationHandler = Preconditions.checkNotNull(navigationHandler);
    }

    public boolean submitQuestionnaire() {
        mAnalytics.record(Events.Ocr.OcrQuestionnaireSubmit);
        return mNavigationHandler.navigateBack();
    }

    public boolean dismissQuestionnaire() {
        mAnalytics.record(Events.Ocr.OcrQuestionnaireDismiss);
        return mNavigationHandler.navigateBack();
    }

}
