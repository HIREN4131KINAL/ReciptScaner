package co.smartreceipts.android.ocr.info;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class OcrInformationalTooltipPresenter implements View.OnClickListener {

    private final OcrInformationalTooltipInteractor mInteractor;

    private final View mInfoButton;
    private final TextView mMessageTextView;
    private final View mDismissButton;
    private CompositeSubscription mCompositeSubscription;

    public OcrInformationalTooltipPresenter(@NonNull OcrInformationalTooltipInteractor interactor, @NonNull View infoButton) {
        mInteractor = Preconditions.checkNotNull(interactor);

        mInfoButton = Preconditions.checkNotNull(infoButton);
        mMessageTextView = Preconditions.checkNotNull((TextView) infoButton.findViewById(R.id.message));
        mDismissButton = Preconditions.checkNotNull(infoButton.findViewById(R.id.close_icon));

        mMessageTextView.setText(R.string.ocr_informational_tooltip_text);
        mInfoButton.setVisibility(View.GONE);
        mInfoButton.setOnClickListener(this);
        mDismissButton.setOnClickListener(this);
    }

    public void onResume() {
        mCompositeSubscription = new CompositeSubscription();
        mCompositeSubscription.add(mInteractor.getShowQuestionTooltipStream()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<Boolean>() {
                @Override
                public void call(Boolean aBoolean) {
                    mInfoButton.setVisibility(View.VISIBLE);
                }
            }));
    }

    public void onPause() {
        mCompositeSubscription.unsubscribe();
        mCompositeSubscription = null;
    }

    @Override
    public void onClick(View view) {
        if (view == mInfoButton) {
            mInteractor.showOcrInformation();
        } else if (view == mDismissButton) {
            mInteractor.dismissTooltip();
        }
        mInfoButton.setVisibility(View.GONE);
    }
}
