package co.smartreceipts.android.ocr.widget;

import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Event;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.workers.EmailAssistant;

public class OcrInformationalInteractor {

    private final Analytics mAnalytics;
    private final FragmentActivity mActivity;
    private final NavigationHandler mNavigationHandler;

    private Event mQuestion1Event = null;
    private Event mQuestion2Event = null;

    public OcrInformationalInteractor(@NonNull Analytics analytics, @NonNull FragmentActivity activity) {
        this(analytics, activity, new NavigationHandler(activity));
    }

    public OcrInformationalInteractor(@NonNull Analytics analytics, @NonNull FragmentActivity activity, @NonNull NavigationHandler navigationHandler) {
        mAnalytics = Preconditions.checkNotNull(analytics);
        mActivity = Preconditions.checkNotNull(activity);
        mNavigationHandler = Preconditions.checkNotNull(navigationHandler);
    }

    public boolean submitQuestionnaire() {
        Logger.info(this, "Submitting OCR Questionnaire");
        mAnalytics.record(Events.Ocr.OcrQuestionnaireSubmit);
        if (mQuestion1Event != null) {
            mAnalytics.record(mQuestion1Event);
        }
        if (mQuestion2Event != null) {
            mAnalytics.record(mQuestion2Event);
        }
        return mNavigationHandler.navigateBack();
    }

    public boolean dismissQuestionnaire() {
        Logger.info(this, "Dismissing OCR Questionnaire");
        mAnalytics.record(Events.Ocr.OcrQuestionnaireDismiss);
        return mNavigationHandler.navigateBack();
    }

    public void toggleQuestionnaireResponse(@IdRes int questionaireRes) {
        if (questionaireRes == R.id.ocr_questionnaire_q1_option1) {
            mQuestion1Event = Events.Ocr.OcrQuestionnaireQuestion1PerReceipt20;
        } else if (questionaireRes == R.id.ocr_questionnaire_q1_option2) {
            mQuestion1Event = Events.Ocr.OcrQuestionnaireQuestion1PerReceipt15;
        } else if (questionaireRes == R.id.ocr_questionnaire_q1_option3) {
            mQuestion1Event = Events.Ocr.OcrQuestionnaireQuestion1TooMuch;
        } else if (questionaireRes == R.id.ocr_questionnaire_q1_option4) {
            mQuestion1Event = Events.Ocr.OcrQuestionnaireQuestion1NotInterested;
        }


        if (questionaireRes == R.id.ocr_questionnaire_q2_option1) {
            mQuestion2Event = Events.Ocr.OcrQuestionnaireQuestion2DelaysOkay;
        } else if (questionaireRes == R.id.ocr_questionnaire_q2_option2) {
            mQuestion2Event = Events.Ocr.OcrQuestionnaireQuestion2NotInterested;
        }
        Logger.info(this, "OCR Answers are currently set as {} and {}", mQuestion1Event, mQuestion2Event);
    }

    public void emailAboutOcr() {
        Logger.info(this, "Emailing about OCR Questionnaire");
        mAnalytics.record(Events.Ocr.OcrQuestionnaireEmailUs);
        final Intent intent = EmailAssistant.getEmailDeveloperIntent("Automatic Scanning (ie OCR) Feedback");
        mActivity.startActivity(intent);
    }

}
