package co.smartreceipts.android.ocr.widget;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;

public class OcrInformationalPresenter implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private final OcrInformationalInteractor mInteractor;

    private final RadioGroup mQuestion1Group;
    private final RadioGroup mQuestion2Group;
    private final Button mEmailUsButton;

    public OcrInformationalPresenter(@NonNull View view, @NonNull OcrInformationalInteractor interactor) {
        mInteractor = Preconditions.checkNotNull(interactor);

        mQuestion1Group = Preconditions.checkNotNull((RadioGroup) view.findViewById(R.id.ocr_questionnaire_q1));
        mQuestion2Group = Preconditions.checkNotNull((RadioGroup) view.findViewById(R.id.ocr_questionnaire_q2));
        mEmailUsButton = Preconditions.checkNotNull((Button) view.findViewById(R.id.ocr_questionnaire_email_us));

        mQuestion1Group.setOnCheckedChangeListener(this);
        mQuestion2Group.setOnCheckedChangeListener(this);
        mEmailUsButton.setOnClickListener(this);
    }

    public void onResume() {

    }

    public void onPause() {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
        mInteractor.toggleQuestionnaireResponse(checkedId);
    }

    @Override
    public void onClick(View view) {
        mInteractor.emailAboutOcr();
    }
}
